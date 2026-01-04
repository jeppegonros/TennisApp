package com.example.tennisapp.viewModel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tennisapp.NotificationHelper
import com.example.tennisapp.bluetooth.XiaoBleManager
import com.example.tennisapp.sensor.pipeline.MotionPipeline
import com.example.tennisapp.session.HitRecord
import com.example.tennisapp.session.SessionFiles
import com.example.tennisapp.session.SessionRecorder
import com.example.tennisapp.session.SessionRepository
import com.example.tennisapp.session.SessionSummary
import com.example.tennisapp.sensor.kpi.KPIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class HomeViewModel(
    app: Application,
    private val ble: XiaoBleManager
) : AndroidViewModel(app) {

    private val motionPipeline = MotionPipeline()
    private val notificationHelper = NotificationHelper(getApplication())

    private val recorder = SessionRecorder(getApplication())
    private val repo = SessionRepository(getApplication())

    private var recordingJob: Job? = null
    private var recordingStartTime = 0L
    private var lastImpactAtMs = 0L
    private val impactCooldownMs = 140L

    private var currentSessionFiles: SessionFiles? = null

    private var currentPlayerName: String = ""
    private var currentSessionNotes: String = ""

    val devices: SharedFlow<BluetoothDevice> = ble.devices
    val isXiaoConnected: StateFlow<Boolean> = ble.isConnected

    private val _kpiState = MutableStateFlow(KPIState())
    val kpiState: StateFlow<KPIState> = _kpiState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _activeSensorSource = MutableStateFlow(SensorSource.EXTERNAL_XIAO)
    val activeSensorSource: StateFlow<SensorSource> = _activeSensorSource.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _hits = MutableStateFlow<List<HitRecord>>(emptyList())
    val hits: StateFlow<List<HitRecord>> = _hits.asStateFlow()

    private val _lastHit = MutableStateFlow<HitRecord?>(null)
    val lastHit: StateFlow<HitRecord?> = _lastHit.asStateFlow()

    private val _sessionSummary = MutableStateFlow<SessionSummary?>(null)
    val sessionSummary: StateFlow<SessionSummary?> = _sessionSummary.asStateFlow()

    private val _sessions = MutableStateFlow<List<SessionSummary>>(emptyList())
    val sessions: StateFlow<List<SessionSummary>> = _sessions.asStateFlow()

    private val _selectedSessionHits = MutableStateFlow<List<HitRecord>>(emptyList())
    val selectedSessionHits: StateFlow<List<HitRecord>> = _selectedSessionHits.asStateFlow()

    fun refreshSessions() {
        _sessions.value = repo.listSessions()
    }

    fun loadSession(sessionId: String) {
        _selectedSessionHits.value = repo.loadHits(sessionId)
    }

    fun startScan() {
        if (activeSensorSource.value != SensorSource.EXTERNAL_XIAO) return
        ble.startScan()
    }

    fun connectTo(dev: BluetoothDevice) {
        try {
            ble.connect(dev)
        } catch (e: Exception) {
            viewModelScope.launch { _errorMessage.emit("Could not connect to the XIAO sensor.") }
        }
    }

    fun disconnectXiao() {
        ble.disconnect()
    }

    fun selectSensorSource(source: SensorSource) {
        _activeSensorSource.value = source
        if (isRecording.value) stopRecording()
    }

    fun startRecording(playerName: String, sessionNotes: String) {
        if (_isRecording.value) return

        if (activeSensorSource.value == SensorSource.EXTERNAL_XIAO && !isXiaoConnected.value) {
            viewModelScope.launch { _errorMessage.emit("XIAO is not connected.") }
            return
        }

        currentPlayerName = playerName.trim()
        currentSessionNotes = sessionNotes.trim()

        _isRecording.value = true
        _hits.value = emptyList()
        _lastHit.value = null
        _sessionSummary.value = null

        recordingStartTime = System.currentTimeMillis()
        lastImpactAtMs = 0L

        currentSessionFiles = recorder.start()
        Log.d("HomeViewModel", "Recording started, sessionId=${currentSessionFiles?.sessionId}")

        recordingJob = viewModelScope.launch {
            ble.imu.collect { imu ->
                val kpis = motionPipeline.update(imu)
                _kpiState.value = kpis

                recorder.appendRaw(imu)
                recorder.appendKpi(kpis)

                if (kpis.impactDetected) {
                    val now = imu.timestamp
                    if (now - lastImpactAtMs >= impactCooldownMs) {
                        lastImpactAtMs = now

                        val hit = HitRecord(
                            timestamp = now,
                            power = kpis.estimatedPower,
                            spinRpm = kpis.spinRPM,
                            impactIntensity = kpis.accelMagnitude
                        )

                        _hits.update { it + hit }
                        _lastHit.value = hit
                        recorder.appendHit(hit)
                    }
                }
            }
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        _isRecording.value = false

        val end = System.currentTimeMillis()
        val start = recordingStartTime
        val sessionId = currentSessionFiles?.sessionId ?: "unknown"

        val summaryBase = computeSummary(
            sessionId = sessionId,
            startTimeMs = start,
            endTimeMs = end,
            hits = _hits.value
        )

        val summary = summaryBase.copy(
            playerName = currentPlayerName,
            sessionNotes = currentSessionNotes
        )

        _sessionSummary.value = summary
        recorder.writeSummary(summary)
        recorder.stop()

        notificationHelper.cancelNotification()
        Log.d("HomeViewModel", "Recording stopped, sessionId=$sessionId")

        refreshSessions()
    }

    private fun computeSummary(
        sessionId: String,
        startTimeMs: Long,
        endTimeMs: Long,
        hits: List<HitRecord>
    ): SessionSummary {

        if (hits.isEmpty()) {
            return SessionSummary(
                sessionId = sessionId,
                startTimeMs = startTimeMs,
                endTimeMs = endTimeMs,
                durationMs = max(0L, endTimeMs - startTimeMs),
                hitCount = 0,

                avgPower = 0f, maxPower = 0f, minPower = 0f,
                avgSpin = 0f, maxSpin = 0f, minSpin = 0f,

                playerName = "",
                sessionNotes = ""
            )
        }

        var sumPower = 0f
        var sumSpin = 0f

        var maxPower = Float.NEGATIVE_INFINITY
        var minPower = Float.POSITIVE_INFINITY

        var maxSpin = Float.NEGATIVE_INFINITY
        var minSpin = Float.POSITIVE_INFINITY

        for (h in hits) {
            sumPower += h.power
            sumSpin += h.spinRpm

            maxPower = max(maxPower, h.power)
            minPower = min(minPower, h.power)

            maxSpin = max(maxSpin, h.spinRpm)
            minSpin = min(minSpin, h.spinRpm)
        }

        val n = hits.size.toFloat()

        return SessionSummary(
            sessionId = sessionId,
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            durationMs = max(0L, endTimeMs - startTimeMs),
            hitCount = hits.size,

            avgPower = sumPower / n,
            maxPower = maxPower,
            minPower = minPower,

            avgSpin = sumSpin / n,
            maxSpin = maxSpin,
            minSpin = minSpin,

            playerName = "",
            sessionNotes = ""
        )
    }
}

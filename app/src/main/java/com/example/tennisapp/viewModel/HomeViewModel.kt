package com.example.tennisapp.viewModel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tennisapp.NotificationHelper
import com.example.tennisapp.bluetooth.IMUData
import com.example.tennisapp.bluetooth.XiaoBleManager
import com.example.tennisapp.sensor.pipeline.MotionPipeline
import com.example.tennisapp.sensor.kpi.KPIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sqrt


class HomeViewModel(
    app: Application,
    private val ble: XiaoBleManager
) : AndroidViewModel(app) {

    /* ---------------- PROCESSING PIPELINE ---------------- */

    private val motionPipeline = MotionPipeline()

    /* ---------------- RECORDING STATE ---------------- */

    private var recordingStartTime = 0L
    val exportStartTime: Long
        get() = recordingStartTime

    private var recordingJob: Job? = null

    /* ---------------- BLE ---------------- */

    val devices: SharedFlow<BluetoothDevice> = ble.devices
    val isXiaoConnected: StateFlow<Boolean> = ble.isConnected

    /* ---------------- DATA ---------------- */

    // Raw IMU data (export / offline analysis)
    val recordedImuData = MutableStateFlow<List<IMUData>>(emptyList())

    // Live KPIs shown in UI
    private val _kpiState = MutableStateFlow(KPIState())
    val kpiState: StateFlow<KPIState> = _kpiState.asStateFlow()

    /* ---------------- UI STATE ---------------- */

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _activeSensorSource = MutableStateFlow(SensorSource.EXTERNAL_XIAO)
    val activeSensorSource: StateFlow<SensorSource> = _activeSensorSource.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val notificationHelper = NotificationHelper(getApplication())

    /* ---------------- BLE CONTROL ---------------- */

    fun startScan() {
        if (activeSensorSource.value != SensorSource.EXTERNAL_XIAO) return
        ble.startScan()
    }

    fun connectTo(dev: BluetoothDevice) {
        try {
            ble.connect(dev)
        } catch (e: Exception) {
            viewModelScope.launch {
                _errorMessage.emit("Could not connect to the XIAO sensor.")
            }
        }
    }

    fun disconnectXiao() {
        ble.disconnect()
    }

    fun selectSensorSource(source: SensorSource) {
        _activeSensorSource.value = source
        if (isRecording.value) stopRecording()
    }

    /* ---------------- RECORDING ---------------- */

    fun startRecording() {
        if (_isRecording.value) return

        if (activeSensorSource.value == SensorSource.EXTERNAL_XIAO &&
            !isXiaoConnected.value
        ) {
            viewModelScope.launch {
                _errorMessage.emit("XIAO is not connected.")
            }
            return
        }

        _isRecording.value = true
        recordedImuData.value = emptyList()
        recordingStartTime = System.currentTimeMillis()

        Log.d("HomeViewModel", "Recording started at $recordingStartTime")


        recordingJob = viewModelScope.launch {

            ble.imu.collectLatest { imu ->

                /* ---------- 1. STORE RAW DATA ---------- */
                recordedImuData.update { it + imu }

                /* ---------- 2. KPI PIPELINE ---------- */
                val kpis = motionPipeline.update(imu)
                _kpiState.value = kpis

                /* ---------- 3. NOTIFICATION ---------- */
                if (recordedImuData.value.size % 20 == 0) {

                    val accMag = sqrt(
                        imu.ax * imu.ax +
                                imu.ay * imu.ay +
                                imu.az * imu.az
                    )

                }
            }
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        _isRecording.value = false

        notificationHelper.cancelNotification()
        Log.d("HomeViewModel", "Recording stopped")
    }
}
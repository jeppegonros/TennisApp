package com.example.tennisapp.navigation

import com.example.tennisapp.view.LiveSessionScreen
import com.example.tennisapp.view.ResultsScreen
import com.example.tennisapp.view.SessionSummaryScreen
import com.example.tennisapp.view.WelcomeScreen


import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.tennisapp.viewModel.HomeViewModel

enum class Screen {
    WELCOME,
    LIVE_SESSION,
    SESSION_SUMMARY,
    RESULTS
}

@Composable
fun HomeScreen(vm: HomeViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var playerName by remember { mutableStateOf("") }
    var sessionNotes by remember { mutableStateOf("") }
    var sessionStartTime by remember { mutableStateOf(0L) }

    val devices = remember { mutableStateListOf<BluetoothDevice>() }
    val isRecording by vm.isRecording.collectAsState()
    val isXiaoConnected by vm.isXiaoConnected.collectAsState()

    val isScanning by vm.isScanning.collectAsState()

    val kpiState by vm.kpiState.collectAsState()
    val hits by vm.hits.collectAsState()
    val lastHit by vm.lastHit.collectAsState()
    val sessionSummary by vm.sessionSummary.collectAsState()
    val sessions by vm.sessions.collectAsState()

    // Check if device Bluetooth is enabled - refreshes every second
    var isBluetoothEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val bluetoothAdapter = bluetoothManager?.adapter
                isBluetoothEnabled = bluetoothAdapter?.isEnabled == true
            } catch (e: Exception) {
                isBluetoothEnabled = false
            }
            kotlinx.coroutines.delay(1000) // Check every second
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    // Collect discovered devices
    LaunchedEffect(Unit) {
        vm.devices.collect { device ->
            if (!devices.contains(device)) {
                devices.add(device)
            }
        }
    }

    // Calculate elapsed time - updates every second while recording
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isRecording, sessionStartTime) {
        if (isRecording && sessionStartTime > 0) {
            while (isRecording) {
                elapsedTime = System.currentTimeMillis() - sessionStartTime
                kotlinx.coroutines.delay(100) // Update every 100ms for smooth display
            }
        } else {
            elapsedTime = 0L
        }
    }

    when (currentScreen) {
        Screen.WELCOME -> {
            WelcomeScreen(
                playerName = playerName,
                onPlayerNameChange = { playerName = it },
                sessionNotes = sessionNotes,
                onSessionNotesChange = { sessionNotes = it },
                isXiaoConnected = isXiaoConnected,
                isScanning = isScanning,
                isBluetoothEnabled = isBluetoothEnabled,
                devices = devices,
                onStartScan = {
                    devices.clear()
                    vm.startScan()
                },
                onConnectDevice = { device ->
                    vm.connectTo(device)
                },
                onDisconnect = {
                    vm.disconnectXiao()
                },
                onNavigateToSession = {
                    currentScreen = Screen.LIVE_SESSION
                },
                onNavigateToResults = {
                    vm.refreshSessions()
                    currentScreen = Screen.RESULTS
                }
            )
        }

        Screen.LIVE_SESSION -> {
            LiveSessionScreen(
                kpiState = kpiState,
                isRecording = isRecording,
                isDeviceConnected = isXiaoConnected,
                hitCount = hits.size,
                lastHit = lastHit,
                elapsedTimeMs = elapsedTime,
                onStartRecording = {
                    sessionStartTime = System.currentTimeMillis()
                    vm.startRecording(playerName, sessionNotes)
                },
                onPauseRecording = {
                    vm.stopRecording()
                },
                onStopRecording = {
                    vm.stopRecording()
                    currentScreen = Screen.SESSION_SUMMARY
                },
                onBack = {
                    if (!isRecording) {
                        currentScreen = Screen.WELCOME

                    }
                }
            )
        }

        Screen.SESSION_SUMMARY -> {
            SessionSummaryScreen(
                sessionSummary = sessionSummary,
                onBackToWelcome = {
                    currentScreen = Screen.WELCOME
                }
            )
        }

        Screen.RESULTS -> {
            // LOGS AFEGITS AQUÍ
            Log.d("HomeScreen", "Showing RESULTS screen with ${sessions.size} sessions")
            sessions.forEach { session ->
                Log.d("HomeScreen", "Session: ${session.sessionId}, avgSpin: ${session.avgSpin}, avgPower: ${session.avgPower}")
            }

            ResultsScreen(
                sessions = sessions,
                onBackToWelcome = {
                    currentScreen = Screen.WELCOME
                },
                onSessionClick = { session ->
                    vm.loadSession(session.sessionId)
                }
            )
        }
    }
}
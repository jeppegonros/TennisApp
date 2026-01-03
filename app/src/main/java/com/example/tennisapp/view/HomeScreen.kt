package com.example.tennisapp.view

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
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
    val kpiState by vm.kpiState.collectAsState()
    val hits by vm.hits.collectAsState()
    val lastHit by vm.lastHit.collectAsState()
    val sessionSummary by vm.sessionSummary.collectAsState()
    val sessions by vm.sessions.collectAsState()

    // Check if device Bluetooth is enabled
    val isBluetoothEnabled = remember {
        derivedStateOf {
            try {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val bluetoothAdapter = bluetoothManager?.adapter
                bluetoothAdapter?.isEnabled == true
            } catch (e: Exception) {
                false
            }
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

    // Calculate elapsed time
    val elapsedTime = remember(isRecording, sessionStartTime) {
        derivedStateOf {
            if (isRecording && sessionStartTime > 0) {
                System.currentTimeMillis() - sessionStartTime
            } else {
                0L
            }
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
                isBluetoothEnabled = isBluetoothEnabled.value,
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
                hitCount = hits.size,
                lastHit = lastHit,
                elapsedTimeMs = elapsedTime.value,
                onStartRecording = {
                    sessionStartTime = System.currentTimeMillis()
                    vm.startRecording()
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
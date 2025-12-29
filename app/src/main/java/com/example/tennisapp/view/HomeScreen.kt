package com.example.tennisapp.view

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.tennisapp.viewModel.HomeViewModel

enum class Screen {
    WELCOME,
    LIVE_SESSION
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
    val elapsedTime = remember(isRecording) {
        derivedStateOf {
            if (isRecording) {
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
                    // Pause functionality can be implemented later
                    vm.stopRecording()
                },
                onStopRecording = {
                    vm.stopRecording()
                    currentScreen = Screen.WELCOME
                },
                onBack = {
                    if (!isRecording) {
                        currentScreen = Screen.WELCOME
                    }
                }
            )
        }
    }
}
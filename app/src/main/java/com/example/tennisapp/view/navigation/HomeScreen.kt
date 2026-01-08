package com.example.tennisapp.view.navigation

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.tennisapp.view.*
import com.example.tennisapp.viewModel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(vm: HomeViewModel) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isXiaoConnected by vm.isXiaoConnected.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val isRecording by vm.isRecording.collectAsState()

    val sessions by vm.sessions.collectAsState()
    val summary by vm.sessionSummary.collectAsState()
    val kpiState by vm.kpiState.collectAsState()
    val hits by vm.hits.collectAsState()
    val lastHit by vm.lastHit.collectAsState()

    val availableDevices = remember { mutableStateListOf<BluetoothDevice>() }
    LaunchedEffect(Unit) {
        vm.devices.collect { device ->
            if (!availableDevices.any { it.address == device.address }) {
                availableDevices.add(device)
            }
        }
    }

    var playerName by remember { mutableStateOf("") }
    var sessionNotes by remember { mutableStateOf("") }

    var accumulatedTimeMs by remember { mutableStateOf(0L) }
    var lastStartTimeMs by remember { mutableStateOf(0L) }
    var elapsedTimeMs by remember { mutableStateOf(0L) }

    LaunchedEffect(sessions.size) {
        if (sessions.isEmpty()) {
            vm.refreshSessions()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            if (lastStartTimeMs == 0L) {
                lastStartTimeMs = System.currentTimeMillis()
            }
        } else {
            if (lastStartTimeMs != 0L) {
                accumulatedTimeMs += System.currentTimeMillis() - lastStartTimeMs
                lastStartTimeMs = 0L
            }
        }
    }

    LaunchedEffect(isRecording, accumulatedTimeMs, lastStartTimeMs) {
        if (isRecording) {
            while (isRecording) {
                elapsedTimeMs = accumulatedTimeMs + (System.currentTimeMillis() - lastStartTimeMs)
                delay(250)
            }
        } else {
            elapsedTimeMs = accumulatedTimeMs
        }
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController,
                visible = true,
                isXiaoConnected = isXiaoConnected
            )
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    playerName = playerName,
                    onPlayerNameChange = { playerName = it },
                    sessionNotes = sessionNotes,
                    onSessionNotesChange = { sessionNotes = it },
                    isXiaoConnected = isXiaoConnected,
                    isBluetoothEnabled = true,
                    isScanning = isScanning,
                    devices = availableDevices,
                    onStartScan = vm::startScan,
                    onConnectDevice = vm::connectTo,
                    onDisconnect = vm::disconnectXiao,

                    onNavigateToSession = {
                        if (isXiaoConnected) {
                            vm.startRecording(playerName, sessionNotes)
                            navController.navigate(Screen.Live.route)
                        }
                    },

                    onNavigateToResults = {
                        navController.navigate(Screen.Summary.route)
                    }
                )
            }

            composable(Screen.Live.route) {
                LiveSessionScreen(
                    kpiState = kpiState,
                    isRecording = isRecording,
                    isDeviceConnected = isXiaoConnected,
                    hitCount = hits.size,
                    lastHit = lastHit,
                    elapsedTimeMs = elapsedTimeMs,

                    onStartRecording = {
                        vm.startRecording(playerName, sessionNotes)
                    },

                    onPauseRecording = {
                        vm.pauseRecording()
                    },

                    onStopRecording = {
                        elapsedTimeMs = accumulatedTimeMs

                        vm.stopRecording()

                        accumulatedTimeMs = 0L
                        lastStartTimeMs = 0L
                        elapsedTimeMs = 0L

                        navController.navigate(Screen.Results.route) {
                            popUpTo(Screen.Live.route) { inclusive = true }
                        }
                    },

                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Results.route) {
                SessionSummaryScreen(
                    sessionSummary = summary,
                    onBackToWelcome = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Summary.route) {
                ResultsScreen(
                    sessions = sessions,
                    onSessionClick = { },
                    onBackToWelcome = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

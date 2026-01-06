package com.example.tennisapp.view.navigation

import android.bluetooth.BluetoothDevice
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.example.tennisapp.view.*
import com.example.tennisapp.viewModel.HomeViewModel
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun HomeScreen(vm: HomeViewModel) {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isXiaoConnected by vm.isXiaoConnected.collectAsState()

    val showBottomBar = true

    // Collect all necessary states from the ViewModel
    val isScanning by vm.isScanning.collectAsState()

    val availableDevices = remember { mutableStateListOf<BluetoothDevice>() }
    LaunchedEffect(Unit) {
        vm.devices.collect { device ->
            // Add device to the list only if it's not already there
            if (!availableDevices.any { it.address == device.address }) {
                availableDevices.add(device)
            }
        }
    }

    val sessions by vm.sessions.collectAsState()
    val summary by vm.sessionSummary.collectAsState()
    val kpiState by vm.kpiState.collectAsState()
    val hits by vm.hits.collectAsState()
    val lastHit by vm.lastHit.collectAsState()
    val isRecording by vm.isRecording.collectAsState()

    // Simple state holders for player name and notes, managed within the Composable's scope
    var playerName by remember { mutableStateOf("") }
    var sessionNotes by remember { mutableStateOf("") }

    // Refresh sessions when the screen is first composed
    LaunchedEffect(Unit) {
        vm.refreshSessions()
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                navController = navController,
                visible = showBottomBar
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
                    isBluetoothEnabled = true, // Assuming this is handled or will be added to VM
                    isScanning = isScanning,
                    devices = availableDevices,
                    onStartScan = vm::startScan,
                    onConnectDevice = vm::connectTo,
                    onDisconnect = vm::disconnectXiao,
                    onNavigateToSession = {
                        // Pass the player name and notes when starting the session
                        if (isXiaoConnected) {
                            vm.startRecording(playerName, sessionNotes)
                            navController.navigate(Screen.Live.route)
                        }
                    },
                    onNavigateToResults = {
                        navController.navigate(Screen.Results.route)
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
                    elapsedTimeMs = 0L, // Placeholder, this should be calculated in VM
                    onStartRecording = { vm.startRecording(playerName, sessionNotes) },
                    onPauseRecording = { vm.pauseRecording() },
                    onStopRecording = {
                        vm.stopRecording()
                        navController.navigate(Screen.Results.route) {
                            popUpTo(Screen.Live.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
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
                    onSessionClick = { session ->
                    },
                    onBackToWelcome = { navController.popBackStack() }
                )
            }
        }
    }
}
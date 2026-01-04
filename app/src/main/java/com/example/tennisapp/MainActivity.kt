package com.example.tennisapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.tennisapp.bluetooth.XiaoBleManager
import com.example.tennisapp.ui.theme.TennisAppTheme
import com.example.tennisapp.navigation.HomeScreen
import com.example.tennisapp.viewModel.HomeViewModel
import com.example.tennisapp.viewModel.HomeViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ble = XiaoBleManager(this)
        val factory = HomeViewModelFactory(application, ble)
        val vm = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        setContent {
            TennisAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(vm)
                }
            }
        }
    }
}
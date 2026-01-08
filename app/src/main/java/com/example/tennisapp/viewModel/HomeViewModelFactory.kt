package com.example.tennisapp.viewModel



import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tennisapp.bluetooth.XiaoBleManager

class HomeViewModelFactory(
    private val app: Application,
    private val ble: XiaoBleManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(app, ble) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
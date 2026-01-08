package com.example.tennisapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

data class IMUData(
    val ax: Float,
    val ay: Float,
    val az: Float,
    val gx: Float,
    val gy: Float,
    val gz: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@SuppressLint("MissingPermission")
class XiaoBleManager(private val context: Context) {

    private val adapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanner = adapter?.bluetoothLeScanner
    private var gatt: BluetoothGatt? = null

    private val SERVICE_UUID =
        UUID.fromString("12345678-1234-5678-1234-56789ABC0001")
    private val CHAR_UUID =
        UUID.fromString("12345678-1234-5678-1234-56789ABC0002")

    private val CONFIG_UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val _imu = MutableSharedFlow<IMUData>(
        replay = 1,
        extraBufferCapacity = 64
    )
    val imu: SharedFlow<IMUData> = _imu

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _devices = MutableSharedFlow<BluetoothDevice>(
        replay = 20,
        extraBufferCapacity = 50
    )
    val devices: SharedFlow<BluetoothDevice> = _devices

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            _devices.tryEmit(result.device)
            Log.d("SCAN", "Found device: ${result.device.address} name=${result.device.name}")
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    fun startScan() {
        if (adapter == null || adapter?.isEnabled != true || scanner == null) {
            Log.e("XiaoBleManager", "Bluetooth not ready or adapter missing.")
            return
        }

        if (!isLocationEnabled()) {
            Log.e("XiaoBleManager", "Location services must be enabled for BLE scanning.")
            return
        }

        try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanner.startScan(null, settings, scanCallback)
            Log.d("XiaoBleManager", "BLE scanning started.")
        } catch (e: SecurityException) {
            Log.e("XiaoBleManager", "Lacks BLE authorization.", e)
        }
    }

    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (_: Exception) { }
        Log.d("XiaoBleManager", "BLE scanning stopped.")
    }

    fun connect(device: BluetoothDevice) {
        stopScan()
        gatt = device.connectGatt(context, false, gattCallback)
        Log.d("XiaoBleManager", "Trying to connect to ${device.address}")
    }

    fun disconnect() {
        try { gatt?.close() } catch (_: Exception) { }
        gatt = null
        _isConnected.value = false
        Log.d("XiaoBleManager", "Disconnected manually.")
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            g: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("XiaoBleManager", "Connected, starting service search.")
                    _isConnected.value = true
                    g.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("XiaoBleManager", "Disconnected.")
                    _isConnected.value = false
                    g.close()
                }
            } else {
                Log.e("XiaoBleManager", "Connection failed: $status")
                _isConnected.value = false
                g.close()
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("BLE", "Service discovery failed")
                return
            }

            val service = g.getService(SERVICE_UUID)
            val char = service?.getCharacteristic(CHAR_UUID)

            if (char == null) {
                Log.e("BLE", "IMU characteristic not found")
                return
            }

            Log.d("BLE", "Characteristic found, enabling notifications")

            g.setCharacteristicNotification(char, true)

            val descriptor = char.getDescriptor(CONFIG_UUID)
            if (descriptor == null) {
                Log.e("BLE", "Missing 0x2902 descriptor")
                return
            }

            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            val ok = g.writeDescriptor(descriptor)
            Log.d("BLE", "Descriptor write started: $ok")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid != CHAR_UUID) return
            if (value.size != 12) {
                Log.w("BLE", "Incorrect package length: ${value.size}")
                return
            }

            val bb = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)

            val imuData = IMUData(
                ax = bb.short.toFloat(),
                ay = bb.short.toFloat(),
                az = bb.short.toFloat(),
                gx = bb.short.toFloat(),
                gy = bb.short.toFloat(),
                gz = bb.short.toFloat()
            )

            //Log.d("BLE", "IMU packet: $imuData")

            _imu.tryEmit(imuData)
        }
    }
}
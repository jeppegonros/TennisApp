package com.example.tennisapp.bluetooth

package com.example.mybluetooth.bluetooth

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface to standardize the retrieval of IMU data, regardless of source
 * (external BLE sensor such as XIAO, or internal Android sensor).
 */
interface SensorAcquirer {

    val imu: SharedFlow<IMUData>

    fun startAcquisition()

    fun stopAcquisition()
}
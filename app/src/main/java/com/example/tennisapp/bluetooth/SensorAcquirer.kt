package com.example.tennisapp.bluetooth

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface to standardize the retrieval of IMU data, regardless of source
 * (external BLE sensor such as XIAO, or internal Android sensor).
 */

/** Ricardo Notes, I think this file is no longer necessary!. There is only external sensor data
 */
interface SensorAcquirer {

    val imu: SharedFlow<IMUData>

    fun startAcquisition()

    fun stopAcquisition()
}
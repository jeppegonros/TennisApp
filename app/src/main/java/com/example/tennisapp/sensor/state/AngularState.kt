package com.example.tennisapp.sensor.state

import com.example.tennisapp.sensor.imu.Vector3

data class AngularState(
    val gyro: Vector3,
    val spinRate: Float
)
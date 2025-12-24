package com.example.tennisapp.sensor.state

import com.example.tennisapp.sensor.imu.Vector3


data class LinearState(
    val acc: Vector3,
    val verticalVelocity: Float,
    val speed: Float
)
package com.example.tennisapp.sensor.imu

import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {
    fun magnitude(): Float = sqrt(x*x + y*y + z*z)
}
package com.example.tennisapp.sensor.imu

import com.example.tennisapp.bluetooth.IMUData

data class ImuSample(
    val timeSec: Float,
    val acc: Vector3,
    val gyro: Vector3
) {
    companion object {
        fun fromBle(data: IMUData): ImuSample {
            val g = 9.81f

            return ImuSample(
                timeSec = data.timestamp / 1000f,
                acc = Vector3(
                    data.ax / 1000f * g,
                    data.ay / 1000f * g,
                    data.az / 1000f * g
                ),
                gyro = Vector3(
                    data.gx / 100f,
                    data.gy / 100f,
                    data.gz / 100f
                )
            )
        }
    }
}
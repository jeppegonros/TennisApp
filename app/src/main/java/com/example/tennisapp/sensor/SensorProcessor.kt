package com.example.tennisapp.sensor


package com.example.mybluetooth.sensor

import android.util.Log
import com.example.mybluetooth.bluetooth.IMUData
import kotlin.math.atan2
import kotlin.math.sqrt

data class ProcessedData(
    val alg1: Float,
    val alg2: Float,
    val timestamp: Long
)

class SensorProcessor {

    private var lastEWMA = 0f
    private var lastFusion = 0f

    private val alphaEWMA = 0.2f
    private val alphaFusion = 0.98f

    fun compute(imu: IMUData): ProcessedData {
        try {
            val ax_g = imu.ax / 1000.0f
            val ay_g = imu.ay / 1000.0f
            val az_g = imu.az / 1000.0f
            val gy_dps = imu.gy / 100.0f

            val denominator = sqrt(ax_g * ax_g + az_g * az_g)
            if (denominator < 0.001f) {
                Log.w("SensorProcessor", "Denominator too small, skipping")
                return ProcessedData(alg1 = lastEWMA, alg2 = lastFusion, timestamp = imu.timestamp)
            }

            val originalAngle = atan2(ay_g, denominator) * (180 / Math.PI).toFloat()

            val angleAcc = 90f - originalAngle //adjust due to assignment req


            lastEWMA = alphaEWMA * angleAcc + (1 - alphaEWMA) * lastEWMA

            val gyroAngle = lastFusion + gy_dps * 0.04f
            lastFusion = alphaFusion * gyroAngle + (1 - alphaFusion) * angleAcc

            return ProcessedData(
                alg1 = lastEWMA,
                alg2 = lastFusion,
                timestamp = imu.timestamp
            )
        } catch (e: Exception) {
            Log.e("SensorProcessor", "Error computing angles", e)
            return ProcessedData(alg1 = lastEWMA, alg2 = lastFusion, timestamp = imu.timestamp)
        }
    }
}

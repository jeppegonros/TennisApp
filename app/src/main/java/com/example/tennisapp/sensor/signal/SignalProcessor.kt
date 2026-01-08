package com.example.tennisapp.sensor.signal

import com.example.tennisapp.sensor.imu.ImuSample
import com.example.tennisapp.sensor.state.AngularState
import com.example.tennisapp.sensor.state.LinearState

class SignalProcessor {

    private val fs = 52f
    private val dt = 1f / fs

    // Filters for linear motion
    private val accLPF = LowPassFilter(alpha = 0.2f)
    private val velHPF = HighPassIntegrator(alpha = 0.98f)

    // Filters for angular motion
    private val gyroLPF = LowPassFilter(alpha = 0.3f)

    fun update(sample: ImuSample): ProcessedSignals {

        val accMag = sample.acc.magnitude()
        val accFiltered = accLPF.update(accMag - 9.81f) // Gravity compensated
        val velZ = velHPF.update(accFiltered, dt)

        val linear = LinearState(
            acc = sample.acc,
            speed = accMag,
            verticalVelocity = velZ
        )

        val gyroMag = sample.gyro.magnitude()
        val gyroFiltered = gyroLPF.update(gyroMag)
        val spinRateRps = gyroFiltered / (2 * Math.PI.toFloat()) // RPS

        val angular = AngularState(
            gyro = sample.gyro,
            spinRate = spinRateRps
        )

        return ProcessedSignals(
            time = sample.timeSec,
            linear = linear,
            angular = angular
        )
    }
}

data class ProcessedSignals(
    val time: Float,
    val linear: LinearState,
    val angular: AngularState
)
package com.example.tennisapp.sensor.pipeline

import com.example.tennisapp.bluetooth.IMUData
import com.example.tennisapp.sensor.events.ApexEvent
import com.example.tennisapp.sensor.events.EventDetector
import com.example.tennisapp.sensor.events.ImpactEvent
import com.example.tennisapp.sensor.kpi.FlightTimeKPI
import com.example.tennisapp.sensor.kpi.KPIState
import com.example.tennisapp.sensor.kpi.PowerKPI
import com.example.tennisapp.sensor.kpi.SpinRateKPI
import com.example.tennisapp.sensor.imu.ImuSample
import com.example.tennisapp.sensor.signal.SignalProcessor
import com.example.tennisapp.sensor.state.MotionState

class MotionPipeline {

    private val signalProcessor = SignalProcessor()
    private val eventDetector = EventDetector()

    private val flightTimeKPI = FlightTimeKPI()
    private val spinRateKPI = SpinRateKPI()
    private val powerKPI = PowerKPI()

    private var lastState: MotionState = MotionState.zero()

    fun update(imu: IMUData): KPIState {

        // 1) Correct scaling from BLE units
        val sample = ImuSample.fromBle(imu)

        val signals = signalProcessor.update(sample)

        val newState = MotionState(
            time = signals.time,
            linear = signals.linear,
            angular = signals.angular
        )

        // 2) Events
        val events = eventDetector.update(newState, lastState)

        // 3) KPIs
        flightTimeKPI.update(newState, events)
        spinRateKPI.update(newState, events)
        powerKPI.update(newState, events)

        lastState = newState

        val impact = events.any { it is ImpactEvent }
        val apex = events.any { it is ApexEvent }

        return KPIState(
            timestamp = imu.timestamp,
            accelMagnitude = newState.linear.acc.magnitude(),
            impactDetected = impact,
            apexDetected = apex,
            angularSpeed = newState.angular.gyro.magnitude(),
            spinRPM = spinRateKPI.value() ?: 0f,
            estimatedPower = powerKPI.value() ?: 0f,
            timeSinceImpactMs = 0L
        )
    }
}

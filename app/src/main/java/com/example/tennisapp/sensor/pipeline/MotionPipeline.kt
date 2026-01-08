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

    private var lastState: MotionState? = null

    fun update(imu: IMUData): KPIState {

        val sample = ImuSample.fromBle(imu)

        val signals = signalProcessor.update(sample)

        val newState = MotionState(
            time = signals.time,
            linear = signals.linear,
            angular = signals.angular
        )

        val events = if (lastState == null) {
            emptyList()
        } else {
            eventDetector.update(newState, lastState!!)
        }

        flightTimeKPI.update(newState, events)
        spinRateKPI.update(newState, events)
        powerKPI.update(newState, events)

        lastState = newState

        val impactDetected = events.any { it is ImpactEvent }
        val apexDetected = events.any { it is ApexEvent }

        return KPIState(
            timestamp = imu.timestamp,
            accelMagnitude = newState.linear.acc.magnitude(),
            impactDetected = impactDetected,
            apexDetected = apexDetected,
            angularSpeed = newState.angular.gyro.magnitude(),
            spinRPM = spinRateKPI.value() ?: 0f,
            estimatedPower = powerKPI.value() ?: 0f,
            timeSinceImpactMs = 0L
        )
    }
}

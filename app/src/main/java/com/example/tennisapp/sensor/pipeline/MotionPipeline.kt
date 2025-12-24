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
import com.example.tennisapp.sensor.imu.Vector3
import com.example.tennisapp.sensor.signal.SignalProcessor
import com.example.tennisapp.sensor.state.AngularState
import com.example.tennisapp.sensor.state.LinearState
import com.example.tennisapp.sensor.state.MotionState

/**
 * Connects BLE -->KPIs
 */

class MotionPipeline {

    // Signal and Event Processors
    private val signalProcessor = SignalProcessor()
    private val eventDetector = EventDetector()

    // KPI Calculators
    private val flightTimeKPI = FlightTimeKPI()
    private val spinRateKPI = SpinRateKPI()
    private val powerKPI = PowerKPI()

    // Keep track of the last known state
    private var lastState = MotionState(
        time = 0f,
        linear = LinearState(),
        angular = AngularState()
    )

    fun update(imu: IMUData): KPIState {

        // 1. Process raw IMU data into a richer MotionState
        val sample = ImuSample(
            timeSec = imu.timestamp / 1000f,
            acc = Vector3(imu.ax, imu.ay, imu.az),
            gyro = Vector3(imu.gx, imu.gy, imu.gz)
        )
        val signals = signalProcessor.update(sample)
        val newState = MotionState(
            time = signals.time,
            linear = signals.linear,
            angular = signals.angular
        )

        // 2. Detect discrete events from the new state
        val events = eventDetector.update(newState, lastState)

        // 3. Update all KPIs with the new state and events
        flightTimeKPI.update(newState, events)
        spinRateKPI.update(newState, events)
        powerKPI.update(newState, events)

        // 4. Update the last known state
        lastState = newState

        // 5. Return the aggregated KPI state for the UI
        return KPIState(
            timestamp = imu.timestamp,
            accelMagnitude = newState.linear.acc.magnitude(),
            impactDetected = events.any { it is ImpactEvent },
            apexDetected = events.any { it is ApexEvent },
            spinRPM = spinRateKPI.value() ?: 0f,
            estimatedPower = powerKPI.value() ?: 0f,
            // Note: FlightTimeKPI now calculates duration between release and impact.
            // If you still need time since the *last* impact, you can manage it here.
            timeSinceImpactMs = 0L // This can be re-implemented if needed
        )
    }
}

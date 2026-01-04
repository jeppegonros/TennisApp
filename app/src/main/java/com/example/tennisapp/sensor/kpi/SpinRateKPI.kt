package com.example.tennisapp.sensor.kpi

import com.example.tennisapp.sensor.events.Event
import com.example.tennisapp.sensor.state.MotionState

class SpinRateKPI : Kpi<Float> {

    // Smoothed live value, so it can go both up and down
    private var smoothed = 0f

    // 0.1 to 0.3 is usually good. Higher = more responsive, lower = more stable
    private val alpha = 0.18f

    override fun update(state: MotionState, events: List<Event>) {
        val v = state.angular.spinRate
        smoothed += alpha * (v - smoothed)
    }

    override fun value(): Float = smoothed

    override fun reset() {
        smoothed = 0f
    }
}

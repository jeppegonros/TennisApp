package com.example.tennisapp.sensor.kpi

import com.example.tennisapp.sensor.events.Event
import com.example.tennisapp.sensor.state.MotionState

class PowerKPI : Kpi<Float> {

    private var smoothed = 0f

    private val alpha = 0.16f

    override fun update(state: MotionState, events: List<Event>) {
        val proxy = state.linear.speed * state.angular.spinRate
        smoothed += alpha * (proxy - smoothed)
    }

    override fun value(): Float = smoothed

    override fun reset() {
        smoothed = 0f
    }
}

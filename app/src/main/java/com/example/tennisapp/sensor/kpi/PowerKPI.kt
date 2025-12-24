package com.example.tennisapp.sensor.kpi

import com.example.tennisapp.sensor.events.Event
import com.example.tennisapp.sensor.state.MotionState

class PowerKPI : Kpi<Float> {

    private var peak = 0f

    override fun update(state: MotionState, events: List<Event>) {
        val proxy = state.linear.speed * state.angular.spinRate
        peak = maxOf(peak, proxy)
    }

    override fun value(): Float = peak
    override fun reset() { peak = 0f }
}
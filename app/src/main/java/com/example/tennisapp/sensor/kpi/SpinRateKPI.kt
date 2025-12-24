package com.example.tennisapp.sensor.kpi

import com.example.tennisapp.sensor.events.Event
import com.example.tennisapp.sensor.state.MotionState

class SpinRateKPI : Kpi<Float> {

    private var peak = 0f

    override fun update(state: MotionState, events: List<Event>) {
        peak = maxOf(peak, state.angular.spinRate)
    }

    override fun value(): Float = peak
    override fun reset() { peak = 0f }
}
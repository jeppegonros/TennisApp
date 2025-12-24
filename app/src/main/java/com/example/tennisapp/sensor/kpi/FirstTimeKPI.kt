package com.example.tennisapp.sensor.kpi

import com.example.tennisapp.sensor.events.Event
import com.example.tennisapp.sensor.events.ImpactEvent
import com.example.tennisapp.sensor.events.ReleaseEvent
import com.example.tennisapp.sensor.state.MotionState

class FlightTimeKPI : Kpi<Float> {

    private var t0: Float? = null
    private var t1: Float? = null

    override fun update(state: MotionState, events: List<Event>) {
        events.forEach {
            when (it) {
                is ReleaseEvent -> t0 = it.time
                is ImpactEvent -> t1 = it.time
                else -> {}
            }
        }
    }

    override fun value(): Float? =
        if (t0 != null && t1 != null) t1!! - t0!! else null

    override fun reset() {
        t0 = null
        t1 = null
    }
}
package com.example.tennisapp.sensor.events

import com.example.tennisapp.sensor.state.MotionState

class EventDetector {

    private var inFlight = false
    private var lastVel = 0f

    fun update(state: MotionState, lastState: MotionState): List<Event> {

        val events = mutableListOf<Event>()

        if (!inFlight && state.linear.acc.z < -7f) {
            inFlight = true
            events.add(ReleaseEvent(state.time))
        }

        if (inFlight && lastVel > 0f && state.linear.verticalVelocity <= 0f) {
            events.add(ApexEvent(state.time))
        }

        if (inFlight && state.linear.acc.magnitude() > 20f) {
            inFlight = false
            events.add(ImpactEvent(state.time))
        }

        lastVel = state.linear.verticalVelocity
        return events
    }
}
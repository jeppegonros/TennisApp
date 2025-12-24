package com.example.tennisapp.sensor.kpi

import com.example.tennisapp.sensor.events.Event
import com.example.tennisapp.sensor.state.MotionState

interface Kpi<T> {
    fun update(state: MotionState, events: List<Event>)
    fun value(): T?
    fun reset()
}
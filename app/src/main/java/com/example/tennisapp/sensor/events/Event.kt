package com.example.tennisapp.sensor.events

sealed class Event(open val time: Float)

data class ReleaseEvent(override val time: Float) : Event(time)
data class ApexEvent(override val time: Float) : Event(time)
data class ImpactEvent(override val time: Float) : Event(time)
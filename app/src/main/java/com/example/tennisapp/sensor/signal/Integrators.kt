package com.example.tennisapp.sensor.signal

class Integrator {
    private var value = 0f
    fun update(x: Float, dt: Float): Float {
        value += x * dt
        return value
    }
    fun reset() { value = 0f }
}
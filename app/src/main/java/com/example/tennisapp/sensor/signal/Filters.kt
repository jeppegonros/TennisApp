package com.example.tennisapp.sensor.signal

class LowPassFilter(private val alpha: Float) {
    private var prev = 0f
    fun update(x: Float): Float {
        prev = alpha * x + (1 - alpha) * prev
        return prev
    }
}

class HighPassIntegrator(private val alpha: Float) {
    private var prev = 0f
    fun update(x: Float, dt: Float): Float {
        prev = alpha * (prev + x * dt)
        return prev
    }
}
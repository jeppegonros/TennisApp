package com.example.tennisapp.sensor.kpi

data class KPIState(

    val timestamp: Long = 0L,

    val accelMagnitude: Float = 0f,

    val impactDetected: Boolean = false,

    val apexDetected: Boolean = false,

    val angularSpeed: Float = 0f,

    val spinRPM: Float = 0f,

    val estimatedPower: Float = 0f,

    val timeSinceImpactMs: Long = 0L
)
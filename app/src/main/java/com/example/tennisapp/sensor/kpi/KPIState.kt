package com.example.tennisapp.sensor.kpi

/**
 * High-level KPIs derived from IMU data.
 * This is what the UI observes.
 */
data class KPIState(

    /* -------- Timing -------- */
    val timestamp: Long = 0L,

    /* -------- Acceleration -------- */
    val accelMagnitude: Float = 0f,   // m/s² or raw units (for now)

    /* -------- Events -------- */
    val impactDetected: Boolean = false,
    val apexDetected: Boolean = false,

    /* -------- Motion -------- */
    val angularSpeed: Float = 0f,      // deg/s or rad/s
    val spinRPM: Float = 0f,

    /* -------- Performance -------- */
    val estimatedPower: Float = 0f,

    /* -------- Flight -------- */
    val timeSinceImpactMs: Long = 0L
)
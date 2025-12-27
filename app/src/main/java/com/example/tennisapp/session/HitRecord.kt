package com.example.tennisapp.session

data class HitRecord(
    val timestamp: Long,
    val power: Float,
    val spinRpm: Float,
    val impactIntensity: Float
)

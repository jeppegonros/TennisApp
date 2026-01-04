package com.example.tennisapp.session

data class SessionSummary(
    val sessionId: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val hitCount: Int,

    val avgPower: Float,
    val maxPower: Float,
    val minPower: Float,

    val avgSpin: Float,
    val maxSpin: Float,
    val minSpin: Float,

    val playerName: String = "",
    val sessionNotes: String = ""
)

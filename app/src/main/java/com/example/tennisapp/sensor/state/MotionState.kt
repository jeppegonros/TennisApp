package com.example.tennisapp.sensor.state


data class MotionState(
    val time: Float,
    val linear: LinearState,
    val angular: AngularState
) {

    var lastImpactTime: Long = 0L
    var lastApexTime: Long = 0L
}

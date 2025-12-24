package com.example.tennisapp.sensor.state


data class MotionState(
    val time: Float,
    val linear: LinearState,
    val angular: AngularState
) {
    // These properties track history and are not part of the core state snapshot.
    // They can be managed by the pipeline if needed.
    var lastImpactTime: Long = 0L
    var lastApexTime: Long = 0L
}

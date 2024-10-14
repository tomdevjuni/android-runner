package com.example.run_tracker_native_app.dataclass

data class AchievementData(
    val distanceUnit: String,
    val distance: Int,
    val image: Int,
    var isCompleted: Boolean,
    var padding: Float? = null,
)

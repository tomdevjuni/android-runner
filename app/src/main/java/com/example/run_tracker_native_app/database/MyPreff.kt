
package com.example.run_tracker_native_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_pref")
data class MyPref(
    @PrimaryKey(autoGenerate = true) var id: Int? =null,
    val dailyGoal: Int = 0,
    val distanceUnit: String = "km",
    val gender: String = "Male",
    val reminderTimeHour: Int? = null,
    val reminderTimeMinute: Int? = null,
    val reminderDays: List<Int> = emptyList(),
    val language: String = "English",
    val isSynchronized: Boolean = false
)


package com.example.run_tracker_native_app.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.util.Date

@Entity(tableName = "my_location_table")
data class MyLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? =null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val currantSession: Int = 0,
    val speed: Float = 0.0F,
    val foreground: Boolean = true,
    val date: Date = Date()
) {

    override fun toString(): String {
        val appState = if (foreground) {
            "in app"
        } else {
            "in BG"
        }

        return "$latitude, $longitude $appState on " +
                "${DateFormat.getDateTimeInstance().format(date)}.\n"
    }
}

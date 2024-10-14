package com.example.run_tracker_native_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "my_running_table")
data class MyRunningEntity(

    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,

    val timeInSeconds: Int = 0,
    val distance: Float = 0.0F,

    @ColumnInfo(name = "avgSpeed")
    val avgSpeed: Double = 0.0,

    @ColumnInfo(name = "image")
    var image : ByteArray? = null,
    @ColumnInfo(name = "imageString") var imageString: String? = null,

    @ColumnInfo(name = "pointsString")
    val pointsString: String? = null,
    val kcal: Float = 0.0F,
    val date: Date = Date(),
    var isSynchronized: Boolean = false,
    val isDeleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MyRunningEntity

        if (id != other.id) return false
        if (timeInSeconds != other.timeInSeconds) return false
        if (distance != other.distance) return false
        if (avgSpeed != other.avgSpeed) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (kcal != other.kcal) return false
        return date == other.date
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + timeInSeconds
        result = 31 * result + distance.hashCode()
        result = 31 * result + avgSpeed.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + kcal.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }
}

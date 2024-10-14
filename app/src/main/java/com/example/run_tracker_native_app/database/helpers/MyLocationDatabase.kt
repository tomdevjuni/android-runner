package com.example.run_tracker_native_app.database.helpers

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.run_tracker_native_app.database.MyLocationEntity
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity

private const val DATABASE_NAME = "my-location-database"
@Database(entities = [MyLocationEntity::class, MyRunningEntity::class, MyPref::class], version = 1, exportSchema = false)
@TypeConverters(MyLocationTypeConverters::class)
abstract class MyLocationDatabase : RoomDatabase() {
    abstract fun locationDao(): MyLocationDao

    companion object {
        // For Singleton instantiation
        @Volatile private var INSTANCE: MyLocationDatabase? = null

        fun getInstance(context: Context): MyLocationDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): MyLocationDatabase {
            return Room.databaseBuilder(
                context,
                MyLocationDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}
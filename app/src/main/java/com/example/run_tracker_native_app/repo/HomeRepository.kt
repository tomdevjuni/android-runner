package com.example.run_tracker_native_app.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.helpers.MyLocationDatabase
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.util.concurrent.ExecutorService

class HomeRepository private constructor(
    private val myLocationDatabase: MyLocationDatabase,
    private val executor: ExecutorService
) {
    private val locationDao = myLocationDatabase.locationDao()
    fun getRunningHistory(): LiveData<List<MyRunningEntity>> = locationDao.getRunningHistory()
    fun getMyPref(): LiveData<MyPref> = locationDao.getMyPref()
    fun getMyPrefForOnce(): MyPref = locationDao.getMyPrefForOnce()
    fun getTotalDistanceToday(date: Long): LiveData<Float> = locationDao.getTotalDistanceToday(date)
    fun getTotalDistanceAllTime(): LiveData<Float> = locationDao.getTotalDistanceAllTime()
    fun getTotalAllTime(): LiveData<Int> = locationDao.getTotalAllTime()
    fun getTotalCal(): LiveData<Float> = locationDao.getTotalCal()
    fun getTotalAvgSpeed(): LiveData<Float> = locationDao.getTotalAvgSpeed()
    fun getLongestDistance(): LiveData<Float> = locationDao.getLongestDistance()
    fun getTopSpeed(): LiveData<Float> = locationDao.getTopSpeed()

    fun getLongestDuration(): LiveData<Int> = locationDao.getLongestDuration()

    companion object {
        @Volatile
        private var INSTANCE: HomeRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): HomeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HomeRepository(
                    MyLocationDatabase.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}
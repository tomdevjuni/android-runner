package com.example.run_tracker_native_app.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.helpers.MyLocationDatabase
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.util.concurrent.ExecutorService

class ReportRepository private constructor(
    private val myLocationDatabase: MyLocationDatabase,
    private val executor: ExecutorService
) {
    private val locationDao = myLocationDatabase.locationDao()
    fun getMyPref(): LiveData<MyPref> = locationDao.getMyPref()
    fun getHistoryFromDateToDate(fromDate: Long, toDate: Long): LiveData<List<MyRunningEntity>> =
        locationDao.getHistoryFromDateToDate(fromDate,toDate)

    companion object {
        @Volatile
        private var INSTANCE: ReportRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): ReportRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ReportRepository(
                    MyLocationDatabase.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}
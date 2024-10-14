package com.example.run_tracker_native_app.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.helpers.MyLocationDatabase
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

class HistoryRepository private constructor(
    private val myLocationDatabase: MyLocationDatabase,
    private val executor: ExecutorService
) {
    private val locationDao = myLocationDatabase.locationDao()
    fun getAllRunningHistory(): LiveData<List<MyRunningEntity>> = locationDao.getAllRunningHistory()
    fun getRunningHistoryById(id: Int): LiveData<MyRunningEntity> =
        locationDao.getRunningHistoryById(id)

    fun getMyPref(): LiveData<MyPref> = locationDao.getMyPref()
    fun softDeleteHistoryByIds(idList: List<Int>) {
        executor.execute {
            locationDao.softDeleteHistoryByIds(idList)
            locationDao.deleteLocationsBySessionIds(idList)
        }
    }

    fun hardDeleteHistoryByIds(idList: List<Int>) {
        executor.execute {
            locationDao.hardDeleteHistoryByIds(idList)
        }
    }

    fun updateIsSyncMyHistory(isSynced: Boolean, idList: List<Int>) {
        executor.execute {
            locationDao.updateIsSyncMyHistory(isSynced, idList)
        }
    }

    fun getAllRunningHistoryOnce(): List<MyRunningEntity> {
        val callable = Callable { locationDao.getAllRunningHistoryOnce() }
        val future = executor.submit(callable)
        return future.get()
    }

    fun insertAll(myRunningEntityData: List<MyRunningEntity>) {
        executor.execute {
            locationDao.insertAll(myRunningEntityData)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HistoryRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): HistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HistoryRepository(
                    MyLocationDatabase.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}
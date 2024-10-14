package com.example.run_tracker_native_app.repo

import android.content.Context
import com.example.run_tracker_native_app.database.helpers.MyLocationDatabase
import com.example.run_tracker_native_app.database.MyPref
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

class IntroProfileRepository private constructor(
    private val myLocationDatabase: MyLocationDatabase,
    private val executor: ExecutorService
) {
    private val locationDao = myLocationDatabase.locationDao()
    fun updateGenderMyPref(gender: String) {
        executor.execute {
            locationDao.updateGenderMyPref(gender)
        }
    }

    fun getGenderMyPref() : String {
        val callable = Callable { locationDao.getGenderMyPref() }
        val future = executor.submit(callable)
        return future.get()
    }

    fun updateDistanceUnitMyPref(gender: String) {
        executor.execute {
            locationDao.updateDistanceUnitMyPref(gender)
        }
    }

fun updateLanguageUnitMyPref(language: String) {
        executor.execute {
            locationDao.updateLanguageUnitMyPref(language)
        }
    }

    fun updateDailyGoalUnitMyPref(dailyGoal: Int) {
        executor.execute {
            locationDao.updateDailyGoalUnitMyPref(dailyGoal)
        }
    }

    fun updateReminderDaysMyPref(reminderDays: List<Int>) {
        executor.execute {
            locationDao.updateReminderDaysMyPref(reminderDays)
        }
    }

    fun updateReminderHourMyPref(reminderHour: Int) {
        executor.execute {
            locationDao.updateReminderHourMyPref(reminderHour)
        }
    }
    fun updateReminderMinutesMyPref(reminderMinutes: Int) {
        executor.execute {
            locationDao.updateReminderMinutesMyPref(reminderMinutes)
        }
    }

    fun updateReminderIsSyncMyPref(isSynced: Boolean) {
        executor.execute {
            locationDao.updateReminderIsSyncMyPref(isSynced)
        }
    }

    fun insertMyPref(myPref: MyPref) {
        executor.execute {
            locationDao.insertMyPref(myPref)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: IntroProfileRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): IntroProfileRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IntroProfileRepository(
                    MyLocationDatabase.getInstance(context),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}
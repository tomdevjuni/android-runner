package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.repo.HistoryRepository
import com.example.run_tracker_native_app.repo.IntroProfileRepository
import com.example.run_tracker_native_app.repo.ReportRepository
import java.util.concurrent.Executors

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val reportRepository = ReportRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )
    var myPrefLiveData: LiveData<MyPref> = reportRepository.getMyPref()

    private val introProfileRepository = IntroProfileRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    fun updateDistanceUnitMyPref(gender: String) {
        introProfileRepository.updateDistanceUnitMyPref(gender)
    }

    fun updateLanguageUnitMyPref(language: String) {
        introProfileRepository.updateLanguageUnitMyPref(language)
    }

    fun updateDailyGoalUnitMyPref(dailyGoal: Int) {
        introProfileRepository.updateDailyGoalUnitMyPref(dailyGoal)
    }

    fun updateReminderDaysMyPref(reminderDays: List<Int>) {
        introProfileRepository.updateReminderDaysMyPref(reminderDays)
    }
    fun updateReminderHourMyPref(hour: Int) {
        introProfileRepository.updateReminderHourMyPref(hour)
    }
    fun updateReminderMinutesMyPref(minutes: Int) {
        introProfileRepository.updateReminderMinutesMyPref(minutes)
    }
    fun updateReminderIsSyncMyPref(isSync: Boolean) {
        introProfileRepository.updateReminderIsSyncMyPref(isSync)
    }




    fun insertMyPref(myPref: MyPref) {
        introProfileRepository.insertMyPref(myPref)
    }

    private val historyRepository = HistoryRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )
    fun getAllRunningHistoryOnce() : List<MyRunningEntity>{
        return  historyRepository.getAllRunningHistoryOnce()
    }
    fun updateIsSyncMyHistory(isSynced: Boolean,idList: List<Int>) {
        historyRepository.updateIsSyncMyHistory(isSynced,idList)
    }
    fun hardDeleteHistoryByIds(idList: List<Int>) {
        historyRepository.hardDeleteHistoryByIds(idList)
    }

    fun insertAll(myRunningEntityData: List<MyRunningEntity>) {
        historyRepository.insertAll(myRunningEntityData)
    }

}

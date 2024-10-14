package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.repo.HistoryRepository
import java.util.concurrent.Executors

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    var runningListAllHistoryData: LiveData<List<MyRunningEntity>> = historyRepository.getAllRunningHistory()

     lateinit var runningHistoryData: LiveData<MyRunningEntity>

    var myPrefLiveData: LiveData<MyPref> = historyRepository.getMyPref()

    fun softDeleteHistoryByIds(idList: List<Int>) {
        historyRepository.softDeleteHistoryByIds(idList)
    }

    fun getHistoryByIds(id: Int) {
        runningHistoryData = historyRepository.getRunningHistoryById(id)
    }
}

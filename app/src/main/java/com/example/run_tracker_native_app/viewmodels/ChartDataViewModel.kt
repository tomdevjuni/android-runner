package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.repo.ReportRepository
import java.util.concurrent.Executors

class ChartDataViewModel(application: Application) : AndroidViewModel(application) {

    private val reportRepository = ReportRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )
    var myPrefLiveData: LiveData<MyPref> = reportRepository.getMyPref()

    lateinit var runningListAllHistoryData: LiveData<List<MyRunningEntity>>

    fun getHistoryFromDateToDate(fromDate: Long, toDate: Long) {
        runningListAllHistoryData =  reportRepository.getHistoryFromDateToDate(fromDate,toDate)
    }

}

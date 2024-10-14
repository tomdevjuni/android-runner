package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.repo.HomeRepository
import java.util.concurrent.Executors

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val homeRepository = HomeRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    var myPrefLiveData: LiveData<MyPref> = homeRepository.getMyPref()

    var myTotalDistanceAllTime: LiveData<Float> = homeRepository.getTotalDistanceAllTime()

    var myTotalAllTime: LiveData<Int> = homeRepository.getTotalAllTime()

    var myTotalCal: LiveData<Float> = homeRepository.getTotalCal()

    var myTotalAvgSpeed: LiveData<Float> = homeRepository.getTotalAvgSpeed()

}

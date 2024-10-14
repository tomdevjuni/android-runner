package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.repo.HomeRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executors

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val homeRepository = HomeRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )

    var runningListHistoryData: LiveData<List<MyRunningEntity>> = homeRepository.getRunningHistory()

    var myPrefLiveData: LiveData<MyPref> = homeRepository.getMyPref()

//    var myTotalDistanceToday: LiveData<Float> = homeRepository.getTotalDistanceToday(
//        LocalDate.of(
//        Calendar.getInstance().get(Calendar.YEAR),
//        Calendar.getInstance().get(Calendar.MONTH),
//        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).toEpochDay())

    var myTotalDistanceToday: LiveData<Float> = homeRepository.getTotalDistanceToday(LocalDate.now().atStartOfDay().atZone(
        ZoneId.systemDefault()).toInstant().toEpochMilli())

    var myTotalDistanceAllTime: LiveData<Float> = homeRepository.getTotalDistanceAllTime()

    var myTotalAllTime: LiveData<Int> = homeRepository.getTotalAllTime()

    var myTotalCal: LiveData<Float> = homeRepository.getTotalCal()

    var myTotalAvgSpeed: LiveData<Float> = homeRepository.getTotalAvgSpeed()

    var myLongestDistance: LiveData<Float> = homeRepository.getLongestDistance()

    var myTopSpeed: LiveData<Float> = homeRepository.getTopSpeed()

    var myLongestDuration: LiveData<Int> = homeRepository.getLongestDuration()


}

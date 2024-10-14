package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.repo.HomeRepository
import java.util.concurrent.Executors

class AchievementViewModel(application: Application) : AndroidViewModel(application) {

    private val homeRepository = HomeRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )
    lateinit var myTotalDistanceAllTime: LiveData<Float>
    var myPrefLiveData: LiveData<MyPref> = homeRepository.getMyPref()
    fun getMyTotalDistanceAllTime() {

        myTotalDistanceAllTime = homeRepository.getTotalDistanceAllTime()
    }

}

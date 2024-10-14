package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.repo.IntroProfileRepository
import java.util.concurrent.Executors

class IntroProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val introProfileRepository = IntroProfileRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )
    fun updateGenderMyPref(gender: String) {
        introProfileRepository.updateGenderMyPref(gender)
    }
    fun updateDistanceUnitMyPref(gender: String) {
        introProfileRepository.updateDistanceUnitMyPref(gender)
    }

    fun updateDailyGoalUnitMyPref(dailyGoal: Int) {
        introProfileRepository.updateDailyGoalUnitMyPref(dailyGoal)
    }

    fun insertMyPref(myPref: MyPref) {
        introProfileRepository.insertMyPref(myPref)
    }
}

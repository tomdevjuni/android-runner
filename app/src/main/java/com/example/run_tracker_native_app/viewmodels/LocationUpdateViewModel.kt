package com.example.run_tracker_native_app.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.repo.LocationRepository
import com.example.run_tracker_native_app.database.MyLocationEntity
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.util.concurrent.Executors

/**
 * Allows Fragment to observer {@link MyLocation} database, follow the state of location updates,
 * and start/stop receiving location updates.
 */
class LocationUpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository.getInstance(
        application.applicationContext,
        Executors.newSingleThreadExecutor()
    )
    var runningLiveData: LiveData<MyRunningEntity>? = null
    var locationListOldData: LiveData<List<MyLocationEntity>>? = null
    var locationListLiveData: LiveData<MyLocationEntity>? = null
    fun updateAvgSpeed(id: Int, speed: Float, bitmap: Bitmap?, points: String) {
        locationRepository.updateSpeedCurrantRunning(id, speed, bitmap, points)
    }

    var myPrefLiveData: LiveData<MyPref> = locationRepository.getMyPref()
    fun getCurrantRunningSession(id: Int) {
        runningLiveData = locationRepository.getCurrantRunningSession(id)

    }

    fun getLengthOfRunningData(): Int? {
        return locationRepository.getLengthOfRunningData()
    }
    fun getLastRunningData(): MyRunningEntity? {
        return locationRepository.getLastRunningData()
    }

    fun getLiveLocationData(id: Int) {

        locationListLiveData = locationRepository.getLocations(id)
    }

    fun getOldLocationDataOfCurrantSession(id: Int) {
        locationListOldData = locationRepository.getLocationsBySessionId(id)
    }
}

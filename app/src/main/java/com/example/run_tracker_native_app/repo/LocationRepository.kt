package com.example.run_tracker_native_app.repo

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.example.run_tracker_native_app.locationservice.LocationTrackerService
import com.example.run_tracker_native_app.database.helpers.MyLocationDatabase
import com.example.run_tracker_native_app.database.MyLocationEntity
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

private const val TAG = "LocationRepository"

/**
 * Access point for database (MyLocation data) and location APIs (start/stop location updates and
 * checking location update status).
 */
class LocationRepository private constructor(
    private val myLocationDatabase: MyLocationDatabase,
    private val myLocationTrackerService: LocationTrackerService,
    private val executor: ExecutorService
) {

    private val locationDao = myLocationDatabase.locationDao()

    fun getMyPref(): LiveData<MyPref> = locationDao.getMyPref()
    fun getLocations(id: Int): LiveData<MyLocationEntity> = locationDao.getLastLocation()

    fun getLocationsBySessionId(id: Int): LiveData<List<MyLocationEntity>> =
        locationDao.getLocationsBySessionId(id)

    fun getCurrantRunningSession(id: Int): LiveData<MyRunningEntity> =
        locationDao.getRunningData(id)

    fun getLengthOfRunningData(): Int? {
        val callable = Callable {locationDao.getLengthOfRunningData() }
        val future = executor.submit(callable)
        return future.get()
    }
    fun getLastRunningData(): MyRunningEntity? {
        val callable = Callable {locationDao.getLastRunningData() }
        val future = executor.submit(callable)
        return future.get()
    }
    fun getRunningDataById(id: Int): MyRunningEntity? {
        val callable = Callable {locationDao.getRunningDataById(id) }
        val future = executor.submit(callable)
        return future.get()
    }
    fun getLocation(id: UUID): LiveData<MyLocationEntity> = locationDao.getLocation(id)

    fun updateCurrantRunning(myRunningEntity: MyRunningEntity) {
        executor.execute {
            locationDao.updateMyRunningEntity(myRunningEntity)
        }
    }

    fun updateSpeedCurrantRunning(id: Int, speed: Float, bitmap: Bitmap?, points: String) {
        executor.execute {
            locationDao.updateSpeedInMyRunningEntity(id, speed, bitmap, points)
        }
    }

    fun addLocation(myLocationEntity: MyLocationEntity) {
        executor.execute {
            locationDao.addLocation(myLocationEntity)
        }
    }

    fun addLocations(myLocationEntities: List<MyLocationEntity>) {
        executor.execute {
            locationDao.addLocations(myLocationEntities)
        }
    }

    fun removeAllLocationsBySessionId(id: Int) {
        executor.execute {
            locationDao.removeAllLocationsBySessionId(id)
        }
    }

    fun removeCurrantSession(id: Int) {
        executor.execute {
            locationDao.removeCurrantSession(id)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LocationRepository? = null

        fun getInstance(context: Context, executor: ExecutorService): LocationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationRepository(
                    MyLocationDatabase.getInstance(context),
                    LocationTrackerService.getInstance(),
                    executor
                )
                    .also { INSTANCE = it }
            }
        }
    }
}

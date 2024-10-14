package com.example.run_tracker_native_app.database.helpers

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.run_tracker_native_app.database.MyLocationEntity
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.util.UUID

@Dao
interface MyLocationDao {

    @Query("SELECT * FROM my_location_table WHERE currantSession=(:id) ORDER BY id ASC")
    fun getLocationsBySessionId(id: Int): LiveData<List<MyLocationEntity>>

    @Query("SELECT * FROM my_running_table WHERE isDeleted = 0 AND pointsString NOT null ORDER BY id DESC LIMIT 3")
    fun getRunningHistory(): LiveData<List<MyRunningEntity>>

    @Query("SELECT * FROM my_pref WHERE id=1")
    fun getMyPref(): LiveData<MyPref>

    @Query("SELECT * FROM my_pref WHERE id=1")
    fun getMyPrefForOnce(): MyPref
    @Query("SELECT * FROM my_running_table WHERE isDeleted = 0 AND pointsString NOT null ORDER BY id DESC")
    fun getAllRunningHistory(): LiveData<List<MyRunningEntity>>

    @Query("SELECT * FROM my_running_table WHERE isSynchronized != 1")
    fun getAllRunningHistoryOnce(): List<MyRunningEntity>

    @Query("SELECT * FROM my_running_table WHERE date >= :fromDate AND date <= :toDate  ORDER BY id ASC")
    fun getHistoryFromDateToDate(fromDate: Long, toDate: Long): LiveData<List<MyRunningEntity>>

    @Query("SELECT * FROM my_running_table WHERE id = (:id)")
    fun getRunningHistoryById(id: Int): LiveData<MyRunningEntity>

    @Query("DELETE FROM my_location_table WHERE currantSession=(:id)")
    fun removeAllLocationsBySessionId(id: Int)

    @Query("SELECT * FROM my_location_table WHERE id=(:id)")
    fun getLocation(id: UUID): LiveData<MyLocationEntity>
    @Query("SELECT * FROM my_location_table ORDER BY id DESC LIMIT 1")
    fun getLastLocation(): LiveData<MyLocationEntity>

    @Query("SELECT id FROM my_running_table ORDER BY id DESC LIMIT 1")
    fun getLengthOfRunningData(): Int?
    @Query("SELECT * FROM my_running_table ORDER BY id DESC LIMIT 1")
    fun getLastRunningData(): MyRunningEntity?

    @Query("SELECT * FROM my_running_table WHERE id =(:id) ")
    fun getRunningDataById(id: Int): MyRunningEntity?

    @Query("SELECT * FROM my_running_table WHERE id=(:id)")
    fun getRunningData(id: Int): LiveData<MyRunningEntity>

    @Query("DELETE FROM my_running_table WHERE id=(:id)")
    fun removeCurrantSession(id: Int)
    @Update
    fun updateLocation(myLocationEntity: MyLocationEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateMyRunningEntity(myRunningEntity: MyRunningEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyPref(myPref: MyPref)

    @Query("UPDATE my_pref SET gender = (:gender) WHERE id = 1")
    fun updateGenderMyPref(gender: String)
    @Query("SELECT gender FROM my_pref WHERE id = 1")
    fun getGenderMyPref() : String

    @Query("UPDATE my_pref SET distanceUnit = (:distanceUnit) WHERE id = 1")
    fun updateDistanceUnitMyPref(distanceUnit: String)

    @Query("UPDATE my_pref SET language = (:language) WHERE id = 1")
    fun updateLanguageUnitMyPref(language: String)

    @Query("UPDATE my_pref SET dailyGoal = (:dailyGoal) WHERE id = 1")
    fun updateDailyGoalUnitMyPref(dailyGoal: Int)

    @Query("UPDATE my_pref SET reminderDays = :reminderDays WHERE id = 1")
    fun updateReminderDaysMyPref(reminderDays: List<Int>)
    @Query("UPDATE my_pref SET reminderTimeHour = (:reminderHour) WHERE id = 1")
    fun updateReminderHourMyPref(reminderHour: Int)
    @Query("UPDATE my_pref SET reminderTimeMinute = (:reminderMinute) WHERE id = 1")
    fun updateReminderMinutesMyPref(reminderMinute: Int)

    @Query("UPDATE my_pref SET isSynchronized = (:isSynced) WHERE id = 1")
    fun updateReminderIsSyncMyPref(isSynced: Boolean)

    @Query("UPDATE my_running_table SET isSynchronized = (:isSynced) WHERE id in (:idList)")
    fun updateIsSyncMyHistory(isSynced: Boolean,idList: List<Int>)

    @Query("UPDATE my_running_table SET avgSpeed = (:speed),image = (:bitmap),pointsString = (:points) WHERE id = (:id)")
    fun updateSpeedInMyRunningEntity(id: Int, speed: Float, bitmap: Bitmap?, points : String)

    @Insert
    fun addLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<MyLocationEntity>)
    @Query("UPDATE my_running_table SET isDeleted = 1, isSynchronized = 0  WHERE id in (:idList)")
    fun softDeleteHistoryByIds(idList: List<Int>)

    @Query("DELETE FROM my_running_table  WHERE id in (:idList)")
    fun hardDeleteHistoryByIds(idList: List<Int>)


    @Query("DELETE FROM my_location_table WHERE currantSession in (:idList)")
    fun deleteLocationsBySessionIds(idList: List<Int>)

    @Query("SELECT SUM(distance) FROM my_running_table WHERE date >= :date")
    fun getTotalDistanceToday(date: Long): LiveData<Float>

    @Query("SELECT SUM(distance) FROM my_running_table")
    fun getTotalDistanceAllTime(): LiveData<Float>

    @Query("SELECT SUM(timeInSeconds) FROM my_running_table")
    fun getTotalAllTime(): LiveData<Int>

    @Query("SELECT SUM(kcal) FROM my_running_table")
    fun getTotalCal(): LiveData<Float>

    @Query("SELECT AVG(avgSpeed) FROM my_running_table")
    fun getTotalAvgSpeed(): LiveData<Float>
    @Query("SELECT Max(distance) FROM my_running_table")
    fun getLongestDistance(): LiveData<Float>

    @Query("SELECT Max(avgSpeed) FROM my_running_table")
    fun getTopSpeed(): LiveData<Float>

    @Query("SELECT Max(timeInSeconds) FROM my_running_table")
    fun getLongestDuration(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(myRunningEntities: List<MyRunningEntity>)
}
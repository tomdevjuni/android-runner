package com.example.run_tracker_native_app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.repo.HomeRepository
import java.util.*
import java.util.concurrent.Executors

class BootReceiver : BroadcastReceiver() {

    private val workRequest = OneTimeWorkRequestBuilder<MyWorkerBoot>()
        .build()

    override fun onReceive(context: Context, intent: Intent) {
        Log.e("TAG", "onReceive:inside:::: ")
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

}

class MyWorkerBoot(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val homeRepository = HomeRepository.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )
    private var mCalendar: Calendar? = null
    private var mAlarmReceiver: AlarmReceiver? = null
    override suspend fun doWork(): Result {
        mCalendar = Calendar.getInstance()
        mAlarmReceiver = AlarmReceiver()
        val data = homeRepository.getMyPref()
        val myPref: MyPref = data.value!!
        if (myPref.reminderTimeHour != null && myPref.reminderTimeMinute != null) {

            mCalendar!!.set(Calendar.HOUR_OF_DAY, myPref.reminderTimeHour)
            mCalendar!!.set(Calendar.MINUTE, myPref.reminderTimeMinute)
            mCalendar!!.set(Calendar.SECOND, 0)


            // Create a new notification
            mAlarmReceiver!!.setAlarm(this.applicationContext, mCalendar!!, 1)
        }
        // Do something with the data (e.g., update UI, send notifications)
        return Result.success()
    }


}
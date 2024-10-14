package com.example.run_tracker_native_app.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.activity.MainActivity
import com.example.run_tracker_native_app.repo.HomeRepository
import com.example.run_tracker_native_app.utils.Constant
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class AlarmReceiver : BroadcastReceiver() {

    private var mAlarmManager: AlarmManager? = null
    private var mPendingIntent: PendingIntent? = null
    private val workRequest = OneTimeWorkRequestBuilder<MyWorker>()
        .build()

    override fun onReceive(context: Context, intent: Intent) {
        Log.e("TAG", "onReceive:Start ServiceL:::: ")
        val id = intent.getStringExtra(Constant.EXTRA_REMINDER_ID)
        Log.e("TAG", "onReceive:Start ServiceL:::: $id")

        try {
            WorkManager.getInstance(context).enqueue(workRequest)

//            val data = Util.getPref(context, Constant.PREFERENCE_SELECTED_REMINDER, "")
//            val type: Type = object : TypeToken<ReminderTableClass>() {}.type
//            reminderClass = Gson().fromJson(data, type)
////            reminderClass = dataBaseHelper.getReminderById(id!!)
//            if (reminderClass.isActive == "true") {
//
//                var arrOfDays = ArrayList<String>()
//                if (reminderClass.days.contains(",")) {
//                    arrOfDays = (reminderClass.days.split(",")) as ArrayList<String>
//                } else {
//                    arrOfDays.add(reminderClass.days)
//                }
//
//                for (i in 0 until arrOfDays.size) {
//                    arrOfDays[i] = arrOfDays[i].replace("'", "")
//                    Log.e("TAG", "onReceive:Array days:::::  " + arrOfDays[i])
//                }
//
//                val dayNumber = getDayNumber(getCurrentDayName().uppercase(Locale.ROOT))
//                Log.e("TAG", "onReceive::::Day Number::::: $dayNumber")
//                if (arrOfDays.contains(dayNumber)) {
//                    fireNotification(context, reminderClass)
//                }
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(context: Context, calendar: Calendar, id: Int) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        intent.putExtra(Constant.EXTRA_REMINDER_ID, id.toString())
        mPendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_MUTABLE)

//        val c = Calendar.getInstance()
//        val currentTime = c.timeInMillis
//        val diffTime = calendar.timeInMillis - currentTime

        mAlarmManager!!.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            mPendingIntent!!
        )


        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        Log.e("TAG", "setAlarm::::==>> " + id + "  " + calendar.timeInMillis)

    }

    fun setRepeatAlarm(context: Context, calendar: Calendar, id: Int, repeatTime: Long) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentId = System.currentTimeMillis().toInt()
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        intent.putExtra(Constant.EXTRA_REMINDER_ID, id.toString())
        mPendingIntent =
            PendingIntent.getBroadcast(context, intentId, intent, PendingIntent.FLAG_MUTABLE)

        val c = Calendar.getInstance()
        val currentTime = c.timeInMillis
        val diffTime = calendar.timeInMillis - currentTime
        mAlarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + diffTime,
            repeatTime, mPendingIntent!!
        )

        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm(context: Context, id: Int) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel Alarm using Reminder ID
        mPendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )
        mAlarmManager!!.cancel(mPendingIntent!!)

        // Disable alarm
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }


    @SuppressLint("WrongConstant")


    private fun getDate(date: String): Date? {
        val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        return simpleDateFormat.parse(date)
    }

    private fun isDateBetweenStartEndDate(max: Date, date: Date): Boolean {
        var isDateBetweenToDate = false
        val currentDate = getCurrentDate()
        val maxDate = getEndDate(max)

        if (currentDate == maxDate) {
            isDateBetweenToDate = true
        } else if (date <= max) {
            isDateBetweenToDate = true
        }
        return isDateBetweenToDate

    }


    private fun getCurrentDayName(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val sdf = SimpleDateFormat("EEE", Locale.US)
        return sdf.format(cal.time)
    }

    private fun getCurrentDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        return sdf.format(cal.time)
    }

    private fun getEndDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        return sdf.format(date)
    }

    private fun getDayNumber(dayName: String): String {
        var dayNumber = ""
        when (dayName) {
            "SUN" -> dayNumber = "1"
            "MON" -> dayNumber = "2"
            "TUE" -> dayNumber = "3"
            "WED" -> dayNumber = "4"
            "THU" -> dayNumber = "5"
            "FRI" -> dayNumber = "6"
            "SAT" -> dayNumber = "7"
        }
        return dayNumber
    }
}

class MyWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val homeRepository = HomeRepository.getInstance(
        context,
        Executors.newSingleThreadExecutor()
    )

    override suspend fun doWork(): Result {
        val myPref = homeRepository.getMyPrefForOnce()

        if (myPref.reminderDays.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (myPref.reminderDays.contains(dayOfWeek)) {
                fireNotification(this.applicationContext)
            }
        }
        val mCalendarCurrent: Calendar = Calendar.getInstance()


        val mDay = mCalendarCurrent.get(Calendar.DATE).plus(1)

        mCalendarCurrent[Calendar.DAY_OF_MONTH] = mDay
        mCalendarCurrent[Calendar.HOUR_OF_DAY] = myPref.reminderTimeHour!!
        mCalendarCurrent[Calendar.MINUTE] = myPref.reminderTimeMinute!!
        mCalendarCurrent[Calendar.SECOND] = 0
        AlarmReceiver().setAlarm(this.applicationContext, mCalendarCurrent, 1)

        return Result.success()
    }

    private fun fireNotification(context: Context) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "reminder"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channelName = context.resources.getString(R.string.app_name)
        val channelDescription = "Application_name Alert"
        val mChannel = NotificationChannel(channelId, channelName, importance)
        mChannel.description = channelDescription
        mChannel.enableVibration(true)
        notificationManager.createNotificationChannel(mChannel)

        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.drawable.ic_notifications_active)
        builder.color = ContextCompat.getColor(context, R.color.theme)

        builder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText("Your body needs energy! You haven't exercised in ${getCurrentFullDayName()}!")
        )
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        builder.setContentTitle(context.resources.getString(R.string.app_name))
        builder.setAutoCancel(false)
        builder.setOngoing(false)

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent =
            PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(intent)

        notificationManager.notify(1, builder.build())

    }

    private fun getCurrentFullDayName(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val sdf = SimpleDateFormat("EEEE", Locale.US)
        return sdf.format(cal.time)
    }
}
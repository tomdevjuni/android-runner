package com.example.run_tracker_native_app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.LocaleList
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.dataclass.AllWeekData
import com.example.run_tracker_native_app.dataclass.WeekDatNameData
import com.example.run_tracker_native_app.fragments.CurrentMonthYear
import com.example.run_tracker_native_app.fragments.SettingFragment
import com.example.run_tracker_native_app.interfaces.CallbackListener
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt


object Util {
    /**
     * @return milliseconds since 1.1.1970 for today 0:00:00 local timezone
     */
    val today: Long
        get() {
            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()
            c[Calendar.HOUR_OF_DAY] = 0
            c[Calendar.MINUTE] = 0
            c[Calendar.SECOND] = 0
            c[Calendar.MILLISECOND] = 0
            return c.timeInMillis
        }

    /**
     * @return milliseconds since 1.1.1970 for tomorrow 0:00:01 local timezone
     */
    val tomorrow: Long
        get() {
            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()
            c[Calendar.HOUR_OF_DAY] = 0
            c[Calendar.MINUTE] = 0
            c[Calendar.SECOND] = 1
            c[Calendar.MILLISECOND] = 0
            c.add(Calendar.DATE, 1)
            return c.timeInMillis
        }


    fun checkRequiredPermission(context: Context): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val result2 = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val result4 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            )

            } else {
            PackageManager.PERMISSION_GRANTED
        }
        val result3 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }


        return result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED &&
                result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED
    }
    fun checkRequiredBackGroundPermission(context: Context): Boolean {
        val result1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }


        return result1 == PackageManager.PERMISSION_GRANTED
    }

    fun checkRequiredAlarmPermission(context: Context): Boolean {
        val result1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.SCHEDULE_EXACT_ALARM
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }


        return result1 == PackageManager.PERMISSION_GRANTED
    }

    fun requestRequiredPermission(appCompatActivity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                appCompatActivity,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACTIVITY_RECOGNITION,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ),
                Constant.RequestCodePermission
            )
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                appCompatActivity,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACTIVITY_RECOGNITION,
                ),
                Constant.RequestCodePermission
            )
        }else {
            ActivityCompat.requestPermissions(
                appCompatActivity,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                Constant.RequestCodePermission
            )
        }

    }
    fun requestRequiredBackgroundPermission(appCompatActivity: AppCompatActivity) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                appCompatActivity,
                arrayOf(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                Constant.RequestCodeBackgroundPermission
            )
        }

    }

    fun requestRequiredAlarmPermission(appCompatActivity: Context) {

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             val manager = ContextCompat.getSystemService(appCompatActivity, AlarmManager::class.java)
             if (manager?.canScheduleExactAlarms() == false) {
                 Intent().apply {
                     action = ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                 }.also {
                     startActivity(appCompatActivity,it,null)
                 }
             } else {
                 Log.d("MainActivity", "onCreate: can schedule it")
             }
//            ActivityCompat.requestPermissions(
//                appCompatActivity,
//                arrayOf(
//                    android.Manifest.permission.SCHEDULE_EXACT_ALARM
//                ),
//                Constant.RequestCodeAlarmPermission
//            )
        }

    }

    fun getDateFromInt(milliSeconds: Long): String? {
        Log.e("TAG", "getDateFromInt:::Date==>>  $milliSeconds")
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd-MMM-yyyy", Locale.US)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun setPref(c: Context, pref: String, value: String) {
        val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
        e.putString(pref, value)
        e.apply()

    }

    fun getPref(c: Context, pref: String, value: String): String? {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(
            pref,
            value
        )
    }

    fun setPref(c: Context, pref: String, value: Float) {
        val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
        e.putFloat(pref, value)
        e.apply()

    }

    fun getPref(c: Context, pref: String, value: Float): Float {
        return PreferenceManager.getDefaultSharedPreferences(c).getFloat(
            pref,
            value
        )
    }

    fun setPref(c: Context, pref: String, value: Boolean) {
        val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
        e.putBoolean(pref, value)
        e.apply()

    }

    fun getPref(c: Context, pref: String, value: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(
            pref, value
        )
    }

    fun delPref(c: Context, pref: String) {
        val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
        e.remove(pref)
        e.apply()
    }

    fun setPref(c: Context, pref: String, value: Int) {
        val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
        e.putInt(pref, value)
        e.apply()

    }

    fun getPref(c: Context, pref: String, value: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(
            pref,
            value
        )
    }

    fun setPref(c: Context, pref: String, value: Long) {
        val e = PreferenceManager.getDefaultSharedPreferences(c).edit()
        e.putLong(pref, value)
        e.apply()
    }

    fun getPref(c: Context, pref: String, value: Long): Long {
        return PreferenceManager.getDefaultSharedPreferences(c).getLong(
            pref,
            value
        )
    }

    fun setPref(c: Context, file: String, pref: String, value: String) {
        val settings = c.getSharedPreferences(
            file,
            Context.MODE_PRIVATE
        )
        val e = settings.edit()
        e.putString(pref, value)
        e.apply()
    }

    fun getPref(c: Context, file: String, pref: String, value: String): String? {
        return c.getSharedPreferences(file, Context.MODE_PRIVATE).getString(
            pref, value
        )
    }

    fun getDailyGoals(context: Context): Int {
        return getPref(context, Constant.KEY_SET_GOAL, 8000)
    }

    fun setDailyGoals(c: Context, steps: Int) {
        setPref(c, Constant.KEY_SET_GOAL, steps)
    }


    fun getCurrentWeek(): ArrayList<WeekDatNameData> {
        val data = ArrayList<WeekDatNameData>()
        val formatterDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val formatterDay = SimpleDateFormat("EEE", Locale.US)
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
        for (i in 0..6) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            data.add(
                WeekDatNameData(
                    formatterDate.format(calendar.time),
                    formatterDay.format(calendar.time)
                )
            )
            Log.e("TAG", "getCurrentWeek::::Dates==>>  ${data[i].strDate}  ${data[i].strDay}")
        }
        return data
    }


    fun getWeekDates(currentWeek: Int): ArrayList<AllWeekData?> {
        val data = ArrayList<AllWeekData?>()
        val strDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val fullDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val month = SimpleDateFormat("MMMM - yyyy", Locale.US)
        val day = SimpleDateFormat("dd", Locale.US)

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.WEEK_OF_YEAR, currentWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        for (i in 0..6) {
            data.add(
                AllWeekData(
                    fullDate.format(calendar.time),
                    month.format(calendar.time),
                    day.format(calendar.time),
                    strDateFormat.format(calendar.time),
                    calendar.time
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return data
    }

    fun getAllDatesMonthWise(year: Int, month: Int): ArrayList<AllWeekData?> {
        val data = ArrayList<AllWeekData?>()
        val strDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val monthFormat = SimpleDateFormat("MMMM - yyyy", Locale.US)
        val dayFormat = SimpleDateFormat("dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.clear()
        cal[year, month - 1] = 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (i in 0 until daysInMonth) {
            data.add(
                AllWeekData(
                    fmt.format(cal.time),
                    monthFormat.format(cal.time),
                    dayFormat.format(cal.time),
                    strDateFormat.format(cal.time),
                    cal.time
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return data
    }

    fun getWeekDatesForChartMonth(
        currentWeek: Int,
        strYear: String,
        strMonth: String,
    ): ArrayList<AllWeekData?> {
        val data = ArrayList<AllWeekData?>()
        val strDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val fullDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val month = SimpleDateFormat("MMMM - yyyy", Locale.US)
        val day = SimpleDateFormat("dd", Locale.US)
        val format = SimpleDateFormat("yyyy-MM", Locale.US)

        val dateFullDate: Date =
            format.parse("$strYear-${convertStrMonthToIntMonth(strMonth)}")!!

        val calendar = Calendar.getInstance()
        calendar.time = dateFullDate
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.WEEK_OF_MONTH, currentWeek)
        for (i in 0..6) {
            data.add(
                AllWeekData(
                    fullDate.format(calendar.time),
                    month.format(calendar.time),
                    day.format(calendar.time),
                    strDateFormat.format(calendar.time),calendar.time
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return data
    }

    fun getCurrentDateWithFullFormat(): String {
        val c = Calendar.getInstance().time

        /*19-Apr-2022  Format Date Value*/
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        Log.e("TAG", "getCurrentDateWithFullFormat===> $c  ${df.format(c)}")
        return df.format(c)
    }

    fun getCurrentDateCustomFormat(format: String): String {
        val c = Calendar.getInstance().time

        /*19-Apr-2022  Format Date Value*/
        val df = SimpleDateFormat(format, Locale.US)
        Log.e("TAG", "getCurrentDateCustomFormat===> $c  ${df.format(c)}")
        return df.format(c)
    }


    fun getWeekMonthsForChartMonth(
        currentWeek: Int,
        strYear: String,
        strMonth: String,
    ): ArrayList<String?> {
        val data = ArrayList<String?>()
        val month = SimpleDateFormat("MMMM", Locale.US)
        val format = SimpleDateFormat("yyyy-MM", Locale.US)

        val dateFullDate: Date =
            format.parse("$strYear-${convertStrMonthToIntMonth(strMonth)}")!!


        val calendar = Calendar.getInstance()
        calendar.time = dateFullDate
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.WEEK_OF_MONTH, currentWeek)
        for (i in 0..6) {
            data.add(month.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return data
    }


    @SuppressLint("SimpleDateFormat")
    fun getTotalNumberOfWeeks(strYear: String, strMonth: String): Int {
        val format = SimpleDateFormat("yyyy-MM", Locale.US)
        val date: Date = format.parse("$strYear-${convertStrMonthToIntMonth(strMonth)}")!!
        val c = Calendar.getInstance()
        c.time = date
        val start = c[Calendar.WEEK_OF_MONTH]
        c.add(Calendar.MONTH, 1)
        c.add(Calendar.DATE, -1)
        val end = c[Calendar.WEEK_OF_MONTH]
        println(" # of weeks in " + format.format(c.time).toString() + ": " + (end - start + 1))
        return (end - start + 1)
    }

    fun getLastAndFirstDateOfMonthAndYear(strYear: String, strMonth: String) {
        Log.e("TAG", "getLastAndFirstDateOfMonthAndYear::: $strMonth  $strYear")
    }

    private fun getStartTimestamp(month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
//        calendar.time = Date()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month - 1
        calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.timeInMillis
    }

    private fun getEndTimestamp(month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
//        calendar.time = Date()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month - 1
        calendar[Calendar.DAY_OF_MONTH] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.timeInMillis
    }

    fun getTimes(month: Int, year: Int, isFirstDate: Boolean): String {
        val start = getStartTimestamp(month, year)
        val end = getEndTimestamp(month, year)
        println("Start date is: " + Date(start) + "  " + month + "  " + year)
        println("End date is: " + Date(end) + "  " + month + "  " + year)
        return if (isFirstDate) {
            DateFormat.format(Constant.formatFull, start).toString()
        } else {
            DateFormat.format(Constant.formatFull, end).toString()
        }

    }

    fun getCurrentDate(): String {
        val c = Calendar.getInstance().time

        val df = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        Log.e("TAG", "Current time Date===> $c  ${df.format(c)}")
        return df.format(c)
    }

    fun convertToStrMonthDate(strDate: String): String {
        val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val targetFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }


    fun convertToOriginal(strDate: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }

    private fun convertStrMonthToIntMonth(strDate: String): String {
        val originalFormat = SimpleDateFormat("MMMM", Locale.US)
        val targetFormat = SimpleDateFormat("MM", Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }

    fun convertLongToShortDate(strDate: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        val targetFormat = SimpleDateFormat("MMM-dd", Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }


    fun convertDateToDateMonthName(strDate: String): String {
        val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val targetFormat = SimpleDateFormat("EEEE, MMM dd", Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }


    fun getCurrentTime(): String {
        val c = Calendar.getInstance().time

        /*19-Apr-2022  Format Date Value*/
        val df = SimpleDateFormat("hh a", Locale.US)
        Log.e("TAG", "getCurrentTime===>  ${df.format(c)}")
        return df.format(c).lowercase(Locale.getDefault())
    }


    fun getCurrentMonthAndYear(): String {
        val c = Calendar.getInstance().time

        val df = SimpleDateFormat("MMMM", Locale.US)
        return df.format(c)
    }

    fun getAllTimeArray(): ArrayList<String> {
        val data = ArrayList<String>()
        data.add("01 am")
        data.add("02 am")
        data.add("03 am")
        data.add("04 am")
        data.add("05 am")
        data.add("06 am")
        data.add("07 am")
        data.add("08 am")
        data.add("09 am")
        data.add("10 am")
        data.add("11 am")
        data.add("12 pm")
        data.add("01 pm")
        data.add("02 pm")
        data.add("03 pm")
        data.add("04 pm")
        data.add("05 pm")
        data.add("06 pm")
        data.add("07 pm")
        data.add("08 pm")
        data.add("09 pm")
        data.add("10 pm")
        data.add("11 pm")
        data.add("12 am")
        return data
    }


    fun getSpecificWeekFromDate(currentDate: String, isFullDate: Boolean): ArrayList<String> {
        var dates = ArrayList<String>()
        val format: SimpleDateFormat = if (isFullDate) {
//            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.US)
        }
        try {
            val date = format.parse(currentDate)
            val c = Calendar.getInstance()
            c.time = date!!
            val dayOfWeek = c[Calendar.DAY_OF_WEEK]

            //Add certain number of days to Today

            val cAdd = Calendar.getInstance()
            cAdd.time = date
            val addInt: Int = if (dayOfWeek == 1) {
                -6
            } else {
                1
            }
            cAdd.add(Calendar.DATE, (7 - dayOfWeek) + addInt) // number of days to add


//Subtract certain number of days to Today
            val cSubtract = Calendar.getInstance()
            cSubtract.time = date

            val subtractInt: Int = if (dayOfWeek == 1) {
                -5
            } else {
                2
            }
            cSubtract.add(
                Calendar.DATE,
                (dayOfWeek * -1) + subtractInt
            ) // number of days to subtract

            val startDate = format.format(cSubtract.time)
            val endDate = format.format(cAdd.time)
            Log.e(
                "TAG",
                "getSpecificWeekFromDate:dayOfWeek==>>  $dayOfWeek   Start Date : ${
                    format.format(cSubtract.time)
                }  End Date : ${format.format(cAdd.time)}    ${(dayOfWeek * -1) + 2}"
            )

            dates = getAllDatesBetweenDates(startDate, endDate, isFullDate)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return dates

    }

    private fun getAllDatesBetweenDates(
        startDate: String,
        endDate: String,
        isFullDate: Boolean,
    ): ArrayList<String> {
        val dates = ArrayList<String>()
        val df1: SimpleDateFormat = if (isFullDate) {
//            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.US)
        }
        var date1: Date? = null
        var date2: Date? = null
        try {
            date1 = df1.parse(startDate)
            date2 = df1.parse(endDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1!!
        val cal2 = Calendar.getInstance()
        cal2.time = date2!!
        while (!cal1.after(cal2)) {
            dates.add(df1.format(cal1.time))
            cal1.add(Calendar.DATE, 1)
        }
        return dates
    }

    fun getCurrentWeekNumberFromDate(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.WEEK_OF_YEAR)
    }

    //function to determine the distance run in kilometers using average step length for men and number of steps
    private fun getDistanceRun(steps: Long): String {
        return try {
            DecimalFormat(
                "##.##",
                DecimalFormatSymbols(Locale.US)
            ).format((steps * 78).toFloat() / 100000.toFloat())
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun formatHoursAndMinutes(totalMinutes: Int): String {
        var minutes = (totalMinutes % 60).toString()
        minutes = if (minutes.length == 1) "0$minutes" else minutes
        return (totalMinutes / 60).toString() + ":" + minutes
    }

    fun getTimeFromStep(steps: Int, isWithText: Boolean = false): String {
        /*Formula = steps * second/per second step*/
        return convertSecondsToHHMM(
            (steps * 1 / 1.66).toInt(),
            isWithText
        )/* 1.66 Step = 1 Sec  And  0.0166667 Min = 1 Sec*/
    }


    private fun convertSecondsToHHMM(timeInSeconds: Int, isWithText: Boolean = false): String {
        val secondsLeft = timeInSeconds % 3600 % 60
        val minutes = floor((timeInSeconds % 3600 / 60).toDouble()).toInt()
        val hours = floor((timeInSeconds / 3600).toDouble()).toInt()
        val hh = (if (hours < 10) "0" else "") + hours
        val mm = (if (minutes < 10) "0" else "") + minutes
        return if (isWithText) {
            "$hh h $mm m"
        } else {
            "$hh:$mm"
        }
    }

    fun convertToCustomFormat(strDate: String, format: String): String {
        val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val targetFormat = SimpleDateFormat(format, Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }

    private fun convertToCustomFormat(
        strDate: String,
        receivedFormat: String,
        sendFormat: String
    ): String {
        val originalFormat = SimpleDateFormat(sendFormat, Locale.US)
        val targetFormat = SimpleDateFormat(receivedFormat, Locale.US)
        val date = originalFormat.parse(strDate)
        return targetFormat.format(date!!)
    }

    fun getCaloriesFromStep(step: Int): Double {
        return DecimalFormat("##.##", DecimalFormatSymbols(Locale.US)).format(step * 0.04)
            .toDouble()

    }

    fun getMinFromTime(strTime: String): Double {
        val units = strTime.split(":").toTypedArray()
        val minutes = units[0].toInt()
        val seconds = units[1].toInt()
        val totalSec = (60 * (minutes + seconds))
        return DecimalFormat("##.##", DecimalFormatSymbols(Locale.US)).format(totalSec / 60)
            .toDouble()
    }

    fun getStartEndDate(todayDate: String, hours: String): String {
        return "${
            convertToCustomFormat(
                todayDate,
                Constant.formatYYYYMMDD,
                Constant.formatDDMMYYYY
            )
        } $hours:00:00"
    }

    fun getSecondFromTime(time: String): Double {
        return if (time != "0") {
            val units = time.split(":").toTypedArray()
            val hours = units[0].toInt()
            val minutes = units[1].toInt()
            ((hours * 60 * 60) + (minutes * 60)).toDouble()
        } else {
            0.0
        }
    }

    fun convertDate(strDate: String): String {
        var str = ""
        val hour = String.format("%02d", strDate.split(":")[0].toInt())
        val min = String.format("%02d", strDate.split(":")[1].toInt())
        val amPm = strDate.split(":")[2]
        str = "$hour:$min ${amPm.uppercase(Locale.getDefault())}"
        return str
    }

    fun getDifferenceBetweenTime(startTime: String, lastTime: String): String {
        val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.US)

        val date1 = simpleDateFormat.parse(startTime)
        val date2 = simpleDateFormat.parse(lastTime)
        val calendarDate2 = Calendar.getInstance()
        val calendarDate1 = Calendar.getInstance()
        calendarDate1.time = date1!!
        calendarDate2.time = date2!!
        calendarDate2.add(Calendar.HOUR, 24)
        val difference: Long = calendarDate2.time.time - calendarDate1.time.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        var hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60))
        val min = (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours) / (1000 * 60)
        hours = if (hours < 0) -hours else hours
        Log.i("======= Hours", " getDifferenceBetweenTime:: $hours  $min")
        return "$hours.$min"
    }

    fun convertThousandToK(number: Int): String {
        var numberString = ""
        numberString = if (abs(number / 1000000) > 1) {
            (number / 1000000).toString() + "m"
        } else if (abs(number / 1000) > 1) {
            (number / 1000).toString() + "k"
        } else {
            number.toString()
        }
        return numberString
    }

    fun convertStringToDate(strDate: String): Date {
        val format = SimpleDateFormat(Constant.formatFull, Locale.US)
        var date = Date()
        try {
            date = format.parse(strDate)!!
            println(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return date
    }

    fun showToast(context: Context, strMessage: String) {
        Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show()
    }

    fun createViewBitmap(
        view: View,
        context: Context,
        isHideView: Boolean,
        shareType: String,
    ): Bitmap {
        view.isDrawingCacheEnabled = true
        if (isHideView) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
        }
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        view.buildDrawingCache(true)
//        val bitmap: Bitmap = view.drawingCache
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        var f: File? = null
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val folder =
                File(context.cacheDir.toString() + "" + File.separator.toString() + "My Temp Files")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            f = File(folder.path, "/filename" + ".png")
        }
        val ostream = FileOutputStream(f)
        bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream)
        ostream.close()
        val imageUri = FileProvider.getUriForFile(
            context,
            context.packageName.toString() + ".provider",
            f!!
        )
        val emailIntent1 = Intent(Intent.ACTION_SEND)
        emailIntent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        emailIntent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        emailIntent1.putExtra(Intent.EXTRA_STREAM, imageUri)
        emailIntent1.type = "image/png"

        when (shareType) {
            Constant.SHARE_WP -> {
                emailIntent1.setPackage("com.whatsapp")
            }

            Constant.SHARE_FB -> {
                emailIntent1.setPackage("com.facebook.katana")
            }

            Constant.SHARE_INSTA -> {
                emailIntent1.setPackage("com.instagram.android")
            }
        }
        context.startActivity(Intent.createChooser(emailIntent1, "Send Using"))

        return bitmap
    }

    @Throws(NumberFormatException::class)
    fun convertCmToFeetInches(str: Double, isFeet: Boolean): String {
        val value: Double = str
        val feet = floor(value / 30.48).toInt()
        val inches = (value / 2.54 - feet * 12).roundToInt()
        Log.e("TAG", "convertCmToFeetInches:::Feet Inch $feet  $inches")
        return if (isFeet) {
            feet.toString()
        } else {
            inches.toString()
        }

    }

    fun convertFeetAndInchesToCentimeter(feet: String?, inches: String?): Double {
        var heightInFeet = 0.0
        var heightInInches = 0.0
        try {
            if (feet != null && feet.trim { it <= ' ' }.isNotEmpty()) {
                heightInFeet = feet.toDouble()
            }
            if (inches != null && inches.trim { it <= ' ' }.isNotEmpty()) {
                heightInInches = inches.toDouble()
            }
        } catch (nfe: java.lang.NumberFormatException) {
            nfe.printStackTrace()
        }
        return heightInFeet * 30.48 + heightInInches * 2.54
    }

    fun convertKgAndLbs(currentValue: Int, isKg: Boolean): Int {
        val tempData: Int
        val change = 2.2046226218
        tempData = if (isKg) {
            (currentValue / change).roundToInt()
        } else {
            (currentValue * change).roundToInt()
        }
        return tempData
    }

    fun parseTimeString(time: Long, pattern: String): String {
        val sdf = SimpleDateFormat(
            pattern,
            Locale.US
        )
        return sdf.format(Date(time))
    }

    fun parseTimeStringLong(time: String, pattern: String): Date {
        val sdf = SimpleDateFormat(
            pattern,
            Locale.US
        )
        try {
            return sdf.parse(time)!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return Date()
    }

    fun getShortDayName(dayNo: Int): String {
        var dayName = ""
        when (dayNo) {
            1 -> dayName = "Sun"
            2 -> dayName = "Mon"
            3 -> dayName = "Tue"
            4 -> dayName = "Wed"
            5 -> dayName = "Thu"
            6 -> dayName = "Fri"
            7 -> dayName = "Sat"

        }
        return dayName
    }

    private fun convertKmsToMiles(kms: Float): Float {
        return (0.621371 * kms).toFloat()
    }

    fun convertKmsToMilesInt(kms: Int): Int {
        return (0.621371 * kms).toInt()
    }

    private fun isSelectedUnitKg(context: Context): Boolean {
        return getPref(context, Constant.SELECTED_UNIT_KM_MILE, true)
    }

    fun getDistanceInKmAndMileFromSteps(
        context: Context,
        steps: String,
        isWithString: Boolean = true,
    ): String {
        try {
            return if (isSelectedUnitKg(context)) {
                if (isWithString) {
                    DecimalFormat("##.##", DecimalFormatSymbols(Locale.US)).format(
                        getDistanceRun(
                            steps.toLong()
                        ).toFloat()
                    ) + " km"
                } else {
                    DecimalFormat("##.##", DecimalFormatSymbols(Locale.US)).format(
                        getDistanceRun(
                            steps.toLong()
                        ).toFloat()
                    )
                }
            } else {
                if (isWithString) {
                    DecimalFormat(
                        "##.##",
                        DecimalFormatSymbols(Locale.US)
                    ).format(convertKmsToMiles(getDistanceRun(steps.toLong()).toFloat())) + " mile"
                } else {
                    DecimalFormat(
                        "##.##",
                        DecimalFormatSymbols(Locale.US)
                    ).format(convertKmsToMiles(getDistanceRun(steps.toLong()).toFloat()))
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun getDistanceFromKm(context: Context, kmDistance: Double): Double {
        return if (isSelectedUnitKg(context)) {
            kmDistance
        } else {
            DecimalFormat("##.##").format(convertKmsToMiles(kmDistance.toFloat()).toDouble())
                .toDouble()
        }
    }

    fun getDistanceType(context: Context): String {
        val distanceType: String = if (isSelectedUnitKg(context)) {
            context.resources.getString(R.string.km)
        } else {
            context.resources.getString(R.string._miles)
        }

        return distanceType
    }

    fun isLogin(context: Context): Boolean {
        return getPref(context, Constant.IS_LOGIN, false)
    }

    fun getTimeMillisFromDate(date: String, format: String): Long {
        val formatter = SimpleDateFormat(format, Locale.US)
        val dateTime = formatter.parse(date)
        val millis = dateTime!!.time
        Log.e("TAG", "getTimeMillisFromDate:===>>  $millis")
        return millis

    }

    /*   fun convertKmsToMiles(kms: Float): Int {
           return (0.621371 * kms).toFloat()
       }
   */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    fun transparentStatusBar(window: Window) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false, window)
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun setWindowFlag(bits: Int, on: Boolean, window: Window) {
        val winParams = window.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        window.attributes = winParams
    }

    fun contactUs(content: Context) {
        try {
            val sendIntentGmail = Intent(Intent.ACTION_SEND)
            sendIntentGmail.type = "plain/text"
            sendIntentGmail.setPackage("com.google.android.gm")
            sendIntentGmail.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf("fitnessentertainmentapps@gmail.com")
            )
            sendIntentGmail.putExtra(
                Intent.EXTRA_SUBJECT,
                content.resources.getString(R.string.app_name)
            )
            content.startActivity(sendIntentGmail)
        } catch (e: Exception) {
            val sendIntentIfGmailFail = Intent(Intent.ACTION_SEND)
            sendIntentIfGmailFail.type = "*/*"
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf("fitnessentertainmentapps@gmail.com")
            )
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_SUBJECT,
                content.resources.getString(R.string.app_name)
            )
            if (sendIntentIfGmailFail.resolveActivity(content.packageManager) != null) {
                content.startActivity(sendIntentIfGmailFail)
            }
        }
    }


    fun openUrl(content: Context, strUrl: String) {
        val appPackageName = content.packageName
        try {
            content.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
        } catch (e: android.content.ActivityNotFoundException) {
            content.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
        }
    }

    fun getHHMMSSFromSteps(steps: Int): String {
        var time = ""
        val totalSec = (steps * 60 / 100).toDouble().roundToInt()
        val sec = totalSec % 60
        val min = totalSec / 60 % 60
        val hours = totalSec / 60 / 60

        val strSec = if (sec < 10) "0$sec" else sec.toString()
        val strMin = if (min < 10) "0$min" else min.toString()
        val strHours = if (hours < 10) "0$hours" else hours.toString()
        time = "$strHours:$strMin:$strSec"
        println("$strHours:$strMin:$strSec")
        return time
    }

    fun openAppSetting(context: Context, isFromMain: Boolean = false) {
        if (isFromMain) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            (context as Activity).startActivity(intent)
        } else {
            (context as Activity).startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)

        }
    }

    fun isBatteryOptimizationAllow(activity: Activity): Boolean {
        val packageName: String = activity.packageName
        val pm: PowerManager? = activity.getSystemService(Context.POWER_SERVICE) as PowerManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm!!.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }
    }

    fun getLastSyncDateAndTime(requireContext: Context): String {
        val lastSyncDate = getPref(requireContext, Constant.LAST_SYNC_DATE, "")!!
        Log.e("TAG", "getLastSyncDateAndTime:lastSyncDate==>>  $lastSyncDate")
        return if (lastSyncDate != "") {
            convertToCustomFormat(lastSyncDate, Constant.formatMMMDDAHHMM, Constant.formatFull)
        } else {
            ""
        }
    }

    fun isPurchased(context: Context): Boolean {
        return getPref(context, Constant.PREF_KEY_PURCHASE_STATUS, false)
    }

    fun getMonthsIndexWise(currentMonth: Int): CurrentMonthYear {
        val dataClass = CurrentMonthYear()
        val c = Calendar.getInstance()
        c.time = Date()
        c.add(Calendar.MONTH, currentMonth)
        val fullDate = SimpleDateFormat(Constant.formatMMMYYYY, Locale.US)
        val formatMonth = SimpleDateFormat(Constant.formatM, Locale.US)
        val formatYear = SimpleDateFormat(Constant.formatYYYY, Locale.US)
        dataClass.strCurrentMonthYear = fullDate.format(c.time)
        dataClass.currentMonth = formatMonth.format(c.time).toInt()
        dataClass.currentYear = formatYear.format(c.time).toInt()
        Log.e("TAG", "getMonthsIndexWise::: $currentMonth")
        return dataClass
    }

    fun getAllLanguageDataOptionArray(context: Context): ArrayList<SettingFragment.LanguagesData> {
        val strDataArray = ArrayList<SettingFragment.LanguagesData>()

        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.bengali),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.chinese),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.english_lang),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.french),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.german),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.hindi),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.italian),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.indonesian),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.japanese),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.korean),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.portuguese),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.punjabi),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.russian),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.spanish),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.tamil),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.telugu),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.turkish),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.urdu),
                false
            )
        )
        strDataArray.add(
            SettingFragment.LanguagesData(
                context.resources.getString(R.string.vietnamese),
                false
            )
        )
        return strDataArray
    }

//    fun isNetworkConnected(context: Context): Boolean {
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
//    }

    fun loadBannerAd(llAdView: RelativeLayout, llAdViewFacebook: LinearLayout, context: Context){
        if(this.isPurchased(context)){
            llAdView.visibility = View.GONE
            llAdViewFacebook.visibility = View.GONE
        }else if (getPref(context, Constant.AD_TYPE_FB_GOOGLE, "")
                .equals(Constant.AD_GOOGLE) &&
            getPref(context, Constant.STATUS_ENABLE_DISABLE, "")
                .equals(Constant.ENABLE)
        ) {
            CommonConstantAd.loadBannerGoogleAd(context, llAdView)
            llAdViewFacebook.visibility = View.GONE
            llAdView.visibility = View.VISIBLE
        } else if (getPref(context, Constant.AD_TYPE_FB_GOOGLE, "")
                .equals(Constant.AD_FACEBOOK)
            &&
            getPref(context, Constant.STATUS_ENABLE_DISABLE, "")
                .equals(Constant.ENABLE)
        ) {
            llAdViewFacebook.visibility = View.VISIBLE
            llAdView.visibility = View.GONE
            CommonConstantAd.loadFacebookBannerAd(context, llAdViewFacebook)
        } else {
            llAdView.visibility = View.GONE
            llAdViewFacebook.visibility = View.GONE
        }
    }

    fun isNetworkConnected(context: Context) =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }

    fun openInternetDialog(
        context: Context,
        callbackListener: CallbackListener,
        isSplash: Boolean?
    ) {
        if (!isNetworkConnected(context)) {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setTitle("No internet Connection")
            builder.setCancelable(false)
            builder.setMessage("Please turn on internet connection to continue")
            builder.setNegativeButton(
                "Retry"
            ) { dialog, _ ->
                if (!isSplash!!) {
                    openInternetDialog(context, callbackListener, false)
                }
                dialog.dismiss()
                callbackListener.onRetry()
            }
            builder.setPositiveButton(
                "Close"
            ) { dialog, _ ->
                dialog.dismiss()
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(homeIntent)
                (context as Activity).finishAffinity()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

}
class ContextUtils(base: Context) : ContextWrapper(base) {
    companion object {
        fun updateLocale(context: Context, localeToSwitchTo: Locale): ContextUtils {
            val resources = context.resources
            val configuration = resources.configuration // 1
            val localeList = LocaleList(localeToSwitchTo) // 2
            LocaleList.setDefault(localeList) // 3
            configuration.setLocales(localeList) // 4
            val updatedContext = context.createConfigurationContext(configuration) // 5
            return ContextUtils(updatedContext)
        }
    }
}
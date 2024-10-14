package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.adapter.WeekDaysADP
import com.example.run_tracker_native_app.alarm.AlarmReceiver
import com.example.run_tracker_native_app.databinding.ActivityReminderBinding
import com.example.run_tracker_native_app.dataclass.WeekDaysData
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.*


class ReminderActivity : BaseActivity() {
    private lateinit var binding: ActivityReminderBinding
    private var mCalendarCurrent: Calendar = Calendar.getInstance()
    private var newHour: Int = 0
    private var newMinute: Int = 0
    private var arrayListDayNumber: ArrayList<Int>? = null
    private val settingsViewModel by lazy {
        ViewModelProvider(this)[SettingsViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getMyPrefFromDatabase()
        binding.llDailySetReinder.setOnClickListener {
            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(newHour)
                    .setMinute(newMinute)
                    .setTitleText(getString(R.string.select_time))
                    .build()
            picker.show(supportFragmentManager, "fragment_tag")

            picker.addOnPositiveButtonClickListener {
                newHour = picker.hour
                newMinute = picker.minute
                var prefHour = newHour.toString()
                var prefMinute = newMinute.toString()
                if(newHour<10){
                    prefHour = "0$prefHour"
                }
                if(newMinute<10){
                    prefMinute = "0$prefMinute"
                }
                val strAmPm = if (newHour < 12) "AM" else "PM"

                binding.txtSatedTimer.text = "$prefHour:$prefMinute $strAmPm"
                settingsViewModel.updateReminderHourMyPref(newHour)
                settingsViewModel.updateReminderMinutesMyPref(newMinute)
            }

        }

        binding.llRepeatTime.setOnClickListener {
            showDaySelectionBottomSheetDialog()
        }

        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.txtSaveBtn.setOnClickListener {

            if (newHour != 0) {
                settingsViewModel.updateReminderHourMyPref(newHour)
            }

            if (newMinute != 0) {
                settingsViewModel.updateReminderMinutesMyPref(newMinute)
            }

            mCalendarCurrent = Calendar.getInstance()

            val mDay = mCalendarCurrent.get(Calendar.DATE)

            mCalendarCurrent[Calendar.DAY_OF_MONTH] = mDay
            mCalendarCurrent[Calendar.HOUR_OF_DAY] = newHour
            mCalendarCurrent[Calendar.MINUTE] = newMinute
            mCalendarCurrent[Calendar.SECOND] = 0
            AlarmReceiver().setAlarm(this@ReminderActivity, mCalendarCurrent, 1)
            finish()
        }

        Util.loadBannerAd(binding.llAdView,binding.llAdViewFacebook,this)

    }

    @SuppressLint("SetTextI18n")
    private fun getMyPrefFromDatabase() {
        settingsViewModel.myPrefLiveData.observe(this@ReminderActivity) { myPref ->
            if (myPref.reminderTimeHour != null && myPref.reminderTimeMinute != null && myPref.reminderDays.isNotEmpty()) {
                newHour = myPref.reminderTimeHour
                newMinute = myPref.reminderTimeMinute
                var prefHour = newHour.toString()
                var prefMinute = newMinute.toString()
                if(newHour<10){
                    prefHour = "0$prefHour"
                }
                if(newMinute<10){
                    prefMinute = "0$prefMinute"
                }
                val strAmPm = if (myPref.reminderTimeHour < 12) "AM" else "PM"
                binding.txtSatedTimer.text = "$prefHour:$prefMinute $strAmPm"
                arrayListDayNumber = ArrayList()
                arrayListDayNumber!!.addAll(myPref.reminderDays)
                var txtDaysName = ""
                for (i in arrayListDayNumber!!.indices) {
                    if (txtDaysName.isEmpty()) {
                        txtDaysName = Util.getShortDayName(arrayListDayNumber!![i])
                        binding.txtSelectedDays.text = txtDaysName
                    } else {
                        txtDaysName = (", ").plus(Util.getShortDayName(arrayListDayNumber!![i]))
                        binding.txtSelectedDays.append(txtDaysName)
                    }
                }
            } else {
                binding.txtSelectedDays.text = getString(R.string.select_days)
            }
        }
    }


    private fun showDaySelectionBottomSheetDialog() {

        val weekDays = listOf(
            WeekDaysData(1, "Sunday", false),
            WeekDaysData(2, "Monday", false),
            WeekDaysData(3, "Tuesday", false),
            WeekDaysData(4, "Wednesday", false),
            WeekDaysData(5, "Thursday", false),
            WeekDaysData(6, "Friday", false),
            WeekDaysData(7, "Saturday", false),
        )

        if (arrayListDayNumber != null) {
            val strDays = arrayListDayNumber!!.sorted()
            for (i in 1 until 8) {
                Log.e("item!!.days", i.toString())
                weekDays[i - 1].isSelected = strDays.contains(i)
            }
        }

        val bottomSheetDialog = BottomSheetDialog(this, R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_repeat)

        val txtCancel: AppCompatTextView? = bottomSheetDialog.findViewById(R.id.txtCancel)
        val txtOk: AppCompatTextView? = bottomSheetDialog.findViewById(R.id.txtOk)
        val daysRecyclerView: RecyclerView? = bottomSheetDialog.findViewById(R.id.rvRepeatDays)
        daysRecyclerView!!.layoutManager = LinearLayoutManager(this)
        val weekDayAdapter = WeekDaysADP(this)
        daysRecyclerView.adapter = weekDayAdapter
        weekDayAdapter.addAllData(weekDays)

        weekDayAdapter.setOnClickListener(object : WeekDaysADP.OnItemClickListener {
            override fun onItemClick(index: Int) {
                weekDayAdapter.onSelectionChange(index)
            }
        })

        txtCancel!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        txtOk!!.setOnClickListener {

            val arrayList = arrayListOf<Int>()

            weekDayAdapter.getAllSelectedData().forEach {
                arrayList.add(it.id)
            }

            arrayListDayNumber = arrayList
            settingsViewModel.updateReminderDaysMyPref(arrayList)
            setResult(Activity.RESULT_OK)

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setOnDismissListener {

        }

        bottomSheetDialog.setOnShowListener { dialogInterface ->
            bgTrans(dialogInterface)
        }

        bottomSheetDialog.show()
    }

    private fun bgTrans(dialogInterface: DialogInterface) {
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
            ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

}
package com.example.run_tracker_native_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.activity.BaseActivity
import com.example.run_tracker_native_app.databinding.FragmentChartWeekBinding
import com.example.run_tracker_native_app.dataclass.AllWeekData
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.utils.ActionSelectedEvent
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.ChartDataViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.squareup.otto.Subscribe
import java.util.Date


class ChartWeekFragment : Fragment() {

    private var currentWeek = 4
    private lateinit var weekDataArray: ArrayList<AllWeekData?>
    private var historyList = arrayListOf<MyRunningEntity>()
    private lateinit var binding: FragmentChartWeekBinding
    private var distanceUnit: String = ""
    private var action: String =""
    private val chartDataViewModel by lazy {
        ViewModelProvider(this)[ChartDataViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChartWeekBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_chart_week, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        action =getString(R.string.distance)
        BaseActivity.eventBus.register(this)
        getMyPrefFromDatabase()
        currentWeek = Util.getCurrentWeekNumberFromDate()
        initWeekData(currentWeek)
        setChartData()

        binding.imgPrevious.setOnClickListener {
            if (currentWeek > -49) {
                currentWeek--
                initWeekData(currentWeek)
                setChartData()
            }
        }

        binding.imgNext.setOnClickListener {
            if (currentWeek < Util.getCurrentWeekNumberFromDate()) {
                currentWeek++
                initWeekData(currentWeek)
                setChartData()
            }
        }
        addHistoryToList()
    }

    private fun getMyPrefFromDatabase() {
        chartDataViewModel.myPrefLiveData.observe(viewLifecycleOwner) { myPref ->
            distanceUnit = myPref.distanceUnit
            updateDataToChart(action)
        }
    }

    private fun addHistoryToList() {
        chartDataViewModel.runningListAllHistoryData.observe(viewLifecycleOwner) { history ->
            historyList.clear()
            historyList.addAll(history)
            updateDataToChart(action)
        }

    }

    @Subscribe
    fun onDurationSelected(event: ActionSelectedEvent) {
        action = event.action

        updateDataToChart(action)
        Log.e("onDurationSelected Week: ", action)
    }

    private fun sumDistanceBetweenDates(
        data: ArrayList<MyRunningEntity>,
        startDate: Date,
        endDate: Date,
        action: String
    ): Double {
        when (action) {
            getString(R.string.distance) -> {
                return if (distanceUnit == "km") {
                    (data.filter { it.date in startDate..endDate }
                        .sumOf { it.distance.toDouble() } / 1000)
                }else {
                    (data.filter { it.date in startDate..endDate }
                        .sumOf { it.distance.toDouble() } / 1609.34)
                }
            }

            getString(R.string.duration) -> {
                return data.filter { it.date in startDate..endDate }
                    .sumOf { it.timeInSeconds.toDouble() }/60
            }

            getString(R.string.kcal) -> {
                return data.filter { it.date in startDate..endDate }
                    .sumOf { it.kcal.toDouble() }
            }
            else -> {
                return 0.0
            }
        }

    }


    private fun setChartData() {

        try {

            binding.chart.description?.isEnabled = false

            binding.chart.setMaxVisibleValueCount(60)

            binding.chart.setPinchZoom(false)

            binding.chart.setDrawBarShadow(false)
            binding.chart.setDrawGridBackground(false)

            val xAxis = binding.chart.xAxis
            xAxis?.position = XAxis.XAxisPosition.BOTTOM
            xAxis?.setDrawGridLines(false)

            binding.chart.axisLeft?.setDrawGridLines(false)
            xAxis?.textColor = ContextCompat.getColor(requireContext(), R.color.black)
            binding.chart.axisLeft?.textColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.chart.axisRight?.textColor =
                ContextCompat.getColor(requireContext(), R.color.black)
            binding.chart.animateY(1500)

            binding.chart.legend?.isEnabled = false
            Log.e("weekDataArray.last()", "${weekDataArray.last()!!.longDate.time}")

            chartDataViewModel.getHistoryFromDateToDate(
                fromDate = weekDataArray.first()!!.longDate.time,
                toDate = Date(weekDataArray.last()!!.longDate.time + (1000 * 60 * 60 * 24)).time
            )

            Handler(Looper.getMainLooper()).postDelayed({
                updateDataToChart(action)
            }, 500)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun updateDataToChart(action: String) {
        val values = ArrayList<BarEntry>()
        for (i in 0 until weekDataArray.size) {
            if (historyList.isNotEmpty()) {
                val sumDistance: Double = if (i == weekDataArray.size - 1) {
                    sumDistanceBetweenDates(
                        historyList,
                        weekDataArray[i]!!.longDate,
                        Date(weekDataArray[i]!!.longDate.time + (1000 * 60 * 60 * 24)),
                        action
                    )
                } else {
                    sumDistanceBetweenDates(
                        historyList,
                        weekDataArray[i]!!.longDate,
                        weekDataArray[i + 1]!!.longDate,
                        action
                    )
                }
                values.add(
                    BarEntry(
                        weekDataArray[i]!!.strDay.toFloat(),
                        sumDistance.toFloat())
                )
            } else {
                values.add(
                    BarEntry(
                        weekDataArray[i]!!.strDay.toFloat(),
                        0.0F
                    )
                )
            }
        }
        val set1: BarDataSet

        if (binding.chart.data != null && binding.chart.data!!.dataSetCount > 0) {
            set1 = binding.chart.data!!.getDataSetByIndex(0) as BarDataSet
            set1.entries = values
            binding.chart.data!!.notifyDataChanged()
            binding.chart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "Data Set")
            set1.setColors(ContextCompat.getColor(requireContext(), R.color.theme))
            set1.setDrawValues(false)
            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            binding.chart.data = data
            binding.chart.setFitBars(true)
        }

        binding.chart.invalidate()
    }

    @SuppressLint("SetTextI18n")
    fun initWeekData(currentWeekCount: Int) {
        weekDataArray = ArrayList()
        weekDataArray = Util.getWeekDates(currentWeekCount)
        binding.txtCurrentWeek.text =
            weekDataArray[0]!!.month.split("-")[0] + " " + weekDataArray[0]!!.strDay + "-" + weekDataArray[weekDataArray.size - 1]!!.strDay
    }

    override fun onDestroyView() {
        super.onDestroyView()
        BaseActivity.eventBus.unregister(this)
    }
}


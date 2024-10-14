package com.example.run_tracker_native_app.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.activity.HistoryDetailActivity
import com.example.run_tracker_native_app.adapter.RecentHistoryADP
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.databinding.FragmentHomeBinding
import com.example.run_tracker_native_app.viewmodels.HomeViewModel
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private var recentHistoryADP: RecentHistoryADP? = null
    private val homeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private var distanceUnit: String = ""
    private var dailyGoal: Int = 0
    private var totalDistanceRecordToday: Float = 0.0F
    private var totalDistanceRecordAllTime: Float = 0.00F
    private var longestDistanceRecord: Float = 0.0F
    private var historyRecordList: List<MyRunningEntity> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    interface OnClickFragmentToActivity {
        fun onClick(s: String?)
    }

    private var someEventListener: OnClickFragmentToActivity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        someEventListener = try {
            activity as OnClickFragmentToActivity
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement onSomeEventListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtMore.setOnClickListener {
            someEventListener!!.onClick(getString(R.string.recent_history))
        }
        getMyPrefFromDatabase()
        getHistoryFromDatabase()
        initRecentHistoryADP()
    }

    @SuppressLint("SetTextI18n")
    private fun getMyPrefFromDatabase() {
        homeViewModel.myPrefLiveData.observe(viewLifecycleOwner) { myPref ->
            binding.tvDailyGoal.text = "${myPref.dailyGoal} ${myPref.distanceUnit.uppercase()}"
            binding.tvDistanceUnit1.text = myPref.distanceUnit.uppercase()
            binding.tvDistanceUnit2.text = myPref.distanceUnit.uppercase()
            binding.tvDistanceUnit5.text = "${myPref.distanceUnit.uppercase()}/hr"
            binding.tvDistanceUnit6.text = myPref.distanceUnit.uppercase()
            distanceUnit = myPref.distanceUnit
            dailyGoal = myPref.dailyGoal
            getMyTotalDistanceToday()
            getTotalDistanceOfAllTime()
            getLongestDistanceRecord()
            getAllHistory()
        }
        homeViewModel.myTotalDistanceToday.observe(viewLifecycleOwner) { todayDistance ->
            if (todayDistance != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    totalDistanceRecordToday = todayDistance
                    getMyTotalDistanceToday()
                }, 500)
            }
        }

        homeViewModel.myTotalDistanceAllTime.observe(viewLifecycleOwner) { totalDistance ->
            if (totalDistance == null) return@observe
            Handler(Looper.getMainLooper()).postDelayed({
                totalDistanceRecordAllTime = totalDistance
                getTotalDistanceOfAllTime()
            }, 500)
        }

        homeViewModel.myTotalAllTime.observe(viewLifecycleOwner) { totalTime ->
            if (totalTime == null) return@observe
            val hours = totalTime / 3600
            val minutes = totalTime % 3600 / 60
            val secs = totalTime % 60
            val time = String.format(
                Locale.getDefault(),
                "%02d:%02d", hours,
                minutes, secs
            )
            binding.tvTotalTime.text = time
        }

        homeViewModel.myTotalCal.observe(viewLifecycleOwner) { totalCal ->
            if (totalCal == null) return@observe
            binding.tvTotalCal.text = String.format("%.2f", totalCal)//totalCal.toInt().toString()
        }
        homeViewModel.myTotalAvgSpeed.observe(viewLifecycleOwner) { totalAvg ->
            if (totalAvg == null) return@observe
            binding.tvAvgSpeed.text = String.format("%.2f", totalAvg)//totalCal.toInt().toString()
        }
        homeViewModel.myLongestDistance.observe(viewLifecycleOwner) { longestDistance ->
            if (longestDistance == null) return@observe
            Handler(Looper.getMainLooper()).postDelayed({
                longestDistanceRecord = longestDistance
                getLongestDistanceRecord()
            }, 500)
        }

        homeViewModel.myTopSpeed.observe(viewLifecycleOwner) { topSpeed ->
            if (topSpeed == null) return@observe
            Handler(Looper.getMainLooper()).postDelayed({
                binding.tvTopSpeed.text = String.format("%.2f", topSpeed)
                binding.tvTopSpeedUnit.text = "$distanceUnit/HRS"
            }, 500)

        }

        homeViewModel.myLongestDuration.observe(viewLifecycleOwner) { longestDuration ->
            if (longestDuration == null) return@observe
            val hours = longestDuration / 3600
            val minutes = longestDuration % 3600 / 60
            val secs = longestDuration % 60
            val time = String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d", hours,
                minutes, secs
            )
            binding.tvLongestDuration.text = time
        }
    }

    private fun getLongestDistanceRecord() {
        val distance =
            if (distanceUnit == "km") longestDistanceRecord / 1000.0 else longestDistanceRecord / 1609.34
        binding.tvLongestDistance.text = String.format("%.2f", distance)
    }

    @SuppressLint("SetTextI18n")
    private fun getTotalDistanceOfAllTime() {
        if (totalDistanceRecordAllTime == 0.00F) binding.tvTotalDistance.text = "00.00"
        else {
            val distance =
                if (distanceUnit == "km") totalDistanceRecordAllTime / 1000.0 else totalDistanceRecordAllTime / 1609.34
           if(distance<10) binding.tvTotalDistance.text = String.format("0%.2f", distance)
           else binding.tvTotalDistance.text = String.format("%.2f", distance)
        }
    }

    private fun getMyTotalDistanceToday() {
        val distance =
            if (distanceUnit == "km") totalDistanceRecordToday / 1000.0 else totalDistanceRecordToday / 1609.34
        binding.tvTodayDistence.text = String.format("%.2f", distance)
        Log.e("setPercent : ", "${(distance * 150) / dailyGoal}")
        binding.circularProgressBar.setPercent(((distance * 150) / dailyGoal).toInt())
    }

    private fun getHistoryFromDatabase() {
        homeViewModel.runningListHistoryData.observe(viewLifecycleOwner) { historyList ->
            if (historyList != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    historyRecordList = historyList
                    getAllHistory()
                }, 500)

            }
        }
    }

    private fun getAllHistory() {
        if (historyRecordList.isNotEmpty()) {
            binding.llEmptyView.visibility = View.GONE
            binding.rvRecentHistory.visibility = View.VISIBLE
            binding.historyTitle.visibility = View.VISIBLE
        } else {
            binding.llEmptyView.visibility = View.GONE
            binding.rvRecentHistory.visibility = View.GONE
            binding.historyTitle.visibility = View.GONE
        }
        recentHistoryADP!!.setHistory(historyRecordList, distanceUnit)
    }

    private fun initRecentHistoryADP() {
        recentHistoryADP = RecentHistoryADP(
            requireContext(),
            isFromMain = true,
            itemCheckListener = { _, _ -> },
            itemClickListener = { item, _ ->
                val intent = Intent(requireContext(), HistoryDetailActivity::class.java)
                intent.putExtra("HistoryId", item.id)
                startActivity(intent)
            })
        binding.rvRecentHistory.setHasFixedSize(true)
        binding.rvRecentHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentHistory.adapter = recentHistoryADP
    }
}
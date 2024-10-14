package com.example.run_tracker_native_app.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.activity.BaseActivity
import com.example.run_tracker_native_app.adapter.ChartFragmentADP
import com.example.run_tracker_native_app.databinding.FragmentReportBinding
import com.example.run_tracker_native_app.utils.ActionSelectedEvent
import com.example.run_tracker_native_app.viewmodels.ReportViewModel
import java.util.Locale


class ReportFragment : Fragment() {

    private lateinit var chartFragmentADP: ChartFragmentADP
    private var strSubChartType = ""
    private var distanceUnit: String = ""
    private var totalDistanceRecordAllTime: Float = 0.0F

    private lateinit var binding: FragmentReportBinding
    private val reportViewModel by lazy {
        ViewModelProvider(this)[ReportViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabLayout()

        binding.imgSetting.setOnClickListener {
            someEventListener!!.onClick(getString(R.string.settings))
        }

        binding.llSeeMore.setOnClickListener {
            someEventListener!!.onClick(getString(R.string.recent_history))
        }

        getMyPrefFromDatabase()
    }

    private var someEventListener: HomeFragment.OnClickFragmentToActivity? = null

    @SuppressLint("SetTextI18n")
    private fun getMyPrefFromDatabase() {
        reportViewModel.myPrefLiveData.observe(viewLifecycleOwner) { myPref ->
            distanceUnit = myPref.distanceUnit
            binding.tvDistanceUnit.text = distanceUnit
            getTotalDistanceOfAllTime()
        }
        reportViewModel.myTotalDistanceAllTime.observe(viewLifecycleOwner) { totalDistance ->
            if (totalDistance == null) return@observe
            Handler(Looper.getMainLooper()).postDelayed({
                totalDistanceRecordAllTime = totalDistance
                getTotalDistanceOfAllTime()
            }, 500)
        }

        reportViewModel.myTotalAllTime.observe(viewLifecycleOwner) { totalTime ->
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

        reportViewModel.myTotalCal.observe(viewLifecycleOwner) { totalCal ->
            if (totalCal == null) return@observe
            binding.tvTotalCal.text = String.format("%.2f", totalCal)//totalCal.toInt().toString()
        }
        reportViewModel.myTotalAvgSpeed.observe(viewLifecycleOwner) { totalAvg ->
            if (totalAvg == null) return@observe
            binding.tvAvgSpeed.text = String.format("%.2f", totalAvg)//totalCal.toInt().toString()
        }
    }

    private fun getTotalDistanceOfAllTime() {
        val distance =
            if (distanceUnit == "km") totalDistanceRecordAllTime / 1000.0 else totalDistanceRecordAllTime / 1609.34
        binding.tvTotalDistance.text = String.format("%.2f", distance)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        someEventListener = try {
            activity as HomeFragment.OnClickFragmentToActivity
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement onSomeEventListener")
        }
    }


    private fun initTabLayout() {
        chartFragmentADP = ChartFragmentADP(requireActivity().supportFragmentManager)
        chartFragmentADP.addFragment(ChartWeekFragment(), "Week")
        chartFragmentADP.addFragment(ChartMonthFragment(), "Month")

        binding.viewpager.adapter = chartFragmentADP
        binding.tabLayout.setupWithViewPager(binding.viewpager)


        binding.txtDuration.setOnClickListener {
            binding.txtDuration.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view
                )
            )
            binding.txtDistance.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )
            binding.txtKcal.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )

            binding.txtDuration.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.txtDistance.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.txtGrayDark
                )
            )
            binding.txtKcal.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.txtGrayDark
                )
            )

//            llMainHeightTop.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.seleted_tab_indicator))
            strSubChartType = getString(R.string.duration)
            BaseActivity.eventBus.post(ActionSelectedEvent(getString(R.string.duration)))
        }

        binding.txtDistance.setOnClickListener {
            binding.txtDistance.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view
                )
            )
            binding.txtDuration.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )
            binding.txtKcal.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )

            binding.txtKcal.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.txtGrayDark
                )
            )
            binding.txtDuration.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.txtGrayDark
                )
            )
            binding.txtDistance.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )

//            llMainHeightTop.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.seleted_tab_indicator))
            strSubChartType = getString(R.string.distance)
            BaseActivity.eventBus.post(ActionSelectedEvent(getString(R.string.distance)))
        }

        binding.txtKcal.setOnClickListener {
            binding.txtKcal.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view
                )
            )
            binding.txtDistance.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )
            binding.txtDuration.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )

            binding.txtDuration.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.txtGrayDark
                )
            )
            binding.txtDistance.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.txtGrayDark
                )
            )
            binding.txtKcal.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

//            llMainHeightTop.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.seleted_tab_indicator))
            strSubChartType = getString(R.string.kcal)
            BaseActivity.eventBus.post(ActionSelectedEvent(getString(R.string.kcal)))
        }
    }
}
package com.example.run_tracker_native_app.fragments

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.FragmentDailyGoalBinding
import com.example.run_tracker_native_app.viewmodels.IntroProfileViewModel
import java.lang.reflect.Field


class DailyGoalFragment : Fragment() {

    private var strDistanceType = ""
    private var kmValue = 1
    private var milesValue = 1
    private lateinit var binding: FragmentDailyGoalBinding
    private val introProfileViewModel by lazy {
        ViewModelProvider(this)[IntroProfileViewModel::class.java]
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNumberPickerData(true)
        strDistanceType = getString(R.string.km)
        binding.txtKm.setOnClickListener {
            binding.txtKm.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view
                )

            )
            binding.txtMiles.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )

            binding.txtKm.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.txtMiles.setTextColor(ContextCompat.getColor(requireContext(), R.color.txtGray))

            binding.llMainHeightTop.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.seleted_tab_indicator)
            strDistanceType = getString(R.string.km)
            introProfileViewModel.updateDistanceUnitMyPref(strDistanceType)
            setNumberPickerData(true)
        }

        binding.txtMiles.setOnClickListener {
            binding.txtMiles.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view
                )
            )
            binding.txtKm.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.selected_tab_view_trans
                )
            )

            binding.txtKm.setTextColor(ContextCompat.getColor(requireContext(), R.color.txtGray))
            binding.txtMiles.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.llMainHeightTop.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.seleted_tab_indicator)
            strDistanceType = getString(R.string.mile)
            introProfileViewModel.updateDistanceUnitMyPref(strDistanceType)
            setNumberPickerData(false)
        }

        introProfileViewModel.updateDailyGoalUnitMyPref(kmValue)

    }

    private fun setNumberPickerData(isKm: Boolean) {
        if (isKm) {
            binding.pickerDistanceKm.visibility = View.VISIBLE
            binding.pickerDistanceMiles.visibility = View.GONE

            binding.pickerDistanceKm.minValue = 1
            binding.pickerDistanceKm.maxValue = 50

            binding.pickerDistanceKm.value = kmValue
        } else {
            binding.pickerDistanceKm.visibility = View.GONE
            binding.pickerDistanceMiles.visibility = View.VISIBLE

            binding.pickerDistanceMiles.minValue = 1
            binding.pickerDistanceMiles.maxValue = 50

            binding.pickerDistanceMiles.value = milesValue
        }

        binding.pickerDistanceKm.setOnValueChangedListener { _, _, i2 ->
            kmValue = i2
            introProfileViewModel.updateDailyGoalUnitMyPref(kmValue)
        }
        binding.pickerDistanceMiles.setOnValueChangedListener { _, _, i2 ->
            milesValue = i2
            introProfileViewModel.updateDailyGoalUnitMyPref(milesValue)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDailyGoalBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_daily_goal, container, false)
    }

}
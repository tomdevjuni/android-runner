package com.example.run_tracker_native_app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.FragmentGenderBinding
import com.example.run_tracker_native_app.viewmodels.IntroProfileViewModel

class GenderFragment : Fragment() {
    private lateinit var binding: FragmentGenderBinding
    private val introProfileViewModel by lazy {
        ViewModelProvider(this)[IntroProfileViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.llMale.setOnClickListener{
            binding.llMaleTransparent.visibility = View.VISIBLE
            binding.llFemaleTransparent.visibility = View.GONE

            binding.txtMale.setTextColor(ContextCompat.getColor(requireContext(),R.color.theme))
            binding.txtFemale.setTextColor(ContextCompat.getColor(requireContext(),R.color.grayBd))

            binding.imgMaleDone.setImageResource(R.drawable.ic_selected_done)
            binding.imgDoneFemale.setImageResource(R.drawable.ic_unseleted_done)

            introProfileViewModel.updateGenderMyPref("male")
        }

        binding.llFemale.setOnClickListener{
            binding.llMaleTransparent.visibility = View.GONE
            binding.llFemaleTransparent.visibility = View.VISIBLE

            binding.txtMale.setTextColor(ContextCompat.getColor(requireContext(),R.color.grayBd))
            binding.txtFemale.setTextColor(ContextCompat.getColor(requireContext(),R.color.theme))

            binding.imgMaleDone.setImageResource(R.drawable.ic_unseleted_done)
            binding.imgDoneFemale.setImageResource(R.drawable.ic_selected_done)
            introProfileViewModel.updateGenderMyPref("female")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGenderBinding.inflate(inflater,container,false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_gender, container, false)
    }
}
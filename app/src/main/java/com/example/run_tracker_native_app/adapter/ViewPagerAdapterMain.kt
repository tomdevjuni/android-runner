package com.example.run_tracker_native_app.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.run_tracker_native_app.fragments.HistoryFragment
import com.example.run_tracker_native_app.fragments.HomeFragment
import com.example.run_tracker_native_app.fragments.ReportFragment
import com.example.run_tracker_native_app.fragments.SettingFragment

class ViewPagerAdapterMain(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {

        val tp: Fragment = when (position) {
            0 -> {
                HomeFragment()
            }
            1 -> {
                HistoryFragment()
            }
            2 -> {
                ReportFragment()
            }
            else -> {
                SettingFragment()
            }
        }
        return tp
    }

    override fun getCount(): Int {
        return 4
    }


}

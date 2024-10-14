package com.example.run_tracker_native_app.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.run_tracker_native_app.fragments.DailyGoalFragment
import com.example.run_tracker_native_app.fragments.GenderFragment
class ProfileViewPagerADP(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {

        val tp: Fragment = when (position) {
            0 -> {
                GenderFragment()
            }
            1 -> {
                DailyGoalFragment()
            }
            else -> {
                GenderFragment()
            }
        }
        return tp
    }

    override fun getCount(): Int {
        return 2
    }


}

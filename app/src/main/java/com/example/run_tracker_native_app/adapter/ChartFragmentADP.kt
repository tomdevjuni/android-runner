package com.example.run_tracker_native_app.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ChartFragmentADP(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
    private val fragmentList : ArrayList<Fragment> = ArrayList()
    private val stringArrayList : ArrayList<String> =  ArrayList()

    fun addFragment(fragment: Fragment,str:String){
        fragmentList.add(fragment)
        stringArrayList.add(str)
    }


    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return stringArrayList[position]
    }
}
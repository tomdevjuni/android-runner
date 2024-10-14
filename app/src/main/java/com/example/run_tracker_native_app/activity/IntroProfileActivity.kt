package com.example.run_tracker_native_app.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.adapter.ProfileViewPagerADP
import com.example.run_tracker_native_app.databinding.ActivityIntroProfileBinding
import com.example.run_tracker_native_app.database.MyPref
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.IntroProfileViewModel

class IntroProfileActivity : BaseActivity() {

    var isDoneClick = false
    private lateinit var binding: ActivityIntroProfileBinding
    private val introProfileViewModel by lazy {
        ViewModelProvider(this)[IntroProfileViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setLightStatusBars(false)
        val mayPref = MyPref(
            id = 1,
            reminderTimeHour = 8,
            reminderTimeMinute = 0,
            reminderDays = listOf(1,2,3,4,5,6,7)
        )
        introProfileViewModel.insertMyPref(mayPref)
        initAdp()
    }

    private fun getItem(): Int {
        return binding.profileViewpager.currentItem + 1
    }

    private fun initAdp(){
        val mViewPagerAdapter = ProfileViewPagerADP(supportFragmentManager)
        binding.profileViewpager.offscreenPageLimit = mViewPagerAdapter.count
        binding.profileViewpager.adapter = mViewPagerAdapter
        binding.profileViewpager.currentItem = 0
        binding.imgStepView.setImageResource(R.drawable.ic_1_step)

        binding.cardNext.setOnClickListener {
            if(isDoneClick){
                Util.setPref(this, Constant.IS_PROFILE_INTRO_DONE,true)
                val intent = Intent(applicationContext,LoadingPlanActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }else{
                binding.profileViewpager.setCurrentItem(getItem(), true)
            }

        }

        binding.profileViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {

                if(position == 0){
                    binding.imgStepView.setImageResource(R.drawable.ic_1_step)
                    binding.mainTitle.setText(R.string.what_s_your_gender)
                    binding.mainDescription.setText(R.string.calories_stride_length_calculation_need_it)
                }else{
                    binding.imgStepView.setImageResource(R.drawable.ic_2_step)
                    binding.mainTitle.setText(R.string.what_s_your_goal)
                    binding.mainDescription.setText(R.string.to_personalize_your_daily_goal)
                }

                isDoneClick = position > 0
                Log.e("TAG", "onPageSelected:::Position==>> $position  $isDoneClick")
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }

    private fun Window.setLightStatusBars(b: Boolean) {
        WindowCompat.getInsetsController(this, decorView).isAppearanceLightStatusBars = b
    }
}
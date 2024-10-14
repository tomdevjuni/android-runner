package com.example.run_tracker_native_app.activity

import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.adapter.ViewPagerAdapter
import com.example.run_tracker_native_app.databinding.ActivityIntroductionBinding
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util

class IntroductionActivity : BaseActivity() {

    private var mViewPagerAdapter: ViewPagerAdapter? = null
    private var images = intArrayOf(
        R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3
    )
    var isDoneClick = false
    private lateinit var binding: ActivityIntroductionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewPager()
    }
    private fun setViewPager() {
        binding.txtTitle.text = getString(R.string.itro_title_1)
        binding.txtDescription.text = getString(R.string.itro_desc_1)
        binding.txtSkipNow.setOnClickListener {
            Util.setPref(this, Constant.IS_FIRST_TIME_INTRO,false)
            val intent = Intent(applicationContext,IntroProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        mViewPagerAdapter = ViewPagerAdapter(this, images,isFromMain = true)
        binding.viewpagerIntro.adapter = mViewPagerAdapter
        binding.tabDots.setupWithViewPager(binding.viewpagerIntro)

        binding.viewpagerIntro.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if(position > 1){
                    isDoneClick = true
                    binding.imgClickNextDone.setImageResource(R.drawable.ic_done_click)
                }else{                    isDoneClick = false
                    binding.imgClickNextDone.setImageResource(R.drawable.ic_next_click)
                }
                when (position) {
                    0 -> {
                        binding.txtTitle.text = getString(R.string.itro_title_1)
                        binding.txtDescription.text = getString(R.string.itro_desc_1)
                    }
                    1 -> {
                        binding.txtTitle.text = getString(R.string.itro_title_2)
                        binding.txtDescription.text = getString(R.string.itro_desc_2)
                    }
                    2 -> {
                        binding.txtTitle.text = getString(R.string.itro_title_3)
                        binding.txtDescription.text = getString(R.string.itro_desc_3)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        binding.imgClickNextDone.setOnClickListener {
            if(isDoneClick){
                Util.setPref(this, Constant.IS_FIRST_TIME_INTRO,false)
                val intent = Intent(applicationContext,IntroProfileActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                binding.viewpagerIntro.setCurrentItem(getItem(), true)
            }
        }
    }


    private fun getItem(): Int {
        return binding.viewpagerIntro.currentItem + 1
    }
}
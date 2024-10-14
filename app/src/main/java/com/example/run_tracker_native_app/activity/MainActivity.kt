package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.viewpager.widget.ViewPager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.adapter.ViewPagerAdapterMain
import com.example.run_tracker_native_app.databinding.ActivityMainBinding
import com.example.run_tracker_native_app.fragments.HomeFragment
import com.example.run_tracker_native_app.interfaces.CallbackListener
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util


class MainActivity : BaseActivity(), HomeFragment.OnClickFragmentToActivity, CallbackListener {
    private lateinit var binding: ActivityMainBinding
    var backPressedTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        successCall()
        window.setLightStatusBars(false)
        initBottomView()
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback);

//        Util.loadBannerAd(binding.llAdView,binding.llAdViewFacebook,this)
    }
    private var onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (backPressedTime + 3000 > System.currentTimeMillis()) {

                finish()
            } else {
                Toast.makeText(this@MainActivity, "Press back again to leave the app.", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        }
    }
    private fun handelBackPressed() {
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {

            }
        } else {
            onBackPressedDispatcher.addCallback(this , object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedTime + 3000 > System.currentTimeMillis()) {

                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, "Press back again to leave the app.", Toast.LENGTH_LONG).show()
                    }
                    backPressedTime = System.currentTimeMillis()
                }
            })
        }
    }

    private fun successCall() {
        if (Util.isNetworkConnected(this)) {
            if (Constant.ENABLE_DISABLE == Constant.ENABLE) {
                Util.setPref(
                    this,
                    Constant.AD_TYPE_FB_GOOGLE,
                    Constant.AD_TYPE_FACEBOOK_GOOGLE
                )
                Util.setPref(
                    this,
                    Constant.FB_BANNER,
                    Constant.FB_BANNER_ID
                )
                Util.setPref(
                    this,
                    Constant.FB_INTERSTITIAL,
                    Constant.FB_INTERSTITIAL_ID
                )
                Util.setPref(
                    this,
                    Constant.GOOGLE_BANNER,
                    Constant.GOOGLE_BANNER_ID
                )
                Util.setPref(
                    this,
                    Constant.GOOGLE_INTERSTITIAL,
                    Constant.GOOGLE_INTERSTITIAL_ID
                )
                Util.setPref(
                    this,
                    Constant.STATUS_ENABLE_DISABLE,
                    Constant.ENABLE_DISABLE
                )
                setAppAdId()
            } else {
                Util.setPref(
                    this,
                    Constant.STATUS_ENABLE_DISABLE,
                    Constant.ENABLE_DISABLE
                )
            }
        } else {
            Util.openInternetDialog(this, this, true)
        }
    }






    private fun setAppAdId() {
        try {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            val beforeChangeId = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
            Log.e("TAG", "setAppAdId:BeforeChange:::::  $beforeChangeId")
            applicationInfo.metaData.putString(
                "com.google.android.gms.ads.APPLICATION_ID",
                Constant.GOOGLE_ADMOB_APP_ID
            )
            val afterChangeId = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
            Log.e("TAG", "setAppAdId:AfterChange::::  $afterChangeId")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initBottomView(activeIndex: Int = 0) {

        val mViewPagerAdapter = ViewPagerAdapterMain(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = mViewPagerAdapter.count
        binding.viewPager.adapter = mViewPagerAdapter
        binding.viewPager.isEnabled = false
        changeBottomIconsIndexWise(0)
        binding.viewPager.currentItem = activeIndex


        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                changeBottomIconsIndexWise(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        binding.imgBottomHome.setOnClickListener {
            changeBottomIconsIndexWise(0)
        }

        binding.imgBottomHistory.setOnClickListener {
            changeBottomIconsIndexWise(1)
        }

        binding.imgBottomReport.setOnClickListener {
            changeBottomIconsIndexWise(2)
        }

        binding.imgBottomSetting.setOnClickListener {
            changeBottomIconsIndexWise(3)
        }

        binding.floatingRun.setOnClickListener {
            if (!Util.checkRequiredPermission(this@MainActivity)) {
                val intent = Intent(applicationContext, AllowsLocation::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, RunningActivity::class.java)
                startActivity(intent)
            }

        }

    }

    fun changeBottomIconsIndexWise(index: Int) {
        when (index) {
            0 -> {
                binding.imgBottomHome.setColorFilter(
                    ContextCompat.getColor(this, R.color.theme),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomHistory.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.grayBd
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomReport.setColorFilter(
                    ContextCompat.getColor(this, R.color.grayBd),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomSetting.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.grayBd
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            1 -> {
                binding.imgBottomHome.setColorFilter(
                    ContextCompat.getColor(this, R.color.grayBd),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomHistory.setColorFilter(
                    ContextCompat.getColor(this, R.color.theme),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomReport.setColorFilter(
                    ContextCompat.getColor(this, R.color.grayBd),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomSetting.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.grayBd
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            2 -> {
                binding.imgBottomHome.setColorFilter(
                    ContextCompat.getColor(this, R.color.grayBd),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomHistory.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.grayBd
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomReport.setColorFilter(
                    ContextCompat.getColor(this, R.color.theme),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomSetting.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.grayBd
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            3 -> {
                binding.imgBottomHome.setColorFilter(
                    ContextCompat.getColor(this, R.color.grayBd),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomHistory.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.grayBd
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomReport.setColorFilter(
                    ContextCompat.getColor(this, R.color.grayBd),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.imgBottomSetting.setColorFilter(
                    ContextCompat.getColor(this, R.color.theme),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }
        binding.viewPager.currentItem = index


    }

    private fun Window.setLightStatusBars(b: Boolean) {
        WindowCompat.getInsetsController(this, decorView).isAppearanceLightStatusBars = b
    }

    override fun onClick(s: String?) {
        Log.e("TAG", "onClick:::Main Activity==>>  $s")
        if (s == getString(R.string.recent_history)) {
            changeBottomIconsIndexWise(1)
        } else if (s == getString(R.string.settings)) {
            changeBottomIconsIndexWise(3)
        }
    }

    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }
}
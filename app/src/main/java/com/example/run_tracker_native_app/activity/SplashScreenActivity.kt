package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.interfaces.CallbackListener
import com.example.run_tracker_native_app.utils.CommonConstantAd
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.interfaces.AdsCallback

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseActivity(), CallbackListener, AdsCallback {
    private var isLoaded = false
    private val handler = Handler(Looper.myLooper()!!)
    private val myRunnable = Runnable {
        if (Util.isNetworkConnected(this@SplashScreenActivity)) {
            if (!isLoaded) {
                startNextActivity(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        callApi()
    }

    private fun callApi() {
        if (Util.isNetworkConnected(this)) {
            successCall()
        } else {
            Util.openInternetDialog(this, this, true)
        }
        handler.postDelayed(myRunnable, 10000)
    }
    private fun successCall() {
        if (Util.getPref(this, Constant.SPLASH_SCREEN_COUNT, 1) == 1) {
            Util.setPref(this, Constant.SPLASH_SCREEN_COUNT, 2)
            startNextActivity(1000)
        } else {
            if(!Util.isPurchased(this)) checkAd() else startNextActivity(1000)
        }
    }
    private fun startNextActivity(time: Long) {
        try {
            Thread {
                kotlin.run {
                    synchronized(this) {
                        Thread.sleep(time)
                        runOnUiThread {
//                            val mainIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                            if(Util.getPref(this, Constant.IS_FIRST_TIME_INTRO,true)){
                                val intent = Intent(applicationContext,IntroductionActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                if(Util.getPref(applicationContext,Constant.IS_PROFILE_INTRO_DONE,false)){
                                    val intent = Intent(applicationContext,MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }else{
                                    val intent = Intent(applicationContext,IntroProfileActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                            }
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkAd() {
        if (Util.getPref(this, Constant.STATUS_ENABLE_DISABLE, "")
                .equals(Constant.ENABLE)
        ) {
            if (Util.getPref(this, Constant.AD_TYPE_FB_GOOGLE, "")
                    .equals(Constant.AD_GOOGLE)
            ) {
                CommonConstantAd.googlebeforloadAd(this)
            } else if (Util.getPref(this, Constant.AD_TYPE_FB_GOOGLE, "")
                    .equals(Constant.AD_FACEBOOK)
            ) {
                CommonConstantAd.facebookbeforeloadFullAd(this)
            }
            if (Util.getPref(this, Constant.STATUS_ENABLE_DISABLE, "")
                    .equals(Constant.ENABLE)
            ) {
                Handler(Looper.myLooper()!!).postDelayed({
                    when {
                        Util.getPref(
                            this@SplashScreenActivity,
                            Constant.AD_TYPE_FB_GOOGLE,
                            ""
                        ).equals(Constant.AD_GOOGLE) -> {
                            CommonConstantAd.showInterstitialAdsGoogle(
                                this@SplashScreenActivity,
                                this@SplashScreenActivity
                            )
                        }
                        Util.getPref(
                            this@SplashScreenActivity,
                            Constant.AD_TYPE_FB_GOOGLE,
                            ""
                        ).equals(Constant.AD_FACEBOOK) -> {
                            CommonConstantAd.showInterstitialAdsFacebook(this@SplashScreenActivity)
                        }
                        else -> {
                            startNextActivity(0)
                        }
                    }
                }, 3000)
                Util.setPref(this, Constant.SPLASH_SCREEN_COUNT, 1)
            } else {
                startNextActivity(0)
            }
        } else {
            Util.setPref(this, Constant.SPLASH_SCREEN_COUNT, 1)
            startNextActivity(1000)
        }
    }

    override fun onSuccess() {
    }

    override fun onCancel() {
    }

    override fun onRetry() {
        callApi()
    }

    override fun adLoadingFailed() {
        startNextActivity(0)
    }

    override fun adClose() {
        startNextActivity(0)
    }

    override fun startNextScreen() {
        startNextActivity(0)
    }

    override fun onLoaded() {
        isLoaded = true
    }


    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(myRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(myRunnable)
    }
}
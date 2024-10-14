package com.example.run_tracker_native_app.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.ActivityLoadingPlanBinding
import com.example.run_tracker_native_app.repo.IntroProfileRepository
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import java.util.concurrent.Executors


class LoadingPlanActivity : BaseActivity() {
    private lateinit var binding: ActivityLoadingPlanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gender :String = IntroProfileRepository.getInstance(this, Executors.newSingleThreadExecutor()).getGenderMyPref()
        if(gender == "female")  binding.profileImage.setImageResource(R.drawable.ic_female_load)
        setProgressBar()
    }

    private fun setProgressBar() {


        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = 10-millisUntilFinished/1000
                binding.progressBarLoader.progress = (sec*10).toInt()
                Log.e("TAG", "onTick:::Progress==>>>>  ${sec*10}  $sec" )

            }

            override fun onFinish() {
                Util.setPref(this@LoadingPlanActivity, Constant.IS_PROFILE_INTRO_DONE,true)
                val intent = Intent(applicationContext,MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
                Log.e("TAG", "onFinish:::==>> " )

            }
        }.start()
    }
}
package com.example.run_tracker_native_app.activity

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.ViewGroup
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.ActivityGetAchievementsBinding
import com.example.run_tracker_native_app.dataclass.AchievementData
import com.google.gson.Gson
import com.intuit.sdp.R.dimen as sdp


class GetAchievementsActivity : BaseActivity() {

    private lateinit var binding: ActivityGetAchievementsBinding
    private var achievementData: AchievementData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.icFaceBook.setOnClickListener { }

        binding.icWhatsapp.setOnClickListener { }

        binding.icInstagram.setOnClickListener { }

        binding.icMore.setOnClickListener { }


        setData()

    }

    private fun setData() {

        val strItemAchievement = intent.getStringExtra("item_achievement")

        if (!strItemAchievement.isNullOrEmpty()) {
            Log.e("ITEM_ACHIEVEMENT", strItemAchievement)

            achievementData = Gson().fromJson(strItemAchievement, AchievementData::class.java)

            if (achievementData != null) {
                binding.imgMadel.setImageResource(achievementData!!.image)

                if (achievementData!!.padding != null) {
                    val param = binding.imgMadel.layoutParams as ViewGroup.MarginLayoutParams
                    val marginTop = resources.getDimension(sdp._95sdp).toInt()
                    val marginBottom = resources.getDimension(sdp._35sdp).toInt()
                    param.setMargins(0, marginTop, 0, marginBottom)
                    binding.imgMadel.layoutParams = param
                }

                val spannableString =
                    SpannableString("${resources.getString(R.string.you_have_achieved)} ${achievementData!!.distance} ${achievementData!!.distanceUnit}!")
                spannableString.setSpan(
                    RelativeSizeSpan(resources.getDimension(R.dimen.sp0_6)),
                    resources.getString(R.string.you_have_achieved).length+1,
                    spannableString.length - (achievementData!!.distanceUnit.length+2),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.txtAchievementKm.text = spannableString

            }


        }


    }
}
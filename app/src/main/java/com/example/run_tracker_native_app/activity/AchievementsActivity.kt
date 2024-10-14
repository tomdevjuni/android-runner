package com.example.run_tracker_native_app.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.adapter.AchievementsADP
import com.example.run_tracker_native_app.databinding.ActivityAchievementsBinding
import com.example.run_tracker_native_app.dataclass.AchievementData
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.AchievementViewModel
import com.google.gson.Gson
import com.intuit.sdp.R.dimen as sdp

class AchievementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private var recyclerView: RecyclerView? = null
    private var achievementADP: AchievementsADP? = null
    private val achievementDataList: ArrayList<AchievementData> = arrayListOf()
    private var distanceUnit: String = "km"
    private val achievementViewModel by lazy {
        ViewModelProvider(this)[AchievementViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            finish()
        }

        setData()
        Util.loadBannerAd(binding.llAdView, binding.llAdViewFacebook, this)
    }


    private fun setData() {
        setListData()
        achievementViewModel.myPrefLiveData.observe(this@AchievementsActivity) { myPref ->
            distanceUnit = myPref.distanceUnit
            setListData()
            achievementViewModel.getMyTotalDistanceAllTime()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            achievementViewModel.myTotalDistanceAllTime.observe(this@AchievementsActivity) { totalDistance ->
                if (totalDistance == null) return@observe
                val distance =
                    if (distanceUnit == "km") totalDistance / 1000.0 else totalDistance / 1609.34
                achievementDataList.forEach { it.isCompleted = distance > it.distance }
                achievementADP!!.addAllData(achievementDataList)
            }
        }, 500)
        recyclerView = binding.rvAchievements
        achievementADP = AchievementsADP(this)

        recyclerView!!.layoutManager = GridLayoutManager(this, 3)
        recyclerView!!.adapter = achievementADP

        achievementADP!!.addAllData(achievementDataList)


        achievementADP!!.setOnClickListener(
            object : AchievementsADP.OnItemClickListener {
                override fun onItemClick(index: Int) {
                    if (achievementDataList[index].isCompleted) {
                        val intent =
                            Intent(this@AchievementsActivity, GetAchievementsActivity::class.java)
                        intent.putExtra(
                            "item_achievement",
                            Gson().toJson(achievementDataList[index])
                        )
                        startActivity(intent)
                    }

                }

            }
        )

    }

    private fun setListData() {
        achievementDataList.clear()
        achievementDataList.addAll(
            listOf(
                AchievementData(distanceUnit, 50, R.drawable.ic_50_km, false),
                AchievementData(distanceUnit, 100, R.drawable.ic_100_km, false),
                AchievementData(distanceUnit, 500, R.drawable.ic_500_km, false),
                AchievementData(
                    distanceUnit,
                    1000,
                    R.drawable.ic_1000_km,
                    false,
                    resources.getDimension(sdp._10sdp)
                ),
                AchievementData(
                    distanceUnit,
                    5000,
                    R.drawable.ic_5000_km,
                    false,
                    resources.getDimension(sdp._10sdp)
                ),
                AchievementData(
                    distanceUnit,
                    10000,
                    R.drawable.ic_10000_km,
                    false,
                    resources.getDimension(sdp._10sdp)
                ),
                AchievementData(distanceUnit, 20000, R.drawable.ic_20000_km, false),
                AchievementData(distanceUnit, 30000, R.drawable.ic_30000_km, false),
                AchievementData(distanceUnit, 50000, R.drawable.ic_50000_km, false),
                AchievementData(distanceUnit, 60000, R.drawable.ic_60000_km, false),
                AchievementData(distanceUnit, 75000, R.drawable.ic_75000_km, false),
                AchievementData(distanceUnit, 100000, R.drawable.ic_100000_km, false),
            )
        )
    }

}
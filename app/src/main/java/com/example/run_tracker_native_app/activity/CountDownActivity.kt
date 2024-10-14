package com.example.run_tracker_native_app.activity

import android.app.Activity
import android.os.Bundle
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.ActivityCountDownBinding
import com.example.run_tracker_native_app.imageslider.constants.AnimationTypes
import com.example.run_tracker_native_app.imageslider.constants.ScaleTypes
import com.example.run_tracker_native_app.imageslider.interfaces.ItemChangeListener
import com.example.run_tracker_native_app.imageslider.models.SlideModel


class CountDownActivity : BaseActivity() {
    private lateinit var binding: ActivityCountDownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountDownBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageList = ArrayList<SlideModel>() // Create image list
        imageList.add(SlideModel(R.drawable.img_one))
        imageList.add(SlideModel(R.drawable.img_two))
        imageList.add(SlideModel(R.drawable.img_three))
        imageList.add(SlideModel(R.drawable.img_go))


        binding.imageSlider.setImageList(imageList, ScaleTypes.CENTER_INSIDE)
        binding.imageSlider.setSlideAnimation(AnimationTypes.FLIP_HORIZONTAL)
        binding.imageSlider.setItemChangeListener(object : ItemChangeListener {
            override fun onItemChanged(position: Int) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
    }

}
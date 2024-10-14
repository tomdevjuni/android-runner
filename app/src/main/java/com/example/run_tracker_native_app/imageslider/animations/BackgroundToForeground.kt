package com.example.run_tracker_native_app.imageslider.animations

import android.view.View
import androidx.viewpager.widget.ViewPager.PageTransformer
import kotlin.math.abs
import kotlin.math.min


class BackgroundToForeground: PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val height: Float = view.height.toFloat()
        val width: Float = view.width.toFloat()
        val scale: Float = min(if (position < 0) 1f else abs(1f - position), 1f)

        view.scaleX = scale
        view.scaleY = scale
        view.pivotX = width * 0.5f
        view.pivotY = height * 0.5f
        view.translationX = if (position < 0) width * position else -width * position * 0.25f
    }

}
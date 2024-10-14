package com.example.run_tracker_native_app.imageslider

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.run_tracker_native_app.imageslider.adapters.ViewPagerAdapter
import com.example.run_tracker_native_app.imageslider.constants.AnimationTypes
import com.example.run_tracker_native_app.imageslider.constants.ScaleTypes
import com.example.run_tracker_native_app.imageslider.interfaces.ItemChangeListener
import com.example.run_tracker_native_app.imageslider.interfaces.ItemClickListener
import com.example.run_tracker_native_app.imageslider.interfaces.TouchListener
import com.example.run_tracker_native_app.imageslider.models.SlideModel
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.imageslider.animations.BackgroundToForeground
import com.example.run_tracker_native_app.imageslider.animations.CubeIn
import com.example.run_tracker_native_app.imageslider.animations.CubeOut
import com.example.run_tracker_native_app.imageslider.animations.DepthSlide
import com.example.run_tracker_native_app.imageslider.animations.FlipHorizontal
import com.example.run_tracker_native_app.imageslider.animations.FlipVertical
import com.example.run_tracker_native_app.imageslider.animations.ForegroundToBackground
import com.example.run_tracker_native_app.imageslider.animations.Gate
import com.example.run_tracker_native_app.imageslider.animations.RotateDown
import com.example.run_tracker_native_app.imageslider.animations.RotateUp
import com.example.run_tracker_native_app.imageslider.animations.Toss
import com.example.run_tracker_native_app.imageslider.animations.ZoomIn
import com.example.run_tracker_native_app.imageslider.animations.ZoomOut
import java.lang.reflect.Field
import java.util.*


@SuppressLint("ClickableViewAccessibility")
class ImageSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr) {

    private var viewPager: ViewPager? = null
    private var pagerDots: LinearLayout? = null
    private var viewPagerAdapter: ViewPagerAdapter? = null

    private var dots: Array<ImageView?>? = null

    private var currentPage = 0
    private var imageCount = 0

    private var cornerRadius: Int = 0
    private var period: Long = 0
    private var delay: Long = 0
    private var autoCycle = false

    private var selectedDot = 0
    private var unselectedDot = 0
    private var errorImage = 0
    private var placeholder = 0
    private var titleBackground = 0
    private var textAlign = "LEFT"
    private var indicatorAlign = "CENTER"
    private var swipeTimer = Timer()

    private var itemChangeListener: ItemChangeListener? = null
    private var touchListener: TouchListener? = null

    private var noDots = false
    private var textColor = "#FFFFFF"

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.image_slider, this, true)
        viewPager = findViewById(R.id.view_pager)
        pagerDots = findViewById(R.id.pager_dots)

        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ImageSlider,
            defStyleAttr,
            defStyleAttr
        )

        cornerRadius = typedArray.getInt(R.styleable.ImageSlider_iss_corner_radius, 1)
        period = typedArray.getInt(R.styleable.ImageSlider_iss_period, 1000).toLong()
        delay = typedArray.getInt(R.styleable.ImageSlider_iss_delay, 1000).toLong()
        autoCycle = typedArray.getBoolean(R.styleable.ImageSlider_iss_auto_cycle, false)
        placeholder = typedArray.getResourceId(
            R.styleable.ImageSlider_iss_placeholder,
            R.drawable.default_loading
        )
        errorImage = typedArray.getResourceId(
            R.styleable.ImageSlider_iss_error_image,
            R.drawable.default_error
        )
        selectedDot = typedArray.getResourceId(
            R.styleable.ImageSlider_iss_selected_dot,
            R.drawable.default_selected_dot
        )
        unselectedDot = typedArray.getResourceId(
            R.styleable.ImageSlider_iss_unselected_dot,
            R.drawable.default_unselected_dot
        )
        titleBackground = typedArray.getResourceId(
            R.styleable.ImageSlider_iss_title_background,
            R.drawable.default_gradient
        )
        noDots = typedArray.getBoolean(R.styleable.ImageSlider_iss_no_dots, false)

        if (typedArray.getString(R.styleable.ImageSlider_iss_text_align) != null) {
            textAlign = typedArray.getString(R.styleable.ImageSlider_iss_text_align)!!
        }

        if (typedArray.getString(R.styleable.ImageSlider_iss_indicator_align) != null) {
            indicatorAlign = typedArray.getString(R.styleable.ImageSlider_iss_indicator_align)!!
        }

        if (typedArray.getString(R.styleable.ImageSlider_iss_text_color) != null) {
            textColor = typedArray.getString(R.styleable.ImageSlider_iss_text_color)!!
        }

    }

    fun setImageList(imageList: List<SlideModel>) {
        viewPagerAdapter = ViewPagerAdapter(
            context,
            imageList,
            cornerRadius,
            errorImage,
            placeholder,
            titleBackground,
            textAlign,
            textColor
        )
        setAdapter(imageList)
    }

    fun setImageList(imageList: List<SlideModel>, scaleType: ScaleTypes? = null) {
        viewPagerAdapter = ViewPagerAdapter(
            context,
            imageList,
            cornerRadius,
            errorImage,
            placeholder,
            titleBackground,
            scaleType,
            textAlign,
            textColor
        )
        setAdapter(imageList)
    }

    private fun setAdapter(imageList: List<SlideModel>) {
        viewPager!!.adapter = viewPagerAdapter
        imageCount = imageList.size
        if (imageList.isNotEmpty()) {
            if (!noDots) {
                setupDots(imageList.size)
            }
            if (autoCycle) {
                startSliding()
            }
        }
    }

    fun setSlideAnimation(animationType: AnimationTypes) {
        when (animationType) {
            AnimationTypes.ZOOM_IN -> {
                viewPager!!.setPageTransformer(true, ZoomIn())
            }

            AnimationTypes.ZOOM_OUT -> {
                viewPager!!.setPageTransformer(true, ZoomOut())
            }

            AnimationTypes.DEPTH_SLIDE -> {
                viewPager!!.setPageTransformer(true, DepthSlide())
            }

            AnimationTypes.CUBE_IN -> {
                viewPager!!.setPageTransformer(true, CubeIn())
            }

            AnimationTypes.CUBE_OUT -> {
                viewPager!!.setPageTransformer(true, CubeOut())
            }

            AnimationTypes.FLIP_HORIZONTAL -> {
                viewPager!!.setPageTransformer(true, FlipHorizontal())
            }

            AnimationTypes.FLIP_VERTICAL -> {
                viewPager!!.setPageTransformer(true, FlipVertical())
            }

            AnimationTypes.ROTATE_UP -> {
                viewPager!!.setPageTransformer(true, RotateUp())
            }

            AnimationTypes.ROTATE_DOWN -> {
                viewPager!!.setPageTransformer(true, RotateDown())
            }

            AnimationTypes.FOREGROUND_TO_BACKGROUND -> {
                viewPager!!.setPageTransformer(true, ForegroundToBackground())
            }

            AnimationTypes.BACKGROUND_TO_FOREGROUND -> {
                viewPager!!.setPageTransformer(true, BackgroundToForeground())
            }

            AnimationTypes.TOSS -> {
                viewPager!!.setPageTransformer(true, Toss())
            }

            AnimationTypes.GATE -> {
                viewPager!!.setPageTransformer(true, Gate())
            }

        }
    }

    private fun setupDots(size: Int) {
        pagerDots!!.gravity = getGravityFromAlign(indicatorAlign)
        pagerDots!!.removeAllViews()
        dots = arrayOfNulls(size)

        for (i in 0 until size) {
            dots!![i] = ImageView(context)
            dots!![i]!!.setImageDrawable(ContextCompat.getDrawable(context, unselectedDot))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            pagerDots!!.addView(dots!![i], params)
        }
        dots!![0]!!.setImageDrawable(ContextCompat.getDrawable(context, selectedDot))

        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                currentPage = position
                for (dot in dots!!) {
                    dot!!.setImageDrawable(ContextCompat.getDrawable(context, unselectedDot))
                }
                Log.e("Pos: ", position.toString())
                dots!![position]!!.setImageDrawable(ContextCompat.getDrawable(context, selectedDot))
                if (itemChangeListener != null) itemChangeListener!!.onItemChanged(position)
            }

            override fun onPageSelected(position: Int) {
                currentPage = position
                for (dot in dots!!) {
                    dot!!.setImageDrawable(ContextCompat.getDrawable(context, unselectedDot))
                }
                dots!![position]!!.setImageDrawable(ContextCompat.getDrawable(context, selectedDot))
                if (itemChangeListener != null) itemChangeListener!!.onItemChanged(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

    }


    private fun startSliding(changeablePeriod: Long = period) {
        stopSliding()
        scheduleTimer(changeablePeriod)
    }


    private fun stopSliding() {
        swipeTimer.cancel()
        swipeTimer.purge()
    }

    private fun scheduleTimer(period: Long) {

        viewPager!!.setViewPageScroller(ViewPagerScroller(context))

        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            if (currentPage == imageCount) {
                if (itemChangeListener != null) itemChangeListener!!.onItemChanged(3)
                stopSliding()
                currentPage = 0
            } else {
                viewPager!!.setCurrentItem(currentPage++, true)
            }
        }

        swipeTimer = Timer()
        swipeTimer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, delay, period)
    }

    private fun ViewPager.setViewPageScroller(viewPageScroller: ViewPagerScroller) {
        try {
            val mScroller: Field = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.isAccessible = true
            mScroller.set(this, viewPageScroller)
        } catch (_: NoSuchFieldException) {
        } catch (_: IllegalArgumentException) {
        } catch (_: IllegalAccessException) {
        }

    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        viewPagerAdapter?.setItemClickListener(itemClickListener)
    }


    fun setItemChangeListener(itemChangeListener: ItemChangeListener) {
        this.itemChangeListener = itemChangeListener
    }

    fun setTouchListener(touchListener: TouchListener) {
        this.touchListener = touchListener
        this.viewPagerAdapter!!.setTouchListener(touchListener)
    }


    private fun getGravityFromAlign(textAlign: String): Int {
        return when (textAlign) {
            "RIGHT" -> {
                Gravity.END
            }

            "LEFT" -> {
                Gravity.START
            }

            else -> {
                Gravity.CENTER
            }
        }
    }

}




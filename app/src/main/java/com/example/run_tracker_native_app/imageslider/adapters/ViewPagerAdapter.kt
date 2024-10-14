package com.example.run_tracker_native_app.imageslider.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.run_tracker_native_app.imageslider.constants.ActionTypes
import com.example.run_tracker_native_app.imageslider.constants.ScaleTypes
import com.example.run_tracker_native_app.imageslider.interfaces.ItemClickListener
import com.example.run_tracker_native_app.imageslider.interfaces.TouchListener
import com.example.run_tracker_native_app.imageslider.models.SlideModel
import com.example.run_tracker_native_app.imageslider.transformation.RoundedTransformation
import com.example.run_tracker_native_app.R
import com.squareup.picasso.Picasso


class ViewPagerAdapter(context: Context?,
                       imageList: List<SlideModel>,
                       private var radius: Int,
                       private var errorImage: Int,
                       private var placeholder: Int,
                       private var titleBackground: Int,
                       private var scaleType: ScaleTypes?,
                       private var textAlign: String,
                       private var textColor: String) : PagerAdapter() {

    constructor(context: Context, imageList: List<SlideModel>, radius: Int, errorImage: Int, placeholder: Int, titleBackground: Int, textAlign: String, textColor: String) :
            this(context, imageList, radius, errorImage, placeholder, titleBackground, null, textAlign, textColor)

    private var imageList: List<SlideModel>? = imageList
    private var layoutInflater: LayoutInflater? = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    private var itemClickListener: ItemClickListener? = null
    private var touchListener: TouchListener? = null

    private var lastTouchTime: Long = 0
    private var currentTouchTime: Long = 0

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return imageList!!.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun instantiateItem(container: ViewGroup, position: Int): View{
        val itemView = layoutInflater!!.inflate(R.layout.pager_row, container, false)

        val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        val linearLayout = itemView.findViewById<LinearLayout>(R.id.linear_layout)
        val textView = itemView.findViewById<TextView>(R.id.text_view)
        textView.setTextColor(Color.parseColor(textColor))

        if (imageList!![position].title != null){
            textView.text = imageList!![position].title
            linearLayout.setBackgroundResource(titleBackground)
            textView.gravity = getGravityFromAlign(textAlign)
            linearLayout.gravity = getGravityFromAlign(textAlign)
        }else{
            linearLayout.visibility = View.INVISIBLE
        }

        // Image from url or local path check.
        val loader = if (imageList!![position].imageUrl == null){
            Picasso.get().load(imageList!![position].imagePath!!)
        }else{
            Picasso.get().load(imageList!![position].imageUrl!!)
        }

        // set Picasso options.
        if ((scaleType != null && scaleType == ScaleTypes.CENTER_CROP) || imageList!![position].scaleType == ScaleTypes.CENTER_CROP){
            loader.fit().centerCrop()
        } else if((scaleType != null && scaleType == ScaleTypes.CENTER_INSIDE) || imageList!![position].scaleType == ScaleTypes.CENTER_INSIDE){
            loader.fit().centerInside()
        }else if((scaleType != null && scaleType == ScaleTypes.FIT) || imageList!![position].scaleType == ScaleTypes.FIT){
            loader.fit()
        }

        loader.transform(RoundedTransformation(radius, 0))
            .placeholder(placeholder)
            .error(errorImage)
            .into(imageView)

        container.addView(itemView)

        imageView.setOnClickListener {
            lastTouchTime = currentTouchTime
            currentTouchTime = System.currentTimeMillis()
            when {
                currentTouchTime - lastTouchTime < 250 -> {
                    itemClickListener?.doubleClick(position)
                }
                else -> {
                    itemClickListener?.onItemSelected(position)
                }
            }
        }

        if (touchListener != null){
            imageView!!.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> touchListener!!.onTouched(ActionTypes.MOVE, position)
                    MotionEvent.ACTION_DOWN -> touchListener!!.onTouched(ActionTypes.DOWN, position)
                    MotionEvent.ACTION_UP -> touchListener!!.onTouched(ActionTypes.UP, position)
                }
                false
            }
        }

        return itemView
    }


    private fun getGravityFromAlign(textAlign: String): Int {
        return when (textAlign) {
            "RIGHT" -> {
                Gravity.END
            }
            "CENTER" -> {
                Gravity.CENTER
            }
            else -> {
                Gravity.START
            }
        }
    }


    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }


    fun setTouchListener(touchListener: TouchListener) {
        this.touchListener = touchListener
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

}
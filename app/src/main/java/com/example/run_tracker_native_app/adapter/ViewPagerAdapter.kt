package com.example.run_tracker_native_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.run_tracker_native_app.R
import java.util.Objects

class ViewPagerAdapter(
// Context object
    var context: Context, // Array of images
    private var images: IntArray,
    var isFromMain: Boolean = false,
) : PagerAdapter() {
    private var mLayoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // inflating the item.xml
        val itemView: View? = if (isFromMain) {
            mLayoutInflater.inflate(R.layout.item_viewpager_intro_1, container, false)
        } else {
            mLayoutInflater.inflate(R.layout.item_viewpager_intro, container, false)
        }

        // referencing the image view from the item.xml file
        val imageView = itemView!!.findViewById<View>(R.id.imageViewMain) as ImageView

        // setting the image in the imageView
        imageView.setImageResource(images[position])

        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

}
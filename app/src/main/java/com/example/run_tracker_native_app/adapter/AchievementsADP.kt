package com.example.run_tracker_native_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.dataclass.AchievementData
import com.intuit.sdp.R.dimen as sdp

class AchievementsADP(private val context: Context) :
    RecyclerView.Adapter<AchievementsADP.AchievementsViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private var data: ArrayList<AchievementData> = arrayListOf()

    fun setOnClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun getAllData(): List<AchievementData> {
        return data
    }

    fun addAllData(data: List<AchievementData>) {
        this.data.clear()
        this.data.addAll(data)
        notifyItemRangeChanged(0, data.size)
    }

    inner class AchievementsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val achievementMadel: AppCompatImageView = itemView.findViewById(R.id.imgAchievement)
        val distance: AppCompatTextView = itemView.findViewById(R.id.txtDistance)
        val distanceUnit: AppCompatTextView = itemView.findViewById(R.id.tv_distance_unit)
        val llLock: ImageView = itemView.findViewById(R.id.llLock)
        val llAchievementItem: FrameLayout = itemView.findViewById(R.id.flAchievementItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_achievements, parent, false)
        return AchievementsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementsViewHolder, position: Int) {
        val achievement = data[position]
        holder.achievementMadel.setImageResource(achievement.image)

        if (achievement.padding != null) {
            holder.achievementMadel.setPadding(achievement.padding!!.toInt())
        } else {
            holder.achievementMadel.setPadding(context.resources.getDimension(sdp._15sdp).toInt())
        }

        holder.distance.text = achievement.distance.toString()
        holder.distanceUnit.text = achievement.distanceUnit
        holder.llLock.visibility = if (achievement.isCompleted) View.GONE else View.VISIBLE


        holder.llAchievementItem.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = data.size

    interface OnItemClickListener {
        fun onItemClick(index: Int)

    }
}
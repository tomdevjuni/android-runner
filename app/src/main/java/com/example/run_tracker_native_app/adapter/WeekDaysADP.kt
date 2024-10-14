package com.example.run_tracker_native_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.dataclass.WeekDaysData

class WeekDaysADP(private val context: Context) :
    RecyclerView.Adapter<WeekDaysADP.WeekdayViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private var data: ArrayList<WeekDaysData> = arrayListOf()

    fun setOnClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


    fun getAllSelectedData(): List<WeekDaysData> {
        return data.filter { it.isSelected }
    }

    fun addAllData(data: List<WeekDaysData>) {
        this.data.addAll(data)
        notifyItemRangeInserted(0, this.data.size)
    }

    inner class WeekdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay: AppCompatTextView = itemView.findViewById(R.id.txtDay)
        val isDaySelect: AppCompatImageView = itemView.findViewById(R.id.imgCheckBox)
        val llDayMain: LinearLayout = itemView.findViewById(R.id.llDayMain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekdayViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_repeat_day, parent, false)
        return WeekdayViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekdayViewHolder, position: Int) {
        val weekday = data[position]
        holder.txtDay.text = weekday.name

        if (weekday.isSelected) {
            holder.isDaySelect.setImageResource(R.drawable.ic_selected_check_box)
        } else {
            holder.isDaySelect.setImageResource(R.drawable.ic_un_selected_check_box)
        }

        holder.llDayMain.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    fun onSelectionChange(index: Int) {
        data[index].isSelected = !data[index].isSelected
        notifyItemChanged(index)
    }

    override fun getItemCount(): Int = data.size

    interface OnItemClickListener {
        fun onItemClick(index: Int)
    }
}
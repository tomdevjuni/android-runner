package com.example.run_tracker_native_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.ItemAllHistoryBinding
import com.example.run_tracker_native_app.databinding.ItemRecentHistoryBinding
import com.example.run_tracker_native_app.database.helpers.MyLocationTypeConverters
import com.example.run_tracker_native_app.database.MyRunningEntity
import java.text.SimpleDateFormat
import java.util.Locale


class RecentHistoryADP(
    private val mContext: Context,
    private val isFromMain: Boolean,
    private val itemCheckListener: (MyRunningEntity, Int) -> Unit,
    private val itemClickListener: (MyRunningEntity, Int) -> Unit
) :
    RecyclerView.Adapter<RecentHistoryADP.ViewHolder>() {
    private var mContextAdp: Context? = null
    private var distanceUnitFromData: String? = null
    private var allHistory = arrayListOf<MyRunningEntity?>() // Cached all users
    private var selectedItems = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemRecentHistoryBinding: ItemRecentHistoryBinding?
        val itemAllHistoryBinding: ItemAllHistoryBinding?
        return if (isFromMain) {
            itemRecentHistoryBinding =
                ItemRecentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(itemRecentHistoryBinding, null)
        } else {
            itemAllHistoryBinding =
                ItemAllHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(null, itemAllHistoryBinding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mContextAdp = mContext
        if (allHistory.isNotEmpty()) {
            val history: MyRunningEntity = allHistory[position]!!
            holder.bind(
                history,
                mContextAdp!!,
                itemCheckListener,
                itemClickListener,
                selectedItems,
                distanceUnitFromData
            )
        }
    }

    override fun getItemCount(): Int {
        return if (isFromMain) {
            if (allHistory.size > 3) 3 else allHistory.size
        } else {
            allHistory.size
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setHistory(history: List<MyRunningEntity?>, distanceUnit: String) {
        distanceUnitFromData = distanceUnit
        allHistory.clear()
        allHistory.addAll(history)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItems(selectedItemsNew: MyRunningEntity, position: Int) {
        if (selectedItems.contains(selectedItemsNew.id!!)) {
            selectedItems.remove(selectedItemsNew.id)
        } else {
            selectedItems.add(selectedItemsNew.id)
        }
        notifyItemChanged(position)
    }

    fun setUnSelectedItemsAll() {
        selectedItems.clear()
        notifyItemRangeChanged(0,allHistory.size)
    }

    class ViewHolder(
        private val itemRecentHistoryBinding: ItemRecentHistoryBinding?,
        private val itemAllHistoryBinding: ItemAllHistoryBinding?,
    ) : RecyclerView.ViewHolder(
        itemRecentHistoryBinding?.root ?: itemAllHistoryBinding?.root!!
    ) {
        @SuppressLint("SetTextI18n")
        fun bind(
            history: MyRunningEntity,
            mContextAdp: Context,
            itemCheckListener: (MyRunningEntity, Int) -> Unit,
            itemClickListener: (MyRunningEntity, Int) -> Unit,
            selectedItems: ArrayList<Int>,
            distanceUnitFromData: String?
        ) {
            if (itemRecentHistoryBinding != null) {
                val hours = history.timeInSeconds / 3600
                val minutes = history.timeInSeconds % 3600 / 60
                val secs = history.timeInSeconds % 60
                val time = String.format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )
                Log.e("distanceUnitFromData : ", "$distanceUnitFromData")
                val distance =
                    if (distanceUnitFromData == "km") history.distance / 1000.0 else history.distance / 1609.34

                itemRecentHistoryBinding.tvDistance.text = String.format("%.2f", distance)
                itemRecentHistoryBinding.tvDistanceUnit.text = distanceUnitFromData
                itemRecentHistoryBinding.tvHistoryDate.text =
                    SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).format(history.date)
                itemRecentHistoryBinding.tvHistoryTime.text = time
                itemRecentHistoryBinding.tvSpeed.text = String.format("%.2f", history.avgSpeed)
                itemRecentHistoryBinding.tvSpeedUnit.text = "${distanceUnitFromData}/Hrs"
                itemRecentHistoryBinding.tvKcal.text =
                    "${String.format("%.2f", history.kcal)} ${
                        getString(
                            mContextAdp,
                            R.string.kcal
                        )
                    }"
                val imageBytes = history.image
                if (imageBytes != null) {
                    val bitmap = MyLocationTypeConverters().toBitmap(imageBytes)
                    itemRecentHistoryBinding.ivMap.setImageBitmap(bitmap)
                }
                itemRecentHistoryBinding.llMainHistory.setOnClickListener {
                    itemClickListener(history,adapterPosition)
                }
            } else {
                val hours = history.timeInSeconds / 3600
                val minutes = history.timeInSeconds % 3600 / 60
                val secs = history.timeInSeconds % 60
                val time = String.format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )
                Log.e("distanceUnitFromData : ", "$distanceUnitFromData")
                val distance =
                    if (distanceUnitFromData == "km") history.distance / 1000.0 else history.distance / 1609.34
                itemAllHistoryBinding!!.tvDistance.text = String.format("%.2f", distance)
                itemAllHistoryBinding.tvDistanceUnit.text = distanceUnitFromData
                itemAllHistoryBinding.tvHistoryDate.text =
                    SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).format(history.date)
                itemAllHistoryBinding.tvHistoryTime.text = time
                itemAllHistoryBinding.tvSpeed.text = String.format("%.2f", history.avgSpeed)
                itemAllHistoryBinding.tvSpeedUnit.text = "${distanceUnitFromData}/Hrs"
                itemAllHistoryBinding.tvKcal.text =
                    "${String.format("%.2f", history.kcal)} ${
                        getString(
                            mContextAdp,
                            R.string.kcal
                        )
                    }"
                val imageBytes = history.image
                if (imageBytes != null) {
                    val bitmap = MyLocationTypeConverters().toBitmap(imageBytes)
                    itemAllHistoryBinding.ivMap.setImageBitmap(bitmap)
                }

                itemAllHistoryBinding.flCheck.setOnClickListener {
                    itemCheckListener(history, adapterPosition)
                }
                itemAllHistoryBinding.llMainHistory.setOnClickListener {
                    itemClickListener(history,adapterPosition)
                }
                if (selectedItems.contains(history.id)) {
                    itemAllHistoryBinding.imgCheckUnselected.visibility = View.GONE
                    itemAllHistoryBinding.imgCheckDelete.visibility = View.VISIBLE
                } else {
                    itemAllHistoryBinding.imgCheckUnselected.visibility = View.VISIBLE
                    itemAllHistoryBinding.imgCheckDelete.visibility = View.GONE
                }
            }
        }
    }
}
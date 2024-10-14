package com.example.run_tracker_native_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.dataclass.SubscriptionData

class SubscriptionADP(private val context: Context) :
    RecyclerView.Adapter<SubscriptionADP.SubscriptionViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private var data: ArrayList<SubscriptionData> = arrayListOf()

    fun setOnClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addAllData(data: List<SubscriptionData>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
//        notifyItemRangeInserted(0, this.data.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onChangeItemSelection(index: Int) {

        for (i in data.indices) {
            data[i].isSelected = (i == index)
        }

        notifyDataSetChanged()
    }

    inner class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSubscriptionTime: AppCompatTextView = itemView.findViewById(R.id.txtSubscriptionTime)
//        val txtSubscriptionPrice: AppCompatTextView =
//            itemView.findViewById(R.id.txtSubscriptionPrice)
        val llSubscriptionItem: LinearLayout = itemView.findViewById(R.id.llSubscriptionItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_subscription, parent, false)
        return SubscriptionViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val subscriptionData = data[position]
        holder.txtSubscriptionTime.text =
            subscriptionData.formattedPrice
//        holder.txtSubscriptionPrice.text = "₹ ${subscriptionData.subPrice}"

        if (subscriptionData.isSelected) {
            holder.llSubscriptionItem.setBackgroundResource(R.drawable.ic_selected_sub)
        } else {
            holder.llSubscriptionItem.setBackgroundResource(R.drawable.ic_un_selected_sub)

        }


        holder.llSubscriptionItem.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = data.size

    interface OnItemClickListener {
        fun onItemClick(index: Int)

    }
}
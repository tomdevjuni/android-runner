package com.example.run_tracker_native_app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.fragments.SettingFragment
import java.util.Locale

class LanguagesADP(private val mContext: Context,private val onSelectedLanguage:OnSelectedLanguage): RecyclerView.Adapter<LanguagesADP.ViewHolder>(),
    Filterable {

    private val data:ArrayList<SettingFragment.LanguagesData> = ArrayList()
    private val dataSource:ArrayList<SettingFragment.LanguagesData> = ArrayList()
    private var isFilterable = false

    interface OnSelectedLanguage{
        fun selectedLanguage(pos:Int,strLanguage:String)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val convertView = LayoutInflater.from(mContext).inflate(R.layout.item_language_option, parent, false)
        return ViewHolder(convertView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(mData: List<SettingFragment.LanguagesData>) {
        try {
            this.data.clear()
            this.data.addAll(mData)

            if (isFilterable) {
                this.dataSource.clear()
                this.dataSource.addAll(mData)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeSelection(pos:Int, isNotify:Boolean)
    {
        for (i in data.indices)
        {
            data[i].isSelected = pos==i
        }
        if(isNotify) {
            onSelectedLanguage.selectedLanguage(pos,dataSource[pos].langName)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtLanguageTitle.text = dataSource[position].langName

        holder.itemView.setOnClickListener {
            changeSelection(position,true)
        }

        if(dataSource[position].isSelected){
            holder.llLanguageMain.setBackgroundColor(ContextCompat.getColor(mContext,R.color.llLanguageMain))
            holder.txtLanguageTitle.setTextColor(ContextCompat.getColor(mContext,R.color.txtLanguageTitle))
            holder.imLangLogo.setColorFilter(ContextCompat.getColor(mContext, R.color.theme), android.graphics.PorterDuff.Mode.SRC_IN)
            holder.imgDone.visibility = View.VISIBLE
            holder.viewLineLanguage.visibility = View.GONE
        }else{
            holder.llLanguageMain.setBackgroundColor(ContextCompat.getColor(mContext,R.color.llLanguageMainUn))
            holder.txtLanguageTitle.setTextColor(ContextCompat.getColor(mContext,R.color.txtLanguageTitleUn))
            holder.imLangLogo.setColorFilter(ContextCompat.getColor(mContext, R.color.txtGray), android.graphics.PorterDuff.Mode.SRC_IN)
            holder.imgDone.visibility = View.GONE
            holder.viewLineLanguage.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtLanguageTitle : TextView = itemView.findViewById(R.id.txtLanguageTitle)
        val imLangLogo :ImageView = itemView.findViewById(R.id.imLangLogo)
        val imgDone :ImageView = itemView.findViewById(R.id.imgDone)
        val viewLineLanguage :View = itemView.findViewById(R.id.viewLineLanguage)
        val llLanguageMain :LinearLayout = itemView.findViewById(R.id.llLanguageMain)
    }


    fun setFilterable(isFilterable: Boolean) {
        this.isFilterable = isFilterable
    }

    override fun getFilter(): Filter? {

        return if (isFilterable) {
            PTypeFilter()
        } else null

    }

    private inner class PTypeFilter : Filter() {

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(prefix: CharSequence, results: FilterResults) {

            dataSource.clear()
            dataSource.addAll(results.values as ArrayList<SettingFragment.LanguagesData>)
            notifyDataSetChanged()
        }

        override fun performFiltering(prefix: CharSequence?): FilterResults {

            val results = FilterResults()
            val new_res = java.util.ArrayList<SettingFragment.LanguagesData>()
            if (prefix != null && prefix.toString().isNotEmpty()) {
                for (index in data.indices) {

                    try {
                        val si = data[index]

                        if (si.langName.lowercase(Locale.ROOT).contains(prefix.toString()
                                .lowercase(Locale.ROOT))) {
                            new_res.add(si)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                results.values = new_res
                results.count = new_res.size

            } else {

                results.values = dataSource
                results.count = dataSource.size

            }

            return results
        }
    }

}
package com.example.run_tracker_native_app.fragments

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.activity.HistoryDetailActivity
import com.example.run_tracker_native_app.adapter.RecentHistoryADP
import com.example.run_tracker_native_app.databinding.FragmentHistoryBinding
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.viewmodels.HistoryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private var recentHistoryADP: RecentHistoryADP? = null
    private val historyViewModel by lazy {
        ViewModelProvider(this)[HistoryViewModel::class.java]
    }
    private var selectedItems = arrayListOf<Int>()
    private var allHistory = arrayListOf<MyRunningEntity?>()
    private var distanceUnit: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMyPrefFromDatabase()
        initRecentHistoryADP()

        binding.imgDelete.setOnClickListener {
            if (selectedItems.isNotEmpty()) openBottomSheet()
        }
        getHistoryFromDatabase()
    }

    private fun getMyPrefFromDatabase() {
        historyViewModel.myPrefLiveData.observe(viewLifecycleOwner) { myPref ->
            distanceUnit = myPref.distanceUnit
            setAllHistory()
        }
    }

    private fun getHistoryFromDatabase() {
        historyViewModel.runningListAllHistoryData.observe(viewLifecycleOwner) { historyList ->
            if (historyList != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    allHistory.clear()
                    allHistory.addAll(historyList)
                    setAllHistory()
                }, 500)

            }
        }
    }

    private fun setAllHistory() {
        if (allHistory.isNotEmpty()) {
            binding.llEmptyView.visibility = View.GONE
            binding.rvAllHistory.visibility = View.VISIBLE
        } else {
            binding.llEmptyView.visibility = View.VISIBLE
            binding.rvAllHistory.visibility = View.GONE
            binding.imdSelectAll.visibility = View.GONE
            binding.imgDelete.setColorFilter(
                ContextCompat.getColor(
                    requireContext(), R.color.whiteOp50
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        recentHistoryADP!!.setHistory(allHistory, distanceUnit)
    }

    private fun initRecentHistoryADP() {
        recentHistoryADP =
            RecentHistoryADP(
                requireContext(),
                isFromMain = false,
                itemCheckListener = { item, position ->
                    setSelectedItems(item, position)
                },
                itemClickListener = { item, _ ->
                    val intent = Intent(requireContext(), HistoryDetailActivity::class.java)
                    intent.putExtra("HistoryId", item.id)
                    startActivity(intent)
                })
        binding.rvAllHistory.setHasFixedSize(true)
        binding.rvAllHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllHistory.adapter = recentHistoryADP
        binding.imdSelectAll.visibility =
            if (selectedItems.isNotEmpty()) View.VISIBLE else View.GONE
        binding.imdSelectAll.setOnClickListener {
            if (selectedItems.size == allHistory.size) {
                recentHistoryADP!!.setUnSelectedItemsAll()
                selectedItems.clear()
                binding.imdSelectAll.visibility =
                    if (selectedItems.isNotEmpty()) View.VISIBLE else View.GONE
                binding.imgDelete.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        if (selectedItems.isNotEmpty()) R.color.white else R.color.whiteOp50
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {

                for (item in allHistory.indices) {
                    if (!selectedItems.contains(allHistory[item]!!.id!!)) {
                        selectedItems.add(allHistory[item]!!.id!!)
                        recentHistoryADP!!.setSelectedItems(allHistory[item]!!, item)
                    }
                }
            }

        }
        binding.imgDelete.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (selectedItems.isNotEmpty()) R.color.white else R.color.whiteOp50
            ), android.graphics.PorterDuff.Mode.SRC_IN
        )

    }

    private fun setSelectedItems(item: MyRunningEntity, position: Int) {
        if (selectedItems.contains(item.id!!)) {
            selectedItems.remove(item.id)
        } else {
            selectedItems.add(item.id)
        }
        recentHistoryADP!!.setSelectedItems(item, position)
        binding.imdSelectAll.visibility =
            if (selectedItems.isNotEmpty()) View.VISIBLE else View.GONE

        binding.imgDelete.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (selectedItems.isNotEmpty()) R.color.white else R.color.whiteOp50
            ), android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    private fun openBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet)
        val txtTitle = bottomSheetDialog.findViewById<TextView>(R.id.txtTitle)

        val llBottomDelete = bottomSheetDialog.findViewById<LinearLayout>(R.id.llBottomDelete)
        val llBottomKmMile = bottomSheetDialog.findViewById<LinearLayout>(R.id.llBottomKmMile)
        txtTitle!!.text = getString(R.string.delete_history)
        llBottomDelete!!.visibility = View.VISIBLE
        llBottomKmMile!!.visibility = View.GONE


        bottomSheetDialog.setOnDismissListener {

        }
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            bgTrans(dialogInterface)
        }

        bottomSheetDialog.show()
        val save = llBottomDelete.findViewById<AppCompatTextView>(R.id.txtSave)
        save.setOnClickListener {
            historyViewModel.softDeleteHistoryByIds(selectedItems)
//            selectedItems.clear()
            bottomSheetDialog.dismiss()
        }
        val cancel = llBottomDelete.findViewById<AppCompatTextView>(R.id.txtCancel)
        cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun bgTrans(dialogInterface: DialogInterface) {

        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
            ?: return
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
    }

}
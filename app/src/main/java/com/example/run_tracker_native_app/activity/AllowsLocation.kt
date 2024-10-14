package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import com.example.run_tracker_native_app.databinding.ActivityAllowsLocationBinding
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util


class AllowsLocation : BaseActivity() {

    private lateinit var binding: ActivityAllowsLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllowsLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initActions()

    }


    private fun initActions() {
        binding.btnAllow.setOnClickListener {
            Util.requestRequiredPermission(this)
        }
        binding.txtNotNow.setOnClickListener {
            finish()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constant.RequestCodePermission && Util.checkRequiredPermission(this@AllowsLocation)) {
            Util.requestRequiredBackgroundPermission(this)
        } else if (requestCode == Constant.RequestCodeBackgroundPermission && Util.checkRequiredPermission(
                this@AllowsLocation
            ) && Util.checkRequiredBackGroundPermission(this@AllowsLocation)
        ) {
            requestDozeDisable()
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestDozeDisable() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:$packageName"))
        }
        startActivity(intent)
    }

    override fun onPostResume() {
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(applicationContext, RunningActivity::class.java)
            startActivity(intent)
            finish()
        }
        super.onPostResume()
    }
}
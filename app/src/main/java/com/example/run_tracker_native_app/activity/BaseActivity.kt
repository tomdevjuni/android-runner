package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.database.CursorWindow
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.run_tracker_native_app.utils.LocaleHelper
import com.example.run_tracker_native_app.utils.Util
import com.squareup.otto.Bus
import java.lang.reflect.Field


open class BaseActivity : AppCompatActivity() {
    companion object {
        val eventBus = Bus()
    }
    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Util.transparentStatusBar(window)
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base))
    }
}
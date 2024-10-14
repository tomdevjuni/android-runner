package com.example.run_tracker_native_app.imageslider.interfaces

import com.example.run_tracker_native_app.imageslider.constants.ActionTypes



interface TouchListener {
    fun onTouched(touched: ActionTypes, position: Int)
}
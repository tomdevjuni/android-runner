package com.example.run_tracker_native_app.interfaces


interface AdsCallback {
    fun adLoadingFailed()
    fun adClose()
    fun startNextScreen()
    fun onLoaded()
}
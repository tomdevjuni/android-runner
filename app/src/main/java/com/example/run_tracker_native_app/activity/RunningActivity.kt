package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.ActivityRunningBinding
import com.example.run_tracker_native_app.locationservice.LocationTrackerService
import com.example.run_tracker_native_app.database.MyLocationEntity
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.interfaces.AdsCallback
import com.example.run_tracker_native_app.utils.CommonConstantAd
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.LocationUpdateViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.StrokeStyle
import com.google.android.gms.maps.model.StyleSpan
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ncorti.slidetoact.SlideToActView
import java.util.Locale
import kotlin.math.ln
import com.intuit.sdp.R.dimen as sdp


class RunningActivity : BaseActivity(), OnMapReadyCallback , AdsCallback {
    private lateinit var binding: ActivityRunningBinding

    private var isStartExercise = false
    private var isFullScreenView = false


    private var myMap: GoogleMap? = null

    //    private var cameraPosition: CameraPosition? = null
    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var lastKnownLocation: Location? = null

    private lateinit var workManager: WorkManager
    private var polyline: Polyline? = null
    private val points = mutableListOf<LatLng>()
    private val speeds = mutableListOf<Float>()

    private val locationUpdateViewModel by lazy {
        ViewModelProvider(this)[LocationUpdateViewModel::class.java]
    }
    private var distanceUnit: String = ""
    private lateinit var mapFragment: SupportMapFragment
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                isStartExercise = true
                binding.imgPlayPause.setImageResource(R.drawable.ic_pause)
                toggleStartScreen(true)

                val historyId = locationUpdateViewModel.getLengthOfRunningData()
                if (historyId != null) Util.setPref(
                    this,
                    Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID, historyId + 1
                ) else Util.setPref(
                    this,
                    Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID, 1
                )
                val intent = Intent(this, LocationTrackerService::class.java)
                intent.action =
                    "com.shreyanshi.run_tracker_native_app.action.TRACK_LOCATION"
                startService(intent)
                points.clear()
                speeds.clear()
                observeLivePolyline()
                observeRunningLiveData()
//                locationUpdateViewModel.lengthOfRunningListLiveData.observe(this@RunningActivity) {
//                    if (it != null) {
//                        Util.setPref(
//                            this,
//                            Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID, it + 1
//                        )
//                    } else {
//
//                    }
//
//                    locationUpdateViewModel.lengthOfRunningListLiveData.removeObservers(this@RunningActivity)
//
//
//
//                }

            }
        }


    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION") val runningServices =
            activityManager.getRunningServices(Int.MAX_VALUE)
        val isServiceRunning =
            runningServices.any { it.service.className == LocationTrackerService::class.java.name }
        Log.e("isServiceRunning.importance", isServiceRunning.toString())
        return isServiceRunning
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        workManager = WorkManager.getInstance(this@RunningActivity)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        initAction()
        mapFragment =
            (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this@RunningActivity)
        getMyPrefFromDatabase()
        if (isAppInForeground(this@RunningActivity)) {
            getDataFromExistingService()
        } else {
            val myRunningData: MyRunningEntity? = locationUpdateViewModel.getLastRunningData()
            if (myRunningData != null && myRunningData.image == null && myRunningData.pointsString == null) {
                getDataFromExistingService()
                val intent = Intent(this, LocationTrackerService::class.java)
                intent.action =
                    "com.shreyanshi.run_tracker_native_app.action.TRACK_LOCATION"
                startService(intent)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getMyPrefFromDatabase() {
        locationUpdateViewModel.myPrefLiveData.observe(this@RunningActivity) { myPref ->
            distanceUnit = myPref.distanceUnit
            binding.txtPace.text = "$distanceUnit/Hrs"
            binding.txtKm.text = distanceUnit
        }
    }

    private fun getDataFromExistingService() {
        val historyId = locationUpdateViewModel.getLengthOfRunningData() ?: return
        isStartExercise = true
        binding.imgPlayPause.setImageResource(R.drawable.ic_pause)
        toggleStartScreen(true)
        Util.setPref(
            this,
            Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID, historyId
        )
        addOldPolyline()
        observeRunningLiveData()
//        locationUpdateViewModel.lengthOfRunningListLiveData.observe(this@RunningActivity) {
//
//            Util.setPref(
//                this,
//                Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID, it
//            )
//            locationUpdateViewModel.lengthOfRunningListLiveData.removeObservers(this@RunningActivity)
//            addOldPolyline()
//            observeRunningLiveData()
//        }

    }

    private fun addOldPolyline() {
        locationUpdateViewModel.getOldLocationDataOfCurrantSession(
            Util.getPref(
                application.applicationContext,
                Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                0
            )
        )
        locationUpdateViewModel.locationListOldData!!.observe(this@RunningActivity) { locations ->
            if (myMap != null) addOldPolylineToMap(locations)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addOldPolylineToMap(locations: List<MyLocationEntity>) {
        polyline = null
        points.clear()
        speeds.clear()
        locations.forEach { location ->
            speeds.add(location.speed)
            points.add(LatLng(location.latitude, location.longitude))
//            polyline!!.points.add(LatLng(location.latitude, location.longitude))
////            polyline!!.points = points
        }
        if (polyline == null) {
            // Create a new Polyline instance for the first location update
            polyline = myMap!!.addPolyline(
                PolylineOptions()
                    .addAll(points)
                    .jointType(JointType.ROUND)
                    .startCap(
                        CustomCap(
                            BitmapDescriptorFactory.fromBitmap((generateSmallIcon(
                                this,
                                R.drawable.place1,25,25
                            ))))
                    )
                    .addSpan( StyleSpan(StrokeStyle.gradientBuilder(Color.BLUE, Color.MAGENTA).build()))
                    .geodesic(true)
                    .width(20f)
            )
            polyline!!.points.clear()
        }
        locationUpdateViewModel.locationListOldData!!.removeObservers(this@RunningActivity)

        Handler(Looper.getMainLooper()).postDelayed({
            observeLivePolyline()
        }, 2000)

    }

    private fun observeLivePolyline() {
        locationUpdateViewModel.getLiveLocationData(
            Util.getPref(
                application.applicationContext,
                Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                0
            )
        )
        locationUpdateViewModel.locationListLiveData?.observe(
            this@RunningActivity
        ) { locations ->
            locations?.let {
                Log.d(TAG, "Got $locations locations")

                val outputStringBuilder = StringBuilder("")

                outputStringBuilder.append(locations.toString() + "\n")
                if (myMap != null && Util.getPref(
                        application.applicationContext,
                        Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                        0
                    ) == locations.currantSession
                ) updatePolyline(locations)
                Log.d(TAG, "Got $outputStringBuilder locations")
            }
        }


    }

    private fun observeRunningLiveData() {
        locationUpdateViewModel.getCurrantRunningSession(
            Util.getPref(
                application.applicationContext,
                Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                0
            )
        )
        locationUpdateViewModel.runningLiveData?.observe(this@RunningActivity) { myRunningEntity ->
            myRunningEntity?.let {

                val hours = myRunningEntity.timeInSeconds / 3600
                val minutes = myRunningEntity.timeInSeconds % 3600 / 60
                val secs = myRunningEntity.timeInSeconds % 60
                val time = String.format(
                    Locale.getDefault(),
                    "%d:%02d:%02d", hours,
                    minutes, secs
                )

                val distance =
                    if (distanceUnit == "km") myRunningEntity.distance / 1000.0 else myRunningEntity.distance / 1609.34
                binding.runningCountDown.text = time
                binding.distance.text = String.format("%.2f", distance)

                binding.kcal.text = String.format("%.2f", myRunningEntity.kcal)
            }
        }
    }

    private fun initAction() {

        resetTexts()

        toggleStartScreen(false)

        binding.imgPlayPause.setOnClickListener {

            if (isStartExercise) {

                onPauseClick()
            } else {

                onPlayClick()
            }

        }

        binding.location.setOnClickListener {
            if (myMap != null) getDeviceLocation(myMap!!)
        }

        binding.satellite.setOnClickListener {
            if (myMap != null) {
                when (myMap!!.mapType) {
                    GoogleMap.MAP_TYPE_NORMAL -> myMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    GoogleMap.MAP_TYPE_SATELLITE -> myMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    GoogleMap.MAP_TYPE_TERRAIN -> myMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
                    GoogleMap.MAP_TYPE_HYBRID -> myMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
            }

        }


        binding.fullScreen.setOnClickListener {

            isFullScreenView = true
            binding.cardView.elevation = 0f
            binding.cardView.radius = 0f
            binding.llDetailsView.visibility = View.GONE
            binding.imgPlayPause.visibility = View.GONE

            val param = binding.cardView.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 0, 0, 0)

            binding.cardView.layoutParams = param


        }

        binding.lock.setOnClickListener {
            onLockClick()
        }

        binding.imgBack.setOnClickListener {

            if (isFullScreenView) {
                isFullScreenView = false
                binding.cardView.elevation = resources.getDimension(sdp._15sdp)
                binding.cardView.radius = resources.getDimension(sdp._6sdp)
                binding.llDetailsView.visibility = View.VISIBLE
                binding.imgPlayPause.visibility = View.VISIBLE

                val param = binding.cardView.layoutParams as ViewGroup.MarginLayoutParams

                val marginLeft = resources.getDimension(sdp._15sdp).toInt()
                val marginRight = resources.getDimension(sdp._15sdp).toInt()
                val marginTop = resources.getDimension(sdp._15sdp).toInt()
                val marginBottom = resources.getDimension(sdp._60sdp).toInt()

                param.setMargins(marginLeft, marginTop, marginRight, marginBottom)
                binding.cardView.layoutParams = param
            } else {

                finish()
            }

        }

    }

    private fun resetTexts() {
        binding.runningCountDown.text =
            ContextCompat.getString(this, R.string._00_00_00)
        binding.distance.text = ContextCompat.getString(this, R.string._0__00)
        binding.pace.text = ContextCompat.getString(this, R.string._00_00)
        binding.kcal.text = ContextCompat.getString(this, R.string._0__00)
    }


    private fun toggleStartScreen(isStart: Boolean) {
        binding.location.visibility = if (isStart) View.VISIBLE else View.GONE
        binding.satellite.visibility = if (isStart) View.VISIBLE else View.GONE
        binding.fullScreen.visibility = if (isStart) View.VISIBLE else View.GONE
        binding.lock.visibility = if (isStart) View.VISIBLE else View.GONE
    }


    private fun onLockClick() {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_lock)
        dialog.setCancelable(false)

        val swipeToUnlock: SlideToActView = dialog.findViewById(R.id.swipeToUnlock)
        swipeToUnlock.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun onPlayClick() {
        val intent = Intent(this, CountDownActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun onPauseClick() {
        LocationTrackerService.getInstance().onClickPause()

        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pause_excercise)
        dialog.setCancelable(false)

        val restart: CardView = dialog.findViewById(R.id.llCardViewRestart)
        val resume: CardView = dialog.findViewById(R.id.llCardViewResume)
        val stop: CardView = dialog.findViewById(R.id.llCardViewStop)


        restart.setOnClickListener {
            onRestartButtonClick(dialog)
        }

        resume.setOnClickListener {
            onResumeButtonClick(dialog)
        }

        stop.setOnClickListener {
            onStopButtonClick(dialog)
        }


        dialog.show()
    }


    private fun onRestartButtonClick(dialog: Dialog) {
        resetTexts()
        isStartExercise = false
        binding.imgPlayPause.setImageResource(R.drawable.ic_play)
        toggleStartScreen(false)

        LocationTrackerService.getInstance().onClickReset(
            Util.getPref(
                this@RunningActivity,
                Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                0
            )
        )
        stopService(Intent(this, LocationTrackerService::class.java))
        speeds.clear()
        points.clear()
        polyline!!.remove()
        polyline = null
        dialog.dismiss()

    }

    private fun onResumeButtonClick(dialog: Dialog) {
        LocationTrackerService.getInstance().onClickResume()
        dialog.dismiss()
    }

    private fun onStopButtonClick(dialog: Dialog) {
        stopService(Intent(this, LocationTrackerService::class.java))
        takeSSOfMapAndStoreInData()
        val bottomSheetDialog = BottomSheetDialog(this, R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_finish_running)

        val icClose: AppCompatImageView? = bottomSheetDialog.findViewById(R.id.icClose)
        val btnFinish: AppCompatTextView? = bottomSheetDialog.findViewById(R.id.btnFinish)

        icClose!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btnFinish!!.setOnClickListener {

            isStartExercise = false
            binding.imgPlayPause.setImageResource(R.drawable.ic_play)
            toggleStartScreen(false)
//            locationUpdateViewModel.stopLocationUpdates()
            bottomSheetDialog.dismiss()
            if(!Util.isPurchased(this)) checkAd()

        }

        bottomSheetDialog.setOnShowListener { dialogInterface ->
            bgTrans(dialogInterface)
        }

        bottomSheetDialog.show()

        dialog.dismiss()

    }

    private fun takeSSOfMapAndStoreInData() {

        if (points.isEmpty()) {
            if (lastKnownLocation != null)
                points.add(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
            else points.add(defaultLocation)

        }
        myMap!!.addMarker(
            MarkerOptions()
                .position(points.last())
                .icon(BitmapDescriptorFactory.fromBitmap((generateSmallIcon(
                    this,
                    R.drawable.place,120,120
                ))))
                .title("End Point")
        )
        val pair: Pair<LatLng?, Int?>? = getCenterWithZoomLevel(*points.toTypedArray())
        Log.e("", "")
        val cameraUpdate = pair?.first?.let {
            pair.second?.let { it1 ->

                Log.e("", "$it : ${it1.toFloat()}")
                CameraUpdateFactory.newLatLngZoom(
                    it,
                    it1.toFloat()
                )
            }
        }

        if (cameraUpdate != null) {
            myMap!!.animateCamera(cameraUpdate)
            val timer = object : CountDownTimer(1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    val pointsString = points.joinToString(";") { "${it.latitude},${it.longitude}" }
                    myMap!!.snapshot { bitmap ->
                        if (speeds.isNotEmpty()) locationUpdateViewModel.updateAvgSpeed(
                            Util.getPref(
                                this@RunningActivity,
                                Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                                0
                            ), speeds.average()
                                .toFloat(), bitmap, pointsString
                        ) else {
                            speeds.add(0.0F)
                            locationUpdateViewModel.updateAvgSpeed(
                                Util.getPref(
                                    this@RunningActivity,
                                    Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
                                    0
                                ), speeds.average()
                                    .toFloat(), bitmap, pointsString
                            )
                        }
                        Util.setPref(
                            this@RunningActivity,
                            Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID, 0
                        )
                    }
                }
            }
            timer.start()

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

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap!!.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json))
        getDeviceLocation(myMap!!)
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation(myMap: GoogleMap) {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
//
                        myMap.isMyLocationEnabled = true
                        myMap.uiSettings.isMyLocationButtonEnabled = false
                        val location = CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude
                            ), DEFAULT_ZOOM.toFloat()
                        )
//                        updatePolyline(
//                            MyLocationEntity(
//                                latitude = lastKnownLocation!!.latitude,
//                                longitude = lastKnownLocation!!.longitude,
//                                foreground = true,
//                                date = Date(lastKnownLocation!!.time)
//                            )
//                        )
                        myMap.animateCamera(location)
                    }
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)
                    myMap.moveCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                    )
                    myMap.uiSettings.isMyLocationButtonEnabled = false
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun updatePolyline(location: MyLocationEntity) {
        points.add(LatLng(location.latitude, location.longitude))
        speeds.add(location.speed)
        if (polyline == null) {
            // Create a new Polyline instance for the first location update
            polyline = myMap!!.addPolyline(
                PolylineOptions()
                    .addAll(points)
                    .jointType(JointType.ROUND)
                    .startCap(
                        CustomCap(
                            BitmapDescriptorFactory.fromBitmap((generateSmallIcon(
                        this,
                        R.drawable.place1,25,25
                    ))))
                    )
                    .addSpan( StyleSpan(StrokeStyle.gradientBuilder(Color.BLUE, Color.MAGENTA).build()))
                    .geodesic(true)
                    .width(20f)
            )
        }

        // Update the polyline's points with the previous and current locations

//        if (lastKnownLocation != null) {
//            points.add(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
//        }
//        polyline!!.points.add(LatLng(location.latitude, location.longitude))
        for (loc in points) {
            Log.e("Points", "lat : ${loc.latitude} Long :  ${loc.longitude}")
        }
        polyline!!.points = points

        // Move the camera to focus on the current location, optionally with animation
        myMap!!.isMyLocationEnabled = true
        myMap!!.uiSettings.isMyLocationButtonEnabled = false
        myMap!!.isBuildingsEnabled = true
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude),
            DEFAULT_ZOOM.toFloat()
        )

//        val cameraPosition = CameraPosition.Builder()
//            .target(LatLng(location.latitude, location.longitude)) // Set your desired location
//            .zoom(DEFAULT_ZOOM.toFloat()) // Zoom level (higher shows more detail)
//            .tilt(70.0f) // Tilt angle (higher for steeper tilt)
//            .build()
//        myMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        myMap!!.animateCamera(cameraUpdate)
        binding.pace.text = String.format("%.2f", location.speed)

        // Update the previous location for the next update
//        lastKnownLocation = Location
    }

    private fun getCenterWithZoomLevel(vararg l: LatLng?): Pair<LatLng?, Int?>? {
        var max = 0f
        if (l.isEmpty()) {
            return null
        }
        val b = LatLngBounds.Builder()
        for (count in l.indices) {
            if (l[count] == null) {
                continue
            }
            b.include(l[count]!!)
        }
        val center = b.build().center
        var distance: Float
        for (count in l.indices) {
            if (l[count] == null) {
                continue
            }
//            distance = center.distanceTo( l[count])
            distance = distanceInMeter(
                center.latitude,
                center.longitude,
                l[count]!!.latitude,
                l[count]!!.longitude
            )
            if (distance > max) {
                max = distance
            }
        }
        val scale = (max / 500).toDouble()
        val zoom = if (scale == 0.0) 15 else (16 - ln(scale) / ln(2.0)).toInt()
        return Pair(center, zoom)
    }
    private fun generateSmallIcon(context: Context, @DrawableRes vectorResId: Int, width:Int, height:Int): Bitmap {

        val bitmap = BitmapFactory.decodeResource(context.resources, vectorResId)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun distanceInMeter(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLon, endLat, endLon, results)
        return results[0]
    }


    private fun checkAd() {
        if (Util.getPref(this, Constant.STATUS_ENABLE_DISABLE, "")
                .equals(Constant.ENABLE)
        ) {
            if (Util.getPref(this, Constant.AD_TYPE_FB_GOOGLE, "")
                    .equals(Constant.AD_GOOGLE)
            ) {
                CommonConstantAd.googlebeforloadAd(this)
            } else if (Util.getPref(this, Constant.AD_TYPE_FB_GOOGLE, "")
                    .equals(Constant.AD_FACEBOOK)
            ) {
                CommonConstantAd.facebookbeforeloadFullAd(this)
            }
            if (Util.getPref(this, Constant.STATUS_ENABLE_DISABLE, "")
                    .equals(Constant.ENABLE)
            ) {
                when {
                    Util.getPref(
                        this@RunningActivity,
                        Constant.AD_TYPE_FB_GOOGLE,
                        ""
                    ).equals(Constant.AD_GOOGLE) -> {
                        CommonConstantAd.showInterstitialAdsGoogle(
                            this@RunningActivity,
                            this@RunningActivity,
                        )
                    }
                    Util.getPref(
                        this@RunningActivity,
                        Constant.AD_TYPE_FB_GOOGLE,
                        ""
                    ).equals(Constant.AD_FACEBOOK) -> {
                        CommonConstantAd.showInterstitialAdsFacebook(this@RunningActivity,)
                    }
                    else -> {

                    }
                }

            } else {
finish()
            }
        } else {
            finish()
        }
    }



    override fun adLoadingFailed() {
        finish()
    }

    override fun adClose() {
        finish()
    }

    override fun startNextScreen() {
        finish()
    }

    override fun onLoaded() {

    }


    override fun onStop() {
        super.onStop()
    }

    companion object {
        private val TAG = RunningActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 18
    }
}
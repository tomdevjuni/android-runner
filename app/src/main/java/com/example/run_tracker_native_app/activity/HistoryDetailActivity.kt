package com.example.run_tracker_native_app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.run_tracker_native_app.R
import com.example.run_tracker_native_app.databinding.ActivityHistoryDetailBinding
import com.example.run_tracker_native_app.utils.Util
import com.example.run_tracker_native_app.viewmodels.HistoryViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ln


class HistoryDetailActivity : BaseActivity(), OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var distanceUnit: String = ""
    private lateinit var mapFragment: SupportMapFragment
    private var myMap: GoogleMap? = null
    private var historyId: Int? = null
    private var polyline: Polyline? = null
    private val historyViewModel by lazy {
        ViewModelProvider(this)[HistoryViewModel::class.java]
    }
    private var points = emptyList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MapsInitializer.initialize(this,MapsInitializer.Renderer.LATEST, this)
        historyId = intent.getIntExtra("HistoryId", 0)
        mapFragment =
            (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this@HistoryDetailActivity)
        getMyPrefFromDatabase()
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.ivShare.setOnClickListener {
            getBitmapFromView(binding.root, this@HistoryDetailActivity) { bitmap ->
                try {
                    val cachePath: File = File(this@HistoryDetailActivity.cacheDir, "images")
                    cachePath.mkdirs() // don't forget to make the directory
                    val stream =
                        FileOutputStream("$cachePath/${historyId}image.png") // overwrites this image every time
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val imagePath: File = File(this@HistoryDetailActivity.cacheDir, "images")
                val newFile = File(imagePath, "${historyId}image.png")
                val contentUri =
                    FileProvider.getUriForFile(
                        this@HistoryDetailActivity,
                        "com.shreyanshi.run_tracker_native_app.fileprovider",
                        newFile
                    )

                if (contentUri != null) {
                    val shareIntent = Intent()
                    shareIntent.setAction(Intent.ACTION_SEND)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                    shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    startActivity(Intent.createChooser(shareIntent, "Choose an app"))
                }

            }
        }
        Util.loadBannerAd(binding.llAdView, binding.llAdViewFacebook, this)
    }

    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + view.width,
                        locationOfViewInWindow[1] + view.height
                    ),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        }
                        // possible to handle other result codes ...
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getMyPrefFromDatabase() {
        historyViewModel.myPrefLiveData.observe(this@HistoryDetailActivity) { myPref ->
            distanceUnit = myPref.distanceUnit
            binding.tvSpeedUnit.text = "Hrs/$distanceUnit"
            binding.tvDistanceUnit.text = distanceUnit

            if (historyId != 0) {
                getHistoryDataById()
            }
        }
    }

    private fun getHistoryDataById() {
        historyViewModel.getHistoryByIds(historyId!!)
        historyViewModel.runningHistoryData.observe(this@HistoryDetailActivity) { history ->
            if (history == null) return@observe

            binding.tvSpeed.text = String.format("%.2f", history.avgSpeed)
            val distance =
                if (distanceUnit == "km") history.distance / 1000.0 else history.distance / 1609.34
            binding.tvTotalDistance.text = String.format("%.2f", distance)
            binding.tvTotalKcal.text = String.format("%.2f", history.kcal)
            val hours = history.timeInSeconds / 3600
            val minutes = history.timeInSeconds % 3600 / 60
            val secs = history.timeInSeconds % 60
            val time = String.format(
                Locale.getDefault(),
                "%d:%02d:%02d", hours,
                minutes, secs
            )
            binding.tvTime.text = time
            binding.tvDateTime.text =
                SimpleDateFormat("dd MMMM yyyy, HH:mm a", Locale.ENGLISH).format(history.date)
            points = history.pointsString!!.split(";").map {
                val (latitude, longitude) = it.split(",")
                LatLng(latitude.toDouble(), longitude.toDouble())
            }
            setDataOnMap(points)
        }
    }
    private fun generateSmallIcon(context: Context, @DrawableRes vectorResId: Int,width:Int,height:Int): Bitmap {

        val bitmap = BitmapFactory.decodeResource(context.resources, vectorResId)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun setDataOnMap(points: List<LatLng>) {
        if (points.isNotEmpty() && myMap != null) {
            if (polyline == null) {
                polyline = myMap!!.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .jointType(JointType.ROUND)
                        .startCap(CustomCap(BitmapDescriptorFactory.fromBitmap((generateSmallIcon(
                            this,
                            R.drawable.place1,25,25
                        )))))
                        .addSpan( StyleSpan(StrokeStyle.gradientBuilder(Color.BLUE, Color.MAGENTA).build()))
                        .geodesic(true)
                        .width(20f)
                )
            }
//            myMap!!.addMarker(
//                MarkerOptions()
//                    .position(points[0])
//                    .icon()
//                    .title("Starting Point")
//            )
            myMap!!.addMarker(
                MarkerOptions()
                    .position(points.last())
                    .icon(BitmapDescriptorFactory.fromBitmap((generateSmallIcon(
                        this,
                        R.drawable.place,120,120
                    ))))
                    .title("End Point")
            )
            val pair: Pair<LatLng?, Int?>? = getCenterWithZoomLevel(*this.points.toTypedArray())
            Log.e("", "")
            val cameraUpdate = pair?.first?.let {
                pair.second?.let { it1 ->

                    Log.e("", "${it} : ${it1.toFloat()}")
                    CameraUpdateFactory.newLatLngZoom(
                        it,
                        it1.toFloat()
                    )
                }
            }
            if (cameraUpdate != null) {

                myMap!!.animateCamera(cameraUpdate)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap!!.isBuildingsEnabled = false
        myMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json))
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
        var distance: Float = 0.0f
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
        val zoom = (16 - ln(scale) / ln(2.0)).toInt()
        return Pair(center, zoom)
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {
    }
}
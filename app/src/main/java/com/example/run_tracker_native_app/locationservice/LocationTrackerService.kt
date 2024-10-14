package com.example.run_tracker_native_app.locationservice

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.PermissionChecker
import com.example.run_tracker_native_app.repo.LocationRepository
import com.example.run_tracker_native_app.database.helpers.MyLocationDatabase
import com.example.run_tracker_native_app.database.MyLocationEntity
import com.example.run_tracker_native_app.database.MyRunningEntity
import com.example.run_tracker_native_app.utils.Constant
import com.example.run_tracker_native_app.utils.Util
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.timer


class LocationTrackerService : Service()  {

    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationManager: NotificationManager
    private val notificationId = 12345
//    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mLocationRequest: LocationRequest
    private val minTime: Long = 1 * 3 * 1000L // 3 seconds minimum time between updates
    private val minDistance: Float = 3f // meters minimum distance between updates
    private var myLocationDatabase: MyLocationDatabase? = null

    private var executor: ExecutorService? = null

    private var seconds = 0

    val handler = Handler(Looper.getMainLooper())
    private var newLocation: Location? = null
    private var lastLocation: Location? = null
    private var totalDistanceInMeters: Float = 0.0f
    private var totalKCAL: Float = 0.0f
    var id: Int = 0
    private lateinit var timer: CountDownTimer


    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude())
                if (Constant.IS_SERVICE_RUNNING_BOOL) {
                    newLocation = location
                    Log.e(
                        "onLocationChanged : ",
                        "newLocation :  ${newLocation.toString()} || lastLocation :  ${lastLocation.toString()}"
                    )

                    if (lastLocation != null) {
                        totalDistanceInMeters += lastLocation!!.distanceTo(newLocation!!)
                        totalKCAL = totalDistanceInMeters * 0.052f
                        lastLocation = location
                    } else {
                        lastLocation = location
                    }
                    Log.e("onLocationResult", totalDistanceInMeters.toString())

                    val myLocationEntity = MyLocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        currantSession = id,
                        speed = location.speed,
                        foreground = isAppInForeground(this@LocationTrackerService),
                        date = Date(location.time)
                    )
                    LocationRepository.getInstance(this@LocationTrackerService, Executors.newSingleThreadExecutor())
                        .addLocation(myLocationEntity)
                    if (this@LocationTrackerService::timer.isInitialized) timer.cancel()
                    timer = object : CountDownTimer(5000, 5000) {
                        override fun onTick(millisUntilFinished: Long) {

                        }

                        override fun onFinish() {
                            val myExistingLocationEntity = MyLocationEntity(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                currantSession = id,
                                speed = 0.0F,
                                foreground = isAppInForeground(this@LocationTrackerService),
                                date = Date(location.time)
                            )
                            if (Constant.IS_SERVICE_RUNNING_BOOL) LocationRepository.getInstance(
                                this@LocationTrackerService,
                                Executors.newSingleThreadExecutor()
                            ).addLocation(myExistingLocationEntity)
                        }
                    }
                    timer.start()
                }

            }
        }
    }
    private fun onClickStart() {
        Constant.IS_SERVICE_RUNNING_BOOL = true

    }

    fun onClickPause() {
        Constant.IS_SERVICE_RUNNING_BOOL = false

    }

    fun onClickResume() {
        Constant.IS_SERVICE_RUNNING_BOOL = true
    }


    fun onClickReset(id: Int) {
        if (this::timer.isInitialized) timer.cancel()
        Constant.IS_SERVICE_RUNNING_BOOL = false
        seconds = 0
        LocationRepository.getInstance(this, Executors.newSingleThreadExecutor())
            .removeAllLocationsBySessionId(id)

        LocationRepository.getInstance(this, Executors.newSingleThreadExecutor())
            .removeCurrantSession(id)
    }

    private fun runTimer() {
        onClickStart()
        val date = Date()

        handler.post(object : Runnable {
            override fun run() {
                // If running is true, increment the
                // seconds variable.
                if (Constant.IS_SERVICE_RUNNING_BOOL) {
                    seconds++

                    val myRunningEntity = MyRunningEntity(
                        id = id,
                        timeInSeconds = seconds,
                        distance = totalDistanceInMeters,
                        avgSpeed = 0.0,
                        kcal = totalKCAL,
                        date = date
                    )
                    LocationRepository.getInstance(
                        this@LocationTrackerService,
                        Executors.newSingleThreadExecutor()
                    ).updateCurrantRunning(myRunningEntity)
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onCreate() {
        super.onCreate()
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        myLocationDatabase = MyLocationDatabase.getInstance(this)
        executor = Executors.newSingleThreadExecutor()
        createNotificationChannel()

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        id = Util.getPref(
            this@LocationTrackerService,
            Constant.PREFERENCE_CURRANT_RUNNING_SESSION_ID,
            0
        )
        val myRunningEntity = LocationRepository.getInstance(
            this@LocationTrackerService,
            Executors.newSingleThreadExecutor()
        ).getRunningDataById(id)
        if (myRunningEntity != null) {
            totalDistanceInMeters = myRunningEntity.distance
            totalKCAL = myRunningEntity.kcal
            seconds = myRunningEntity.timeInSeconds
        }
        requestLocationUpdates()
        showForegroundNotification("App is getting location in background...")

//        showNotification("App is getting location in background...")
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channelName = "Location Tracker"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH
        notificationChannel = NotificationChannel(channelName, channelName, channelImportance)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (checkPermissions()) {
            mLocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, minTime)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(minTime)
                .setMaxUpdateDelayMillis(minTime)
                .build()

            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())

//            locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                minTime,
//                minDistance,
//                this,
//            )
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                locationManager.requestLocationUpdates(
//                    LocationManager.FUSED_PROVIDER,
//                    minTime,
//                    minDistance,
//                    this,
//                )
//            } else {
//                locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER,
//                    minTime,
//                    minDistance,
//                    this,
//                )
//            }
            runTimer()
        } else {
            // Handle permission request if needed
        }
    }

    /*override fun onLocationChanged(location: Location) {
        if (Constant.IS_SERVICE_RUNNING_BOOL) {
            newLocation = location
            Log.e(
                "onLocationChanged : ",
                "newLocation :  ${newLocation.toString()} || lastLocation :  ${lastLocation.toString()}"
            )

            if (lastLocation != null) {
                totalDistanceInMeters += lastLocation!!.distanceTo(newLocation!!)
                totalKCAL = totalDistanceInMeters * 0.052f
                lastLocation = location
            } else {
                lastLocation = location
            }
            Log.e("onLocationResult", totalDistanceInMeters.toString())

            val myLocationEntity = MyLocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
                currantSession = id,
                speed = location.speed,
                foreground = isAppInForeground(this),
                date = Date(location.time)
            )
            LocationRepository.getInstance(this, Executors.newSingleThreadExecutor())
                .addLocation(myLocationEntity)
            if (this::timer.isInitialized) timer.cancel()
            timer = object : CountDownTimer(5000, 5000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    val myExistingLocationEntity = MyLocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        currantSession = id,
                        speed = 0.0F,
                        foreground = isAppInForeground(this@LocationTrackerService),
                        date = Date(location.time)
                    )
                    if (Constant.IS_SERVICE_RUNNING_BOOL) LocationRepository.getInstance(
                        this@LocationTrackerService,
                        Executors.newSingleThreadExecutor()
                    ).addLocation(myExistingLocationEntity)
                }
            }
            timer.start()
        }

    }*/

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        appProcesses.forEach { appProcess ->
            if (appProcess.importance ==
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == context.packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun showForegroundNotification(contentText: String) {
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannel.id)
            .setSmallIcon(com.example.run_tracker_native_app.R.drawable.ic_home_location)
            .setContentTitle("Location tracking active")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)

        startForeground(notificationId, notificationBuilder.build())
    }


    private fun showNotification(contentText: String) {
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannel.id)
            .setSmallIcon(com.example.run_tracker_native_app.R.drawable.ic_home_location)
            .setContentTitle("Location tracking active")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true) // Makes notification sticky and unremovable
            .setAutoCancel(false) // Prevents notification from disappearing

        with(notificationManager) {
            notify(notificationId, notificationBuilder.build())
        }
    }

    override fun onDestroy() {
//        locationManager.removeUpdates(this)
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
        notificationManager.cancel(notificationId)
        handler.removeCallbacksAndMessages(null)
        Log.e("onDestroy : ", "onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun checkPermissions(): Boolean {
        val fineLocationGranted = PermissionChecker.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        val coarseLocationGranted = PermissionChecker.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        return fineLocationGranted && coarseLocationGranted
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: LocationTrackerService? = null

        fun getInstance(): LocationTrackerService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationTrackerService().also { INSTANCE = it }
            }
        }
    }
}
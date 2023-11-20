package com.example.teeya_li.service

import android.app.NotificationManager
import android.app.Service
import android.location.LocationListener
import android.location.LocationManager
import com.example.teeya_li.database.HistoryViewModel
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.example.myruns.R
import com.example.teeya_li.MainActivity
import com.example.teeya_li.database.HistoryDatabase
import com.example.teeya_li.database.HistoryDatabaseDao
import com.example.teeya_li.database.HistoryEntry
import com.example.teeya_li.database.HistoryRepository
import com.example.teeya_li.database.HistoryViewModelFactory
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

private val PERMISSION_REQUEST_CODE = 0

class TrackingService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val PENDINGINTENT_REQUEST_CODE = 0
    private val NOTIFY_ID = 11
    private val CHANNEL_ID = "notification channel"
    private lateinit var notificationManager: NotificationManager
    private var locationList: ArrayList<LatLng> = ArrayList()

    //variables for calculations
    private var avgSpeed = 0.0;
    private var curSpeed = 0.0;
    private var climb = 0.0;
    private var calorieTotal = 0.0;
    private var distance = 0.0;
    private var duration = " "

    companion object {
        var exerciseEntryViewModel: HistoryViewModel? = null
        val STOP_SERVICE_ACTION = "stop service action"
        const val LOCATION_UPDATE_ACTION = "com.example.teeya_li.service.LOCATION_UPDATE"
        val SAVE_DATA_ACTION = "save entry"
        val SEND_COUNT_ACTION = "grab duration"
    }
    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        initLocationManager()

        showNotification()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val latLng: LatLng? = intent.getParcelableExtra("latLng")
        if (latLng != null) { //save the new latLng data from gps
            //locationList.add(latLng)
        }

        //update variables with the new lat/long info
        avgSpeed = calculateAverageSpeed()
        curSpeed = calculateCurrentSpeed()
        climb = calculateClimb()
        calorieTotal = calculateCalorieTotal()
        distance = calculateDistance()

        //pass these variables back to the map activity for display
        val intent = Intent(LOCATION_UPDATE_ACTION)
        intent.putExtra("avgSpeed", avgSpeed)
        intent.putExtra("curSpeed", curSpeed)
        intent.putExtra("climb", climb)
        intent.putExtra("calorieTotal", calorieTotal)
        intent.putExtra("distance", distance)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        if (intent.action == STOP_SERVICE_ACTION) {
            // Clear or reset the data in locationList, the user hit cancel
            locationList.clear()
            notificationManager.cancel(NOTIFY_ID)
            stopSelf()
        }

        if (intent.action == SEND_COUNT_ACTION){
            duration = intent.getIntExtra("totalCount", 0).toString()

        }

        if(intent.action == SAVE_DATA_ACTION){

            val locationListIntent = Intent(LOCATION_UPDATE_ACTION)
            locationListIntent.putExtra("locationList", locationList)
            LocalBroadcastManager.getInstance(this).sendBroadcast(locationListIntent)

        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null)
                onLocationChanged(location)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        // Broadcast or use a callback to send location data to the activity
        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        sendBroadcast(intent)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // Called when the status of the GPS provider changes
    }
    fun showNotification() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        // Create a PendingIntent with the main activity intent
        val pendingIntent = PendingIntent.getActivity(
            this, PENDINGINTENT_REQUEST_CODE,
            mainActivityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )

        //Notification details
        notificationBuilder.setContentTitle("MyRuns")
        notificationBuilder.setContentText("Recording your path now")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setSmallIcon(R.drawable.baseline_directions_run_24)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH

        val notification = notificationBuilder.build()

        //Ensure version correctness
        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "channel name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFY_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }

    //following functions are for calculation purposes
    fun calculateAverageSpeed(): Double {
        if (locationList.size < 2) {
            return 0.0
        }

        val totalDistance = calculateTotalDistance()

        return if (totalDistance > 0) {
            totalDistance / locationList.size
        } else {
            0.0
        }
    }

    fun calculateCurrentSpeed(): Double {
        if (locationList.size < 2) {
            return 0.0
        }

        val lastLatLng = locationList[locationList.size - 1]
        val secondLastLatLng = locationList[locationList.size - 2]

        val distance = calculateDistanceBetween(lastLatLng, secondLastLatLng)

        return if (distance > 0) {
            distance
        } else {
            0.0
        }
    }

    fun calculateClimb(): Double {
        if (locationList.size < 2) {
            return 0.0
        }

        val lastLatLng = locationList[locationList.size - 1]
        val secondLastLatLng = locationList[locationList.size - 2]

        val climb = lastLatLng.latitude - secondLastLatLng.latitude
        return if (climb > 0) climb else 0.0
    }

    fun calculateCalorieTotal(): Double {
        var distance = calculateDistance()
        return distance * 0.01
    }

    fun calculateDistance(): Double {
        if (locationList.size < 2) {
            return 0.0
        }

        return calculateTotalDistance()
    }

    private fun calculateTotalDistance(): Double {
        var totalDistance = 0.0

        for (i in 1 until locationList.size) {
            totalDistance += calculateDistanceBetween(locationList[i - 1], locationList[i])
        }

        return totalDistance
    }

    private fun calculateDistanceBetween(point1: LatLng, point2: LatLng): Double {
        val lat1 = point1.latitude
        val lon1 = point1.longitude
        val lat2 = point2.latitude
        val lon2 = point2.longitude

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        return dLat + dLon
    }
}
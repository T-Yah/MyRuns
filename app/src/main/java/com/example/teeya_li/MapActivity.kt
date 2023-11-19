package com.example.teeya_li

import android.graphics.Color
import android.location.LocationListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myruns.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.example.teeya_li.database.HistoryDatabase
import com.example.teeya_li.database.HistoryDatabaseDao
import com.example.teeya_li.database.HistoryEntry
import com.example.teeya_li.database.HistoryRepository
import com.example.teeya_li.database.HistoryViewModel
import com.example.teeya_li.database.HistoryViewModelFactory
import com.example.teeya_li.service.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Calendar

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private var activity_type_options = arrayOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
        "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical",
        "Other")

    //grab xml elements
    private lateinit var typeTextView: TextView
    private lateinit var avgSpeedTextView: TextView
    private lateinit var curSpeedTextView: TextView
    private lateinit var climbTextView: TextView
    private lateinit var calorieTextView: TextView
    private lateinit var distanceTextView: TextView

    //grab unit preference
    lateinit var unitPreference: String
    var activityPosition = 0;

    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>

    //to calculate duration
    private var count = 0
    private val handler = Handler()

    private lateinit var database: HistoryDatabase
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var repository: HistoryRepository
    private lateinit var viewModelFactory: HistoryViewModelFactory
    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var latlongList: ArrayList<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //define database objects
        database = HistoryDatabase.getInstance(this)
        databaseDao = database.historyDatabaseDao
        repository = HistoryRepository(databaseDao)
        viewModelFactory = HistoryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory).get(
            HistoryViewModel::class.java)

        // Initialize TextViews
        typeTextView = findViewById(R.id.type)
        avgSpeedTextView = findViewById(R.id.avgSpeed)
        curSpeedTextView = findViewById(R.id.curSpeed)
        climbTextView = findViewById(R.id.climb)
        calorieTextView = findViewById(R.id.calorie)
        distanceTextView = findViewById(R.id.distance)
        activityPosition = intent.getIntExtra("activity_position", -1)
        val selectedActivityType = activity_type_options[activityPosition]
        typeTextView.text = "Type: $selectedActivityType"

        // Access unitPreference value
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        unitPreference = sharedPreferences.getString("unitPreference", "metric").toString()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        startCounting()

        var cancelBtn = findViewById<Button>(R.id.cancelButton)
        cancelBtn.setOnClickListener{
            val intent = Intent()
            intent.action = TrackingService.STOP_SERVICE_ACTION
            sendBroadcast(intent)
            finish()
        }
        val saveBtn = findViewById<Button>(R.id.saveButton)
        saveBtn.setOnClickListener{
            stopCounting()
            val intent = Intent()
            intent.action = TrackingService.SAVE_DATA_ACTION
            intent.putExtra("activityType", activityPosition)
            sendBroadcast(intent)
            intent.action = TrackingService.STOP_SERVICE_ACTION
            sendBroadcast(intent)
            saveToDatabase()
            finish()
        }
    }

    private fun saveToDatabase() {
        val entry = HistoryEntry(
            //create a new history Entry
            dateTime = Calendar.getInstance(),
            duration = count.toDouble(),
            distance = distanceTextView.text.toString().toDouble(),
            calorie = calorieTextView.text.toString().toDouble(),
            heartRate = 0.0,
            comment = " ",
            avgPace = curSpeedTextView.text.toString().toDouble(),
            avgSpeed = avgSpeedTextView.text.toString().toDouble(),
            climb = climbTextView.text.toString().toDouble(),
            locationList = latlongList,
            activityType = activityPosition,
            inputType = 1
        )
        historyViewModel.insert(entry)

    }

    private fun startCounting() {
        // Start the initial count
        handler.postDelayed({
            count++
            handler.postDelayed({ startCounting() }, 1000)
        }, 1000)
    }

    private fun stopCounting() {
        // Remove the messages to stop counting
        handler.removeCallbacksAndMessages(null)

        val intent = Intent(this, TrackingService::class.java)
        intent.action = TrackingService.SEND_COUNT_ACTION
        intent.putExtra("totalCount", count)
        startService(intent)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null) {
            locationManager.removeUpdates(this)
        }
    }

    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null)
                onLocationChanged(location)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        println("debug: onlocationchanged() ${location.latitude} ${location.longitude}")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)

        val intent = Intent(this, TrackingService::class.java)
        intent.action = TrackingService.LOCATION_UPDATE_ACTION
        intent.putExtra("latLng", latLng) // Pass latLng as an extra
        startService(intent)

        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            mMap.addMarker(markerOptions)
            polylineOptions.add(latLng)
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
        for (i in polylines.indices) polylines[i].remove()
        polylineOptions.points.clear()
    }

    override fun onMapLongClick(latLng: LatLng) {
        markerOptions.position(latLng!!)
        mMap.addMarker(markerOptions)
        polylineOptions.add(latLng)
        polylines.add(mMap.addPolyline(polylineOptions))
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        else
            initLocationManager()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }

    //function to display the data calculated from tracking service
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == TrackingService.LOCATION_UPDATE_ACTION) {
                val avgSpeedReturn = intent.getDoubleExtra("avgSpeed", 0.0)
                val curSpeedReturn = intent.getDoubleExtra("curSpeed", 0.0)
                val climbReturn = intent.getDoubleExtra("climb", 0.0)
                val calorieTotalReturn = intent.getDoubleExtra("calorieTotal", 0.0)
                val distanceReturn = intent.getDoubleExtra("distance", 0.0)

                // Update your UI or do something with the received data
                avgSpeedTextView.text = "Avg soeed: " + avgSpeedReturn + " $unitPreference"
                curSpeedTextView.text = "Cur soeed: " + curSpeedReturn + " $unitPreference"
                climbTextView.text = "Climb: " + climbReturn + " $unitPreference"
                calorieTextView.text = "Calorie: " + calorieTotalReturn
                distanceTextView.text = "Distance: " + distanceReturn + " $unitPreference"

            }
            else if (intent != null) {
                if (intent.action == TrackingService.LOCATION_UPDATE_ACTION) {
                    // Receive the updated locationList
                    val updatedLocationList = intent.getParcelableArrayListExtra<LatLng>("locationList")
                    if (updatedLocationList != null) {
                        // Do something with the updated locationList in your MapActivity
                        latlongList = updatedLocationList
                    }
                }
            }
        }
    }

    // Register the receiver in onResume
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(TrackingService.LOCATION_UPDATE_ACTION))
    }

    // Unregister the receiver in onPause to avoid memory leaks
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

}
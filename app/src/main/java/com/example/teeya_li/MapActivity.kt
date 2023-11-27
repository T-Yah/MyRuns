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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.util.Calendar
import java.util.LinkedList
import org.apache.commons.math3.transform.DftNormalization


class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, SensorEventListener {
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
    private var lastLocation: LatLng? = null

    private var launchType = 0 //to determine behaviour

    // to hold the readings from the sensor which will be in a double if user selected automatic
    private var x: Double = 0.0
    private var y: Double = 0.0
    private var z: Double = 0.0
    // for time checking
    private var lastTime: Long = 0
    private var currentTime: Long =0
    private lateinit var sensorManager: SensorManager

    //for weka
    private val accelerometerQueue: LinkedList<FloatArray> = LinkedList()
    private val FEATURE_VECTOR_SIZE = 64
    private val wekaClassifier = WekaClassifier()


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

        //Determine if it was gps or automatic that launched the activity
        launchType = intent.getIntExtra("gpsOrAuto", -1)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        startCounting()

        if (launchType == 2){ //if the user selected automatic mode
            lastTime = System.currentTimeMillis() //get time when app opens

        }

        var cancelBtn = findViewById<Button>(R.id.cancelButton)
        cancelBtn.setOnClickListener{
            val intent = Intent()
            intent.action = TrackingService.STOP_SERVICE_ACTION
            sendBroadcast(intent)
            finish()
        }
        val saveBtn = findViewById<Button>(R.id.saveButton)
        saveBtn.setOnClickListener{
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            stopCounting()
            val intent = Intent()
            intent.action = TrackingService.SAVE_DATA_ACTION
            sendBroadcast(intent)
            intent.action = TrackingService.STOP_SERVICE_ACTION
            sendBroadcast(intent)
            saveToDatabase()
            finish()
        }
    }

    private fun saveToDatabase() {
        println("debug: in save database function")
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
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)

//        if (lastLocation == null || latLng != lastLocation) {
//            // Broadcast or use a callback to send location data to the service
//            val intent = Intent(this, TrackingService::class.java)
//            intent.action = TrackingService.LOCATION_UPDATE_ACTION
//            intent.putExtra("latLng", latLng) // Pass latLng as an extra
//            startService(intent)
//
//            // Save the current location as the last location
//            lastLocation = latLng
//        }

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
    private fun generateFeatureVector(): DoubleArray {
        // Initialize arrays for FFT
        val re = DoubleArray(FEATURE_VECTOR_SIZE)
        val im = DoubleArray(FEATURE_VECTOR_SIZE)

        // Copy the accelerometer readings from the queue to re array
        for (i in accelerometerQueue.indices) {
            val reading = accelerometerQueue[i]
            re[i] = reading[0].toDouble() // Assuming x values are stored in the first position of the reading array
        }

        // Compute max value
        val max = re.maxOrNull() ?: 0.0

        // Use Apache Commons Math FFT implementation
        val fft = FastFourierTransformer(DftNormalization.STANDARD)
        fft.transform(re, TransformType.FORWARD)

        // The transformed values are now in 're' and 'im' arrays

        // Create a feature vector
        val featureVector = DoubleArray(FEATURE_VECTOR_SIZE * 2 + 1) // +1 for label

        // Compute FFT coefficients
        for (i in re.indices) {
            // Compute each coefficient magnitude
            val mag = Math.sqrt(re[i] * re[i] + im[i] * im[i])
            // Adding the computed FFT coefficient to the feature vector
            featureVector[i] = mag
            // Clear the field
            im[i] = 0.0
        }

        // Append magnitudes after frequency components
        for (i in 0 until FEATURE_VECTOR_SIZE) {
            featureVector[FEATURE_VECTOR_SIZE + i] = re[i]
        }

        // Finally, append max after frequency components
        featureVector[FEATURE_VECTOR_SIZE * 2] = max

        return featureVector
    }

    override fun onSensorChanged(event: SensorEvent?) { // called very time there is new data sent to the sensor
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // make sure you are getting quality data, not null and from the correct sensor
            // grab the data from the sensor
            x = (event.values[0] / SensorManager.GRAVITY_EARTH).toDouble()
            y = (event.values[1] / SensorManager.GRAVITY_EARTH).toDouble()
            z = (event.values[2] / SensorManager.GRAVITY_EARTH).toDouble()

            val magnitude = Math.sqrt(x * x + y * y + z * z)
            currentTime = System.currentTimeMillis() // get time at shake
            if (magnitude > 3 && currentTime - lastTime > 300) { //the shake is not just noise
                lastTime = currentTime

                // Store the values in the queue
                val reading = floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
                accelerometerQueue.add(reading)

                if (accelerometerQueue.size >= FEATURE_VECTOR_SIZE) {
                    // Generate the feature vector
                    val featureVector = generateFeatureVector()

                    // Use WekaClassifier to classify the feature vector
                    val classificationResult = wekaClassifier.classify(featureVector.toObjectArray())

                    // Do something with the classification result
                    // For example, print it
                    Log.d("weka", "Weka Classification Result: $classificationResult")

                    // Clear the queue after processing
                    accelerometerQueue.clear()
                }
            }
        }
    }

    // Add this extension function to convert DoubleArray to Object[]
    fun DoubleArray.toObjectArray(): Array<Any> {
        return Array(this.size) { this[it] as Any }
    }

    // Register the receiver in onResume
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(TrackingService.LOCATION_UPDATE_ACTION))
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // sensor object
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // Unregister the receiver in onPause to avoid memory leaks
    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // executed only when accuracy is changed, and you need to be aware of the change, not used in this class

}
package com.example.teeya_li

import android.graphics.Color
import android.location.LocationListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myruns.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        if (locationManager != null)
            locationManager.removeUpdates(this)
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
}
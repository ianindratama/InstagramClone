package com.example.submission.view.locationStoryPicker

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.submission.R
import com.example.submission.databinding.ActivityLocationStoryPickerBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import java.util.concurrent.TimeUnit

class LocationStoryPickerActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityLocationStoryPickerBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var storyLocationPicked: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationStoryPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapLocationPicker) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        supportActionBar?.title = getString(R.string.LocationStoryPickerActivity_pageTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setupAction(){

        binding.btnPickThisLocation.setOnClickListener {

            if (storyLocationPicked != null){
                val intent = Intent()

                val lat = storyLocationPicked!!.latitude.toFloat()
                val lon = storyLocationPicked!!.longitude.toFloat()

                intent.putExtra("lat", lat)
                intent.putExtra("lon", lon)
                setResult(RESULT_OK, intent)

                finish()

            }else{
                Toast.makeText(
                    this,
                    getString(R.string.LocationStoryPickerActivity_toast_pleaseWaitUntilMapsLoaded),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }

    override fun onMapReady(googleMap: GoogleMap) {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mMap = googleMap
        mMap.setOnMarkerDragListener(this)

        getMyLocation()
        setMapStyle()

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLocation()
                }
                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.LocationStoryPickerActivity_toast_pleaseAllowLocationPermission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private val requestLocationLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_OK ->
                    getMyLocation()
                RESULT_CANCELED ->
                    Toast.makeText(
                        this,
                        getString(R.string.LocationStoryPickerActivity_toast_pleaseEnableGPS),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {

            val locationRequest = LocationRequest.create().apply {
                interval = TimeUnit.SECONDS.toMillis(1)
                maxWaitTime = TimeUnit.SECONDS.toMillis(1)
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                mMap.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        showStartMarker(location)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.LocationStoryPickerActivity_toast_errorLocationNotFound),
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(100, Intent())
                        finish()
                    }
                }
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException){

                    try {
                        requestLocationLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(
                            this,
                            getString(R.string.LocationStoryPickerActivity_toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

    }

    private fun showStartMarker(location: Location) {
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(startLocation)
                .title(getString(R.string.LocationStoryPickerActivity_marker_title))
                .snippet(getString(R.string.LocationStoryPickerActivity_marker_snippet))
                .draggable(true)
        )?.showInfoWindow()
        storyLocationPicked = startLocation
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 17f))
    }

    override fun onMarkerDragStart(p0: Marker) {
        // do nothing
    }

    override fun onMarkerDrag(p0: Marker) {
        // do nothing
    }

    override fun onMarkerDragEnd(p0: Marker) {
        storyLocationPicked = p0.position
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater

        inflater.inflate(R.menu.option_menu_location_picker, menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            android.R.id.home -> {
                finish()
            }

            R.id.setting_option -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }

        }
        return true
    }

    companion object {
        private const val TAG = "LocationStoryPickerActivity"
    }

}
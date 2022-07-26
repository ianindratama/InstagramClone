package com.example.submission.view.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submission.R
import com.example.submission.ViewModelFactory
import com.example.submission.databinding.ActivityMapsBinding
import com.example.submission.model.UserPreference
import com.example.submission.network.StoryResponse
import com.example.submission.view.detailStory.DetailStoryActivity
import com.example.submission.view.login.LoginActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import java.util.concurrent.TimeUnit


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private var mapReady = false

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel

    private var listStoryResponse: List<StoryResponse>? = null

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{ googleMap ->
            mMap = googleMap
            mapReady = true
            updateMap()
        }

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

        supportActionBar?.title = getString(R.string.MapsActivity_pageTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setupViewModel() {

        mapsViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), null)
        )[MapsViewModel::class.java]

        mapsViewModel.getUser().observe(this) { user ->

            if (user.token != "") {
                setupViewModelData(user.token)
            } else {
                finish()
            }

        }

    }

    private fun setupViewModelData(token: String){

        mapsViewModel.getAllStories(token)

        mapsViewModel.listStoryResponse.observe(this){
            this.listStoryResponse = it
            updateMap()
        }

        mapsViewModel.isLoading.observe(this){
            showLoading(it)
        }

        mapsViewModel.status.observe(this){
            showError(it)
        }


    }

    private fun showLoading(isLoading: Boolean) {
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showError(status: Int?){

        when (status) {

            100 -> {

                Toast.makeText(this, getString(R.string.MapsActivity_toast_noData), Toast.LENGTH_SHORT).show()

            }
            0 -> {

                Toast.makeText(this, getString(R.string.MapsActivity_toast_error), Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun updateMap(){
        if (mapReady && listStoryResponse != null){

            mMap.setOnInfoWindowClickListener(this)

            listStoryResponse?.forEach { story ->

                val marker = LatLng(story.lat.toDouble(), story.lon.toDouble())

                val clickToSeeDetails = getString(R.string.MapsActivity_clickToSeeDetails)
                val infoWindowDescription = "${story.description.take(10)}...$clickToSeeDetails"

                val markerMap = mMap.addMarker(
                    MarkerOptions()
                        .position(marker)
                        .title(story.name)
                        .snippet(infoWindowDescription))

                if (markerMap != null) {
                    markerMap.tag = story.id
                }

            }

            getMyLocation()
            setMapStyle()

            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isIndoorLevelPickerEnabled = true
            mMap.uiSettings.isCompassEnabled = true
            mMap.uiSettings.isMapToolbarEnabled = true
            mMap.uiSettings.isRotateGesturesEnabled = true

            // Animate zoom to Indonesia Coordinate
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.143136, 118.7371783), 1f))

        }
    }

    override fun onInfoWindowClick(p0: Marker) {
        val intent = Intent(this, DetailStoryActivity::class.java)

        val story: StoryResponse? = listStoryResponse?.single { story ->
            p0.tag == story.id
        }

        intent.putExtra("Story", story)
        startActivity(intent)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }else{
                Toast.makeText(
                    this,
                    getString(R.string.MapsActivity_toast_pleaseEnableGPS),
                    Toast.LENGTH_SHORT
                ).show()
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
                        this@MapsActivity,
                        getString(R.string.MapsActivity_toast_pleaseEnableGPS),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    private fun getMyLocation() {

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
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
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException){

                    try {
                        requestLocationLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(
                            this@MapsActivity,
                            getString(R.string.MapsActivity_toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

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

        inflater.inflate(R.menu.option_menu, menu)

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
            R.id.logout_option -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_logoutVerification_title))
                        setMessage(context.getString(R.string.dialog_logoutVerification_message))
                        setPositiveButton(context.getString(R.string.dialog_logoutVerification_button)) { p0, _ ->
                            mapsViewModel.logoutUser()
                            val intent = Intent(this@MapsActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            p0?.dismiss()
                            finish()
                        }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
        }
        return true
    }

    companion object {
        private const val TAG = "MapsActivity"
    }

}
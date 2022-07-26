package com.example.submission.view.addStory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.submission.R
import com.example.submission.ViewModelFactory
import com.example.submission.databinding.ActivityAddStoryBinding
import com.example.submission.model.UserPreference
import com.example.submission.view.camera.*
import com.example.submission.view.locationStoryPicker.LocationStoryPickerActivity
import com.example.submission.view.login.LoginActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var addStoryViewModel: AddStoryViewModel

    private var alertDialog: AlertDialog? = null

    private var getFile: File? = null

    private val _token = MutableLiveData<String>()
    private val token: LiveData<String> = _token

    private var lat: Float? = null
    private var lon: Float? = null

    companion object{

        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (!allPermissionGranted()){
                Toast.makeText(
                    this,
                    getString(R.string.cantgetpermissionspleaseallow),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionGranted()){
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupView()
        setupViewModel()
        setupAction()

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

        supportActionBar?.title = getString(R.string.AddStoryActivity_pageTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setupViewModel() {

        addStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), null)
        )[AddStoryViewModel::class.java]

        addStoryViewModel.getUser().observe(this) { user ->

            _token.value = "Bearer ${user.token}"

        }

    }

    private fun setupAction(){

        binding.cameraButton.setOnClickListener {
            startCameraX()
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.locationButton.setOnClickListener {
            startMaps()
        }

        binding.uploadButton.setOnClickListener {

            addStoryViewModel.setLoadingToTrue()

            val description = binding.descriptionEditText.text.toString().trim()

            when{
                description.isEmpty() -> {
                    binding.descriptionEditText.error = getString(R.string.error_blank_description)
                }
                getFile == null -> {
                    Toast.makeText(this, getString(R.string.error_blank_picture), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    uploadImage(description, lat, lon)
                    addStoryViewModel.isLoading.observe(this){
                        showLoading(it)
                    }

                    addStoryViewModel.status.observe(this){
                        showStatus(it)
                    }
                }
            }

        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.chooseAPicture))
        launcherIntentGallery.launch(chooser)
    }

    private fun startMaps(){
        val intent = Intent(this, LocationStoryPickerActivity::class.java)
        launcherLocationStoryPicker.launch(intent)
    }

    private fun uploadImage(description: String, lat: Float?, lon: Float?) {

        val file = reduceFileImage(getFile as File)

        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())

        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        addStoryViewModel.uploadStory(descriptionRequestBody, imageMultipart, lat, lon, token.value.toString())

    }

    private fun showLoading(isLoading: Boolean) {
        if(isLoading){
            binding.progressBar.bringToFront()
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showStatus(status: Int?){
        when (status) {

            200 -> {
                
                Toast.makeText(this, getString(R.string.addStoryResult_toast_success), Toast.LENGTH_SHORT).show()
                finish()

            }
            0 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_addStoryResult_error_title))
                        setMessage(context.getString(R.string.dialog_addStoryResult_error_message))
                        setPositiveButton(context.getString(R.string.dialog_addStoryResult_error_button)) { p0, _ -> p0?.dismiss() }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
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
                            addStoryViewModel.logoutUser()
                            val intent = Intent(this@AddStoryActivity, LoginActivity::class.java)
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

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == CAMERA_X_RESULT){

            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )

            getFile = bitmapToFile(result, this)

            binding.previewImageView.setImageBitmap(result)

        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)

            getFile = myFile

            binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(myFile.path))

        }
    }

    private val launcherLocationStoryPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == RESULT_OK){

            lat = it.data?.getSerializableExtra("lat") as Float
            lon = it.data?.getSerializableExtra("lon") as Float

            binding.locationTextView.text =
                String.format(getString(R.string.AddStoryActivity_Location_PlaceHolder), lat, lon)

        }
    }

}
package com.example.submission.view.detailStory

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.submission.R
import com.example.submission.ViewModelFactory
import com.example.submission.network.StoryResponse
import com.example.submission.databinding.ActivityDetailStoryBinding
import com.example.submission.model.UserPreference
import com.example.submission.view.login.LoginActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var detailStoryViewModel: DetailStoryViewModel

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupData()

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

        supportActionBar?.title = getString(R.string.DetailStoryActivity_pageTitle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setupViewModel() {

        detailStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), null)
        )[DetailStoryViewModel::class.java]

    }

    private fun setupData() {
        val story = intent.getParcelableExtra<StoryResponse>("Story") as StoryResponse
        Glide.with(applicationContext)
            .load(story.photoUrl)
            .into(binding.detailPhoto)
        binding.detailUsername.text = story.name
        binding.detailDescription.text = story.description
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
                            detailStoryViewModel.logoutUser()
                            val intent = Intent(this@DetailStoryActivity, LoginActivity::class.java)
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

}
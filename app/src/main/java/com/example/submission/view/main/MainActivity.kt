package com.example.submission.view.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.submission.R
import com.example.submission.ViewModelFactory
import com.example.submission.adapter.LoadingStateAdapter
import com.example.submission.adapter.StoryListAdapter
import com.example.submission.databinding.ActivityMainBinding
import com.example.submission.model.UserPreference
import com.example.submission.view.addStory.AddStoryActivity
import com.example.submission.view.login.LoginActivity
import com.example.submission.view.maps.MapsActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        supportActionBar?.title = getString(R.string.MainActivity_pageTitle)

        binding.rvStoryList.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        binding.rvStoryList.layoutManager = layoutManager

        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvStoryList.addItemDecoration(itemDecoration)

    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->

            if (user.token != "") {
                setupViewModelData(user.token)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }
    }

    private fun setupViewModelData(token: String){

        val adapter = StoryListAdapter()

        mainViewModel.getAllStories(token)

        mainViewModel.story.observe(this) {

            adapter.submitData(lifecycle, it)

            lifecycleScope.launch {
                adapter.loadStateFlow.collectLatest { loadStates ->

                    binding.progressBar.isVisible = loadStates.refresh is LoadState.Loading

                    if (loadStates.refresh is LoadState.NotLoading && loadStates.mediator?.refresh is LoadState.NotLoading && adapter.itemCount == 0){
                        binding.rvStoryList.visibility = View.GONE

                        binding.MainActivityNoDataImage.visibility = View.VISIBLE
                        binding.MainActivityNoDataTitle.visibility = View.VISIBLE
                        binding.MainActivityNoDataSubTitle.visibility = View.VISIBLE
                        binding.MainActivityNoDataSubTitle.text = getString(R.string.MainActivityNoDataSubTitle1)

                    }else if(loadStates.mediator?.refresh is LoadState.Error && adapter.itemCount == 0){

                        binding.rvStoryList.visibility = View.GONE

                        binding.MainActivityNoDataImage.visibility = View.VISIBLE
                        binding.MainActivityNoDataTitle.visibility = View.VISIBLE
                        binding.MainActivityNoDataSubTitle.visibility = View.VISIBLE
                        binding.MainActivityNoDataSubTitle.text = getString(R.string.MainActivityNoDataSubTitle2)

                    }else{
                        binding.MainActivityNoDataImage.visibility = View.GONE
                        binding.MainActivityNoDataTitle.visibility = View.GONE
                        binding.MainActivityNoDataSubTitle.visibility = View.GONE

                        binding.rvStoryList.visibility = View.VISIBLE
                    }

                }
            }

        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0){
                    binding.rvStoryList.scrollToPosition(0)
                }
            }
        })

        binding.rvStoryList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

    }

    private fun setupAction(){
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater

        inflater.inflate(R.menu.option_menu_with_maps, menu)

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.maps_option -> {
                startActivity(Intent(this, MapsActivity::class.java))
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
                            mainViewModel.logoutUser()
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
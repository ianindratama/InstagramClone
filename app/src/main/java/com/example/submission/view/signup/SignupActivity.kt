package com.example.submission.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submission.R
import com.example.submission.ViewModelFactory
import com.example.submission.databinding.ActivitySignupBinding
import com.example.submission.model.UserPreference
import com.example.submission.view.main.MainActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var signupViewModel: SignupViewModel

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
        playAnimation()
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
        supportActionBar?.hide()
    }

    private fun setupViewModel() {
        signupViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), null)
        )[SignupViewModel::class.java]
    }

    private fun setupAction() {

        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            when {
                name.isEmpty() -> {
                    binding.nameEditTextLayout.error = getString(R.string.error_blank_name)
                }
                email.isEmpty() -> {
                    binding.emailEditTextLayout.error = getString(R.string.error_blank_email)
                }
                !email.isValidEmail() -> {
                    binding.emailEditTextLayout.error = getString(R.string.error_email)
                }
                password.isEmpty() -> {
                    binding.passwordEditTextLayout.error = getString(R.string.error_blank_password)
                }
                password.length in 1..5 -> {
                    binding.passwordEditTextLayout.error = getString(R.string.error_password)
                }


                else -> {

                    signupViewModel.saveUser(name, email, password)

                    signupViewModel.isLoading.observe(this){
                        showLoading(it)
                    }

                    signupViewModel.status.observe(this){
                        showStatus(it)
                    }

                }
            }
        }
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun showLoading(isLoading: Boolean) {
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showStatus(status: Int?){
        when (status) {
            200 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_registerResult_success_title))
                        setMessage(context.getString(R.string.dialog_registerResult_success_message))
                        setPositiveButton(context.getString(R.string.dialog_registerResult_success_button)) { p0, _ ->
                            val intent = Intent(this@SignupActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            p0.dismiss()
                            finish()
                        }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
            100 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_registerResult_errorEmailUsed_title))
                        setMessage(context.getString(R.string.dialog_registerResult_errorEmailUsed_message))
                        setPositiveButton(context.getString(R.string.dialog_registerResult_errorEmailUsed_button)) { p0, _ -> p0?.dismiss() }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
            0 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_registerResult_error_title))
                        setMessage(context.getString(R.string.dialog_registerResult_error_message))
                        setPositiveButton(context.getString(R.string.dialog_registerResult_error_button)) { p0, _ -> p0?.dismiss() }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(500)
        val nameTextView = ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(500)
        val nameEditTextLayout = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val emailTextView = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(500)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordTextView = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(500)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(500)


        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                signup
            )
            startDelay = 500
        }.start()
    }
}
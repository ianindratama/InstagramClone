package com.example.submission.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.submission.R
import com.example.submission.ViewModelFactory
import com.example.submission.databinding.ActivityLoginBinding
import com.example.submission.model.UserPreference
import com.example.submission.view.main.MainActivity
import com.example.submission.view.signup.SignupActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), null)
        )[LoginViewModel::class.java]
    }

    private fun setupAction() {

        binding.loginButton.setOnClickListener {

            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            when {
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

                    loginViewModel.loginUser(email, password)

                    loginViewModel.isLoading.observe(this){
                        showLoading(it)
                    }

                    loginViewModel.status.observe(this){
                        showStatus(it)
                    }

                }
            }
        }
        binding.registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
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

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            }
            150 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_loginResult_errorEmail_title))
                        setMessage(context.getString(R.string.dialog_loginResult_errorEmail_message))
                        setPositiveButton(context.getString(R.string.dialog_loginResult_errorEmail_button)) { p0, _ -> p0?.dismiss() }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
            100 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_loginResult_errorPassword_title))
                        setMessage(context.getString(R.string.dialog_loginResult_errorPassword_message))
                        setPositiveButton(context.getString(R.string.dialog_loginResult_errorPassword_button)) { p0, _ -> p0?.dismiss() }
                    }.show()
                }else{
                    alertDialog?.show()
                }

            }
            0 -> {

                if (alertDialog == null || !alertDialog?.isShowing!!){
                    alertDialog = AlertDialog.Builder(this).apply {
                        setTitle(context.getString(R.string.dialog_loginResult_error_title))
                        setMessage(context.getString(R.string.dialog_loginResult_error_message))
                        setPositiveButton(context.getString(R.string.dialog_loginResult_error_button)) { p0, _ -> p0?.dismiss() }
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
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(500)
        val emailTextView = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(500)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordTextView = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(500)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(500)
        val register = ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title, message, emailTextView, emailEditTextLayout, passwordTextView, passwordEditTextLayout, login, register)
            startDelay = 500
        }.start()
    }

}
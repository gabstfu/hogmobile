package com.example.smart_hog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEt: TextInputEditText
    private lateinit var passwordEt: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var rememberMe: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        btnLogin = findViewById(R.id.btnLogin)
        rememberMe = findViewById(R.id.rememberMe)

        btnLogin.setOnClickListener {
            val input = emailEt.text.toString()
            val password = passwordEt.text.toString()

            if (input.isNotEmpty() && password.isNotEmpty()) {
                val loading = LoadingUtils.showLoading(this)

                // Sending as Form Data fields to fix the 400 error
                RetrofitClient.instance.login(input, input, password)
                    .enqueue(object : retrofit2.Callback<Map<String, Any>> {
                        override fun onResponse(
                            call: retrofit2.Call<Map<String, Any>>,
                            response: retrofit2.Response<Map<String, Any>>
                        ) {
                            loading.dismiss()
                            if (response.isSuccessful) {
                                // Save "Remember Me" preference and user email
                                val loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
                                loginPrefs.edit()
                                    .putBoolean("remember_device", rememberMe.isChecked)
                                    .putString("user_email", input)
                                    .apply()

                                // Set default name from email if not already set
                                val profilePrefs = getSharedPreferences("user_profile", MODE_PRIVATE)
                                if (!profilePrefs.contains("user_name")) {
                                    val defaultName = input.substringBefore("@")
                                    profilePrefs.edit().putString("user_name", defaultName).apply()
                                }

                                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val code = response.code()
                                when (code) {
                                    401 -> Toast.makeText(this@LoginActivity, "Invalid credentials (401)", Toast.LENGTH_SHORT).show()
                                    400 -> Toast.makeText(this@LoginActivity, "Input Error (400). Contact Developer.", Toast.LENGTH_SHORT).show()
                                    else -> Toast.makeText(this@LoginActivity, "Login Failed: $code", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                            loading.dismiss()
                            Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
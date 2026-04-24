package com.example.smart_hog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnGetStarted = findViewById<Button>(R.id.btn_get_started)
        btnGetStarted.setOnClickListener {
            val loading = LoadingUtils.showLoading(this)
            
            // "Wake Up" the Render Backend early to reduce Login latency
            RetrofitClient.instance.getDashboardOverview().enqueue(object : retrofit2.Callback<DashboardOverviewResponse> {
                override fun onResponse(call: retrofit2.Call<DashboardOverviewResponse>, response: retrofit2.Response<DashboardOverviewResponse>) {
                    proceedToLogin(loading)
                }

                override fun onFailure(call: retrofit2.Call<DashboardOverviewResponse>, t: Throwable) {
                    // Even if it fails (unauthorized), the server is now awake
                    proceedToLogin(loading)
                }
            })

            // Safety timeout: don't wait more than 5 seconds on the welcome screen
            Handler(Looper.getMainLooper()).postDelayed({
                proceedToLogin(loading)
            }, 5000)
        }
    }

    private fun proceedToLogin(loading: android.app.Dialog) {
        if (isFinishing) return
        loading.dismiss()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

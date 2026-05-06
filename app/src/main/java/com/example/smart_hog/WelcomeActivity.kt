package com.example.smart_hog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnGetStarted = findViewById<Button>(R.id.btn_get_started)
        btnGetStarted.setOnClickListener {
            // "Wake Up" the Render Backend early in the background to reduce Login latency
            RetrofitClient.instance.getDashboardOverview().enqueue(object : retrofit2.Callback<DashboardOverviewResponse> {
                override fun onResponse(call: retrofit2.Call<DashboardOverviewResponse>, response: retrofit2.Response<DashboardOverviewResponse>) {}
                override fun onFailure(call: retrofit2.Call<DashboardOverviewResponse>, t: Throwable) {}
            })

            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

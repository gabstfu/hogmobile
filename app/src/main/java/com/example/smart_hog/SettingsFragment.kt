package com.example.smart_hog

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData(view)

        // Straight Back Arrow with Loading
        view.findViewById<ImageButton>(R.id.btn_back_settings).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                findNavController().navigateUp()
            }, 800)
        }

        // Account Profile Click - Goes to ProfileActivity with Loading
        view.findViewById<LinearLayout>(R.id.item_profile).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent)
            }, 1000)
        }

        // Privacy & Security - Navigate to Fragment with Loading
        view.findViewById<LinearLayout>(R.id.item_privacy_nav).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                findNavController().navigate(R.id.navigation_privacy_security)
            }, 1000)
        }

        // Language & Region - Navigate to Fragment with Loading
        view.findViewById<LinearLayout>(R.id.item_language_nav).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                findNavController().navigate(R.id.navigation_language_region)
            }, 1000)
        }

        // About SMART-HOG with Loading
        view.findViewById<LinearLayout>(R.id.item_about_nav).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                findNavController().navigate(R.id.navigation_about)
            }, 1000)
        }

        // Sign Out with Loading
        view.findViewById<MaterialButton>(R.id.btn_sign_out).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                Toast.makeText(requireContext(), "Signing out...", Toast.LENGTH_SHORT).show()
                
                // Clear User Data
                val prefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                // Check Remember Me preference
                val loginPrefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val isRemembered = loginPrefs.getBoolean("remember_device", false)

                val intent = if (isRemembered) {
                    Intent(requireContext(), LoginActivity::class.java)
                } else {
                    Intent(requireContext(), WelcomeActivity::class.java)
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }, 1500)
        }
    }

    private fun loadUserData(view: View) {
        val prefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "Silas Farm Owner")
        val imageString = prefs.getString("image", null)

        val tvName = view.findViewById<TextView>(R.id.tv_username_settings)
        val ivProfile = view.findViewById<ImageView>(R.id.iv_user_profile_settings)

        tvName.text = name

        if (imageString != null) {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ivProfile.setImageBitmap(bitmap)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadUserData(it) }
    }
}

package com.example.smart_hog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class PrivacySecurityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_privacy_security, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Professional Date/Time Header
        val tvHeaderDateTime = view.findViewById<TextView>(R.id.tv_header_datetime)
        val sdf = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault())
        tvHeaderDateTime.text = sdf.format(Date())

        // Back Button
        view.findViewById<ImageButton>(R.id.btn_back_privacy).setOnClickListener {
            findNavController().navigateUp()
        }

        // Country Spinner with Flag Emojis
        val countryList = arrayOf(
            "🇵🇭 Philippines (+63)",
            "🇺🇸 United States (+1)",
            "🇬🇧 United Kingdom (+44)",
            "🇨🇦 Canada (+1)",
            "🇦🇺 Australia (+61)",
            "🇯🇵 Japan (+81)",
            "🇸🇬 Singapore (+65)"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, countryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Spinner>(R.id.spinner_country).adapter = adapter

        // Save Changes with Loading
        view.findViewById<View>(R.id.btn_save_privacy).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }, 2000)
        }
    }
}

package com.example.smart_hog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class LanguageRegionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_language_region, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set Date/Time in Header
        val tvHeaderDateTime = view.findViewById<TextView>(R.id.tv_header_datetime_lang)
        val sdf = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault())
        tvHeaderDateTime.text = sdf.format(Date())

        // Back Button
        view.findViewById<ImageButton>(R.id.btn_back_language).setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Language Spinner
        val languages = arrayOf("English", "Tagalog")
        val langAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Spinner>(R.id.spinner_language).adapter = langAdapter

        // Setup Region Spinner
        val regions = arrayOf("Philippines", "United States", "United Kingdom", "Canada", "Australia")
        val regAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, regions)
        regAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.findViewById<Spinner>(R.id.spinner_region).adapter = regAdapter

        // Set Date & Time Picker Button
        view.findViewById<MaterialButton>(R.id.btn_set_datetime).setOnClickListener {
            showDateTimePicker()
        }

        // Apply Changes with Loading
        view.findViewById<View>(R.id.btn_save_region).setOnClickListener {
            val loading = LoadingUtils.showLoading(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                loading.dismiss()
                Toast.makeText(requireContext(), "Regional Settings Applied!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }, 1500)
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
            val timePickerDialog = TimePickerDialog(requireContext(), { _, hour, minute ->
                Toast.makeText(requireContext(), "Date/Time Set: $day/${month + 1}/$year $hour:$minute", Toast.LENGTH_LONG).show()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            timePickerDialog.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }
}

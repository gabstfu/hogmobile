package com.example.smart_hog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FeedFragment : Fragment() {

    private lateinit var profileManager: ProfileManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Profile Management
        val profileIcon = view.findViewById<ImageView>(R.id.profile_icon_feed)
        if (profileIcon != null) {
            profileManager = ProfileManager(this, profileIcon)
            profileManager.init()
        }

        // Back Button Functionality - Babalik sa Menu
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back_feed)
        btnBack?.setOnClickListener {
            findNavController().navigate(R.id.navigation_menu)
        }

        // Calendar Logic
        val calendar = view.findViewById<CalendarView>(R.id.sellCalendar)
        calendar?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(requireContext(), "Selling schedule: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // New Schedule Button Functionality
        val btnNewSchedule = view.findViewById<View>(R.id.btn_new_schedule)
        btnNewSchedule?.setOnClickListener {
            showNewScheduleDialog()
        }
    }

    private fun showNewScheduleDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_new_schedule)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Batch Code Dropdown
        val spinnerBatchCode = dialog.findViewById<AutoCompleteTextView>(R.id.spinner_batch_code)
        
        // 1. Start with local batches from Monitor
        val localBatches = BatchRepository.getBatches(requireContext()).toMutableList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, localBatches)
        spinnerBatchCode.setAdapter(adapter)

        // 2. Fetch batches from Backend and update the list
        RetrofitClient.instance.getBatches().enqueue(object : retrofit2.Callback<BatchListResponse> {
            override fun onResponse(call: retrofit2.Call<BatchListResponse>, response: retrofit2.Response<BatchListResponse>) {
                if (response.isSuccessful) {
                    val unwanted = setOf("B001", "B002", "B003", "bading batch", "bading kaba", "batch001", "batch002")
                    val apiBatches = response.body()?.data?.map { it.batchCode } ?: emptyList()
                    
                    val combinedBatches = (localBatches + apiBatches)
                        .distinct()
                        .filter { batch -> unwanted.none { it.equals(batch, ignoreCase = true) } }
                        .sorted()
                    
                    // Update the adapter with live data
                    adapter.clear()
                    adapter.addAll(combinedBatches)
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onFailure(call: retrofit2.Call<BatchListResponse>, t: Throwable) {
                // If API fails, we still have the local batches
            }
        })

        // Time Selection
        val chipGroupTimes = dialog.findViewById<ChipGroup>(R.id.chip_group_times)
        val inputCustomTime = dialog.findViewById<TextInputLayout>(R.id.input_custom_time)
        val etFeedingTime = dialog.findViewById<EditText>(R.id.et_feeding_time)

        var selectedTime = ""

        chipGroupTimes.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_time1 -> {
                    selectedTime = "06:00:00"
                    inputCustomTime.visibility = View.GONE
                }
                R.id.chip_time2 -> {
                    selectedTime = "11:00:00"
                    inputCustomTime.visibility = View.GONE
                }
                R.id.chip_time3 -> {
                    selectedTime = "17:00:00"
                    inputCustomTime.visibility = View.GONE
                }
                R.id.chip_custom_time -> {
                    inputCustomTime.visibility = View.VISIBLE
                    selectedTime = etFeedingTime.text.toString()
                }
            }
        }

        etFeedingTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, m ->
                val time = String.format(Locale.US, "%02d:%02d:00", h, m)
                etFeedingTime.setText(time)
                selectedTime = time
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        // Start Date Logic
        val etStartDate = dialog.findViewById<EditText>(R.id.et_start_date)
        etStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                etStartDate.setText(date)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        val etAmount = dialog.findViewById<EditText>(R.id.et_amount)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<Button>(R.id.btn_save_schedule)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val batch = spinnerBatchCode.text.toString()
            val startDate = etStartDate.text.toString()
            val amount = etAmount.text.toString()
            
            // Fix: If custom time is selected, ensure we use the text from etFeedingTime
            if (chipGroupTimes.checkedChipId == R.id.chip_custom_time) {
                selectedTime = etFeedingTime.text.toString()
            }

            // Days are no longer selectable, so we default to all days or empty list
            val selectedDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

            if (batch.isNotEmpty() && selectedTime.isNotEmpty() && amount.isNotEmpty()) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
                
                // Format time correctly for ISO (HH:mm:ss)
                val formattedTime = if (selectedTime.length == 5) "$selectedTime:00" else selectedTime
                val isoDateTime = "${if (startDate.isNotEmpty()) startDate else today}T$formattedTime"
                
                val newSchedule = FeedingSchedule(
                    batchCode = batch,
                    feedQuantity = amount.toDouble(),
                    feedTime = isoDateTime,
                    datetime = isoDateTime,
                    daysOfWeek = selectedDays,
                    startDate = if (startDate.isNotEmpty()) startDate else today,
                    feedType = "Starter", 
                    growthCode = "GC001",
                    deviceCode = "DEV001",
                    penCode = "PEN001",
                    isActive = true
                )
                
                // Log the JSON being sent to help debugging
                android.util.Log.d("FEED_DEBUG", "Sending Schedule: $newSchedule")
                
                saveScheduleToBackend(newSchedule, dialog)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveScheduleToBackend(schedule: FeedingSchedule, dialog: Dialog) {
        val loading = LoadingUtils.showLoading(requireContext())
        
        RetrofitClient.instance.createSchedule(schedule)
            .enqueue(object : retrofit2.Callback<Map<String, Any>> {
                override fun onResponse(
                    call: retrofit2.Call<Map<String, Any>>,
                    response: retrofit2.Response<Map<String, Any>>
                ) {
                    loading.dismiss()
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Schedule Saved Successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        android.util.Log.e("FEED_ERROR", "Server Rejected: $errorBody")
                        
                        // Parse error to show a better message if possible
                        if (errorBody.contains("batch_code")) {
                            Toast.makeText(requireContext(), "Error: Invalid Batch Code", Toast.LENGTH_LONG).show()
                        } else if (errorBody.contains("feed_type")) {
                            Toast.makeText(requireContext(), "Error: Invalid Feed Type", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                    loading.dismiss()
                    android.util.Log.e("FEED_ERROR", "Network Failure: ${t.message}")
                    Toast.makeText(requireContext(), "Network Error: Check Connection", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (::profileManager.isInitialized) {
            profileManager.refresh()
        }
    }
}

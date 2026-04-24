package com.example.smart_hog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private val monitorViewModel: MonitorViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    private lateinit var switchPower: SwitchMaterial
    private lateinit var autoBatchSelector: AutoCompleteTextView
    private lateinit var btnSelectDate: MaterialButton
    private lateinit var btnSelectTime: MaterialButton
    private lateinit var etCustomFeed: TextInputEditText
    private lateinit var btnApplyRemote: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        switchPower = view.findViewById(R.id.switch_power)
        autoBatchSelector = view.findViewById(R.id.auto_batch_selector)
        btnSelectDate = view.findViewById(R.id.btn_select_date)
        btnSelectTime = view.findViewById(R.id.btn_select_time)
        etCustomFeed = view.findViewById(R.id.et_custom_feed)
        btnApplyRemote = view.findViewById(R.id.btn_apply_remote)

        setupBatchSelector()
        setupDateTimePickers()
        setupPresets(view)

        btnApplyRemote.setOnClickListener {
            handleApplyCommand()
        }

        // Observe batches to populate the dropdown
        monitorViewModel.apiBatches.observe(viewLifecycleOwner) { batches ->
            val batchNames = batches.map { it.batchName }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, batchNames)
            autoBatchSelector.setAdapter(adapter)
        }

        monitorViewModel.fetchApiData()
    }

    private fun setupBatchSelector() {
        // Initial state
        val initialList = listOf("Loading batches...")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, initialList)
        autoBatchSelector.setAdapter(adapter)
    }

    private fun setupDateTimePickers() {
        btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateLabel()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSelectTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    updateTimeLabel()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        btnSelectDate.text = format.format(calendar.time)
    }

    private fun updateTimeLabel() {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        btnSelectTime.text = format.format(calendar.time)
    }

    private fun setupPresets(view: View) {
        view.findViewById<Button>(R.id.btn_preset_6am).setOnClickListener {
            setPresetTime(6, 0)
        }
        view.findViewById<Button>(R.id.btn_preset_11am).setOnClickListener {
            setPresetTime(11, 0)
        }
        view.findViewById<Button>(R.id.btn_preset_4pm).setOnClickListener {
            setPresetTime(16, 0)
        }
        view.findViewById<Button>(R.id.btn_preset_3kg).setOnClickListener {
            etCustomFeed.setText("3.0")
        }
    }

    private fun setPresetTime(hour: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        updateTimeLabel()
    }

    private fun handleApplyCommand() {
        val isPowerOn = switchPower.isChecked
        val batch = autoBatchSelector.text.toString()
        val dateText = btnSelectDate.text.toString()
        val timeText = btnSelectTime.text.toString()
        val amount = etCustomFeed.text.toString()

        if (batch.isEmpty() || batch == "Loading batches..." || dateText == "Set Date" || timeText == "Set Time" || amount.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all remote parameters", Toast.LENGTH_SHORT).show()
            return
        }

        val powerStatus = if (isPowerOn) "ON" else "OFF"
        val confirmationMessage = "Remote Command Sent!\nPower: $powerStatus\nBatch: $batch\nSchedule: $dateText $timeText\nAmount: $amount KG"
        
        Toast.makeText(requireContext(), confirmationMessage, Toast.LENGTH_LONG).show()
        
        // In a real scenario, you'd send this to your IoT server via Retrofit
    }
}

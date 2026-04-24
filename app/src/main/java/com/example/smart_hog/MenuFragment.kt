package com.example.smart_hog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MenuFragment : Fragment() {

    private val monitorViewModel: MonitorViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    private lateinit var autoBatchSelector: AutoCompleteTextView
    private lateinit var btnSelectDate: MaterialButton
    private lateinit var btnSelectTime: MaterialButton
    private lateinit var etCustomFeed: TextInputEditText
    private lateinit var btnFeedCircle: MaterialCardView
    private lateinit var tvFeedStatus: TextView
    private lateinit var ivFeedStatus: ImageView
    private lateinit var profileManager: ProfileManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Header Views
        view.findViewById<ImageButton>(R.id.btn_profile_nav).setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        
        val tvUsernameHeader = view.findViewById<TextView>(R.id.tv_username_header)
        loadHeaderData(tvUsernameHeader)

        // Initialize Remote Views
        autoBatchSelector = view.findViewById(R.id.auto_batch_selector)
        btnSelectDate = view.findViewById(R.id.btn_select_date)
        btnSelectTime = view.findViewById(R.id.btn_select_time)
        etCustomFeed = view.findViewById(R.id.et_custom_feed)
        btnFeedCircle = view.findViewById(R.id.btn_feed_circle)
        tvFeedStatus = view.findViewById(R.id.tv_feed_status)
        ivFeedStatus = view.findViewById(R.id.iv_feed_status)

        // Initialize Profile Manager
        val userAvatar = view.findViewById<ImageView>(R.id.user_avatar)
        profileManager = ProfileManager(this, userAvatar)
        profileManager.init()

        setupBatchSelector()
        setupDateTimePickers()
        setupPresets(view)

        btnFeedCircle.setOnClickListener {
            handleFeedingAction()
        }

        // Observe batches to populate the dropdown
        monitorViewModel.apiBatches.observe(viewLifecycleOwner) { batches ->
            val batchNames = batches.map { it.batchName }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, batchNames)
            autoBatchSelector.setAdapter(adapter)
        }

        monitorViewModel.fetchApiData()
    }

    private fun loadHeaderData(tvUsername: TextView) {
        val prefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val loginPrefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        
        val defaultEmail = loginPrefs.getString("user_email", "owner@smarthog.com")
        val defaultName = defaultEmail?.substringBefore("@")?.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        } ?: "Silas Farm Owner"

        tvUsername.text = prefs.getString("username", defaultName)
    }

    private fun setupBatchSelector() {
        val loadingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("Loading batches..."))
        autoBatchSelector.setAdapter(loadingAdapter)
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

    private fun handleFeedingAction() {
        val batch = autoBatchSelector.text.toString()
        val dateText = btnSelectDate.text.toString()
        val timeText = btnSelectTime.text.toString()
        val amount = etCustomFeed.text.toString()

        if (batch.isEmpty() || batch == "Loading batches..." || dateText == "Set Date" || timeText == "Set Time" || amount.isEmpty()) {
            Toast.makeText(requireContext(), "Please complete the schedule first", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulate Success UI State
        tvFeedStatus.text = "FEEDING\nSCHEDULED"
        btnFeedCircle.setCardBackgroundColor(resources.getColor(R.color.dark_orange))
        ivFeedStatus.setImageResource(android.R.drawable.ic_menu_today)

        val confirmationMessage = "Feeding Scheduled Successfully!\nBatch: $batch\nTime: $dateText $timeText\nAmount: $amount KG"
        Toast.makeText(requireContext(), confirmationMessage, Toast.LENGTH_LONG).show()
    }
}

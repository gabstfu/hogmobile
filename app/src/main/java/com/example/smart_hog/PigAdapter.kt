package com.example.smart_hog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.smart_hog.databinding.ItemPigBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.smart_hog.databinding.DialogWeightHistoryBinding

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.smart_hog.databinding.DialogUpdatePigBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PigAdapter(
    private var pigList: MutableList<Pig>,
    private val fragment: Fragment,
    private val onEditImageClick: (Int) -> Unit
) : RecyclerView.Adapter<PigAdapter.PigViewHolder>() {

    inner class PigViewHolder(val binding: ItemPigBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PigViewHolder {
        val binding = ItemPigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PigViewHolder(binding)
    }

    override fun getItemCount(): Int = pigList.size

    override fun onBindViewHolder(holder: PigViewHolder, position: Int) {
        val pig = pigList[position]
        holder.binding.apply {
            pigID.text = pig.id
            pigBatch.text = pig.batch
            pigWeight.text = "Last Weight: ${pig.weight} kg"
            pigStatus.text = pig.status

            // Display Image
            if (pig.imageBitmap != null) {
                pigImage.setImageBitmap(pig.imageBitmap)
            } else {
                pigImage.setImageResource(pig.imageResId)
            }

            // Update Click
            btnEditPigImage.setOnClickListener {
                showUpdateDialog(root.context, position)
            }

            // History Click
            historyBtn.setOnClickListener {
                showHistoryDialog(root.context, pig.id)
            }
        }
    }

    private fun showUpdateDialog(context: android.content.Context, position: Int) {
        val pig = pigList[position]
        val dialogBinding = DialogUpdatePigBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.tvUpdatePigId.text = "Pig ${pig.id}"
        dialogBinding.etCurrentWeight.setText(pig.weight.toString())
        dialogBinding.etBirthdate.setText(pig.birthDate ?: "")
        dialogBinding.etFeedingTime.setText(pig.feedingTime ?: "")
        dialogBinding.etFeedingAmount.setText(pig.feedingAmount.toString())
        dialogBinding.etHealthNotes.setText(pig.healthNotes ?: "")

        // Compute age if birthdate exists
        pig.birthDate?.let { updateAgeDisplay(it, dialogBinding) }

        // Date Picker for Birthdate
        dialogBinding.etBirthdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(context, { _, year, month, day ->
                val selectedDate = "${year}-${month + 1}-${day}"
                dialogBinding.etBirthdate.setText(selectedDate)
                updateAgeDisplay(selectedDate, dialogBinding)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Time Picker for Feeding
        dialogBinding.etFeedingTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(context, { _, hour, minute ->
                dialogBinding.etFeedingTime.setText(String.format("%02d:%02d", hour, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        // Health Status Dropdown
        val statuses = arrayOf("Healthy", "Under observation", "Sick")
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, statuses)
        dialogBinding.autoCompleteHealthStatus.setAdapter(adapter)
        dialogBinding.autoCompleteHealthStatus.setText(pig.status, false)

        // Display Image
        if (pig.imageBitmap != null) {
            dialogBinding.ivUpdatePigPreview.setImageBitmap(pig.imageBitmap)
        } else {
            dialogBinding.ivUpdatePigPreview.setImageResource(pig.imageResId)
        }

        // Change Image Click
        dialogBinding.btnChangePigImage.setOnClickListener {
            onEditImageClick(position)
            // Note: The actual image selection logic is handled in MonitorFragment via onEditImageClick
        }

        dialogBinding.btnCancelUpdate.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSaveUpdate.setOnClickListener {
            val weightStr = dialogBinding.etCurrentWeight.text.toString()
            if (weightStr.isEmpty()) {
                Toast.makeText(context, "Please enter weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update Pig object
            pig.weight = weightStr.toDouble()
            pig.birthDate = dialogBinding.etBirthdate.text.toString()
            pig.feedingTime = dialogBinding.etFeedingTime.text.toString()
            pig.feedingAmount = dialogBinding.etFeedingAmount.text.toString().toDoubleOrNull() ?: 0.0
            pig.status = dialogBinding.autoCompleteHealthStatus.text.toString()
            pig.healthNotes = dialogBinding.etHealthNotes.text.toString()

            // Update through ViewModel to sync with Web
            if (fragment.requireActivity() is MainActivity) {
                val monitorFragment = fragment as? MonitorFragment
                monitorFragment?.viewModel?.updatePigData(context, pig)
            }

            notifyItemChanged(position)
            Toast.makeText(context, "Unit updated successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateAgeDisplay(birthDateStr: String, binding: DialogUpdatePigBinding) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDate = sdf.parse(birthDateStr)
            val currentDate = Date()
            val diff = currentDate.time - birthDate!!.time
            val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            
            if (days < 0) {
                binding.tvComputedAge.text = "Invalid birthdate"
                return
            }

            val weeks = days / 7
            val months = days / 30
            binding.tvComputedAge.text = "Age: $days days ($weeks weeks, $months months)"
        } catch (e: Exception) {
            binding.tvComputedAge.text = "Age: --"
        }
    }

    fun updateData(newList: List<Pig>) {
        pigList = newList.toMutableList()
        notifyDataSetChanged()
    }

    private fun showHistoryDialog(context: android.content.Context, pigId: String) {
        val dialogBinding = DialogWeightHistoryBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvHistoryPigId.text = "Pig $pigId"
        dialogBinding.btnCloseHistory.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

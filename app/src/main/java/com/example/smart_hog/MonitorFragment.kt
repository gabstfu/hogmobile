package com.example.smart_hog

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smart_hog.databinding.FragmentMonitorBinding

class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!
    
    val viewModel: MonitorViewModel by viewModels()
    private lateinit var adapter: PigAdapter
    
    private var isAddCardVisible = false
    private var tempBitmap: Bitmap? = null
    private var editingPosition: Int = -1

    // Modern Activity Result Launchers
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    private lateinit var profileManager: ProfileManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Profile Management
        profileManager = ProfileManager(this, binding.profileIconMonitor)
        profileManager.init()

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PigAdapter(mutableListOf(), this) { position ->
            editingPosition = position
            galleryLauncher.launch("image/*")
        }
        binding.pigRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = this@MonitorFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBackMonitor.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnShowAddCard.setOnClickListener {
            toggleAddCard()
        }

        binding.btnSelectImage.setOnClickListener {
            editingPosition = -1
            galleryLauncher.launch("image/*")
        }

        binding.btnSavePig.setOnClickListener {
            saveBatch()
        }
    }

    private fun observeViewModel() {
        // Load initial data from persistence
        viewModel.loadData(requireContext())

        viewModel.pigList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }
    }

    private fun toggleAddCard() {
        isAddCardVisible = !isAddCardVisible
        binding.cardAddPig.visibility = if (isAddCardVisible) View.VISIBLE else View.GONE
        binding.btnShowAddCard.setImageResource(
            if (isAddCardVisible) android.R.drawable.ic_menu_close_clear_cancel 
            else R.drawable.ic_db_add_circle
        )
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            if (editingPosition == -1) {
                // New pig upload
                tempBitmap = bitmap
                binding.ivPreviewNewPig.setImageBitmap(bitmap)
                binding.ivPreviewNewPig.visibility = View.VISIBLE
            } else {
                // Edit existing pig
                viewModel.updatePigImage(requireContext(), editingPosition, bitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBatch() {
        val batchName = binding.etBatchNameInput.text.toString().trim()
        // Now calling saveNewBatch with batchName instead of ID
        viewModel.saveNewBatch(requireContext(), batchName, tempBitmap)
        resetAddForm()
        Toast.makeText(requireContext(), "Batch Saved Successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun resetAddForm() {
        binding.etBatchNameInput.setText("")
        binding.ivPreviewNewPig.visibility = View.GONE
        tempBitmap = null
        if (isAddCardVisible) toggleAddCard()
    }

    override fun onResume() {
        super.onResume()
        if (::profileManager.isInitialized) {
            profileManager.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

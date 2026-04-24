package com.example.smart_hog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream

class ProfileManager(private val fragment: Fragment, private val profileImageView: ImageView) {

    private val context: Context get() = fragment.requireContext()
    private val activity: Activity get() = fragment.requireActivity()

    private val cameraLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            photo?.let { saveAndSetProfileImage(it) }
        }
    }

    private val galleryLauncher = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.let { b -> saveAndSetProfileImage(b) }
            }
        }
    }

    private val permissionLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera()
        else Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    fun init() {
        loadProfileImage()
        profileImageView.setOnClickListener { showImagePicker() }
    }

    fun refresh() {
        loadProfileImage()
    }

    private fun showImagePicker() {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_image_picker, null)
        
        view.findViewById<LinearLayout>(R.id.option_camera).setOnClickListener {
            if (checkCameraPermission()) openCamera()
            else permissionLauncher.launch(Manifest.permission.CAMERA)
            dialog.dismiss()
        }
        
        view.findViewById<LinearLayout>(R.id.option_gallery).setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun checkCameraPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun openCamera() {
        cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    private fun openGallery() {
        galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    private fun saveAndSetProfileImage(bitmap: Bitmap) {
        val loading = LoadingUtils.showLoading(context)
        Handler(Looper.getMainLooper()).postDelayed({
            profileImageView.setImageBitmap(bitmap)
            val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            prefs.edit().putString("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)).apply()
            loading.dismiss()
            Toast.makeText(context, "Profile Photo Updated!", Toast.LENGTH_SHORT).show()
        }, 800)
    }

    private fun loadProfileImage() {
        val prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val imageString = prefs.getString("image", null)
        if (imageString != null) {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            profileImageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        }
    }
}

package com.example.smart_hog

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvContact: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvUsername: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnEditPhoto: View

    private val CAMERA_REQUEST = 100
    private val GALLERY_REQUEST = 102
    private val CAMERA_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        imageView = findViewById(R.id.profileImage)
        tvUsername = findViewById(R.id.tvUsername)
        tvFullName = findViewById(R.id.tvFullName)
        tvContact = findViewById(R.id.tvContact)
        tvAddress = findViewById(R.id.tvAddress)
        btnBack = findViewById(R.id.btnBack)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)

        loadProfileData()

        btnBack.setOnClickListener { finish() }

        btnEditPhoto.setOnClickListener {
            showModernImagePicker()
        }
    }

    private fun showModernImagePicker() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_image_picker, null)
        
        view.findViewById<LinearLayout>(R.id.option_camera).setOnClickListener {
            if (checkCameraPermission()) openCamera()
            dialog.dismiss()
        }
        
        view.findViewById<LinearLayout>(R.id.option_gallery).setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            return false
        }
        return true
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    photo?.let { saveProfileImage(it) }
                }
                GALLERY_REQUEST -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let { uri ->
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        bitmap?.let { saveProfileImage(it) }
                    }
                }
            }
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        val loading = LoadingUtils.showLoading(this)
        Handler(Looper.getMainLooper()).postDelayed({
            imageView.setImageBitmap(bitmap)
            val prefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            prefs.edit().putString("image", Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)).apply()
            loading.dismiss()
            Toast.makeText(this, "Profile Photo Updated!", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val loginPrefs = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        
        val defaultEmail = loginPrefs.getString("user_email", "owner@smarthog.com")
        val defaultName = defaultEmail?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: "Silas Farm Owner"

        tvUsername.text = prefs.getString("username", defaultName)
        tvFullName.text = prefs.getString("name", defaultName)
        tvContact.text = prefs.getString("contact", "+63 912 345 6789")
        tvAddress.text = prefs.getString("address", "Purok 5, Smart Hog Village")
        
        val imageString = prefs.getString("image", null)
        if (imageString != null) {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        }
    }
}

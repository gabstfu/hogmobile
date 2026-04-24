package com.example.smart_hog

import android.graphics.Bitmap

data class Pig(
    val id: String,
    var batch: String,
    var weight: Double,
    var status: String,
    var birthDate: String? = null,
    var ageDays: Int = 0,
    var feedingTime: String? = null,
    var feedingAmount: Double = 0.0,
    var lastVaccinationDate: String? = null,
    var nextVaccinationDate: String? = null,
    var healthNotes: String? = null,
    var imageBitmap: Bitmap? = null,
    var imageResId: Int = R.drawable.pig4,
    var imagePath: String? = null,
    var isAlertEnabled: Boolean = true
)

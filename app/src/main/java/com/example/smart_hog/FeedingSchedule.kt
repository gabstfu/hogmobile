package com.example.smart_hog

import com.google.gson.annotations.SerializedName

data class FeedingSchedule(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("batch_code")
    val batchCode: String,

    @SerializedName("feed_quantity")
    val feedQuantity: Double,

    @SerializedName("feed_time")
    val feedTime: String,

    @SerializedName("feed_type")
    val feedType: String = "Starter",

    @SerializedName("growth_code")
    val growthCode: String = "GC001",

    @SerializedName("device_code")
    val deviceCode: String = "DEV001",

    @SerializedName("pen_code")
    val penCode: String = "PEN001",

    @SerializedName("datetime")
    val datetime: String,

    @SerializedName("days_of_week")
    val daysOfWeek: List<String>? = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("start_date")
    val startDate: String? = null
)
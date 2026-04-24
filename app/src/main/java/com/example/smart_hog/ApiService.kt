package com.example.smart_hog

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

data class BatchItem(
    @SerializedName("batch_code")
    val batchCode: String,
    @SerializedName("batch_name")
    val batchName: String,
    @SerializedName("no_of_pigs")
    val noOfPigs: Int,
    @SerializedName("avg_weight")
    val avgWeight: Double,
    @SerializedName("current_age")
    val currentAge: Int,
    @SerializedName("status")
    val status: String? = "Active"
)

data class BatchListResponse(
    val message: String,
    val count: Int,
    val data: List<BatchItem>
)

data class DashboardOverview(
    @SerializedName("total_pigs")
    val totalPigs: Int,
    @SerializedName("active_batches")
    val activeBatches: Int,
    @SerializedName("total_feed_today")
    val totalFeedToday: Double,
    @SerializedName("avg_weight_today")
    val avgWeightToday: Double
)

data class DashboardOverviewResponse(
    val status: String,
    val results: DashboardOverview
)

data class GrowthTrendPoint(
    @SerializedName("sample_date")
    val sampleDate: String,
    @SerializedName("avg_weight")
    val avgWeight: Double,
    @SerializedName("pig_age_days")
    val pigAgeDays: Int
)

data class GrowthTrendBatch(
    @SerializedName("batch_code")
    val batchCode: String,
    val series: List<GrowthTrendPoint>
)

data class GrowthTrendsResponse(
    val status: String,
    val results: List<GrowthTrendBatch>
)

data class FeedConsumptionBatch(
    @SerializedName("batch_code")
    val batchCode: String,
    @SerializedName("total_feed_quantity")
    val totalFeedQuantity: Double
)

data class FeedConsumptionResponse(
    val status: String,
    val results: List<FeedConsumptionBatch>
)

data class ReportsSummary(
    @SerializedName("reports_generated")
    val reportsGenerated: Int,
    @SerializedName("critical_findings")
    val criticalFindings: Int,
    @SerializedName("average_efficiency_percent")
    val averageEfficiencyPercent: Int
)

data class ReportsSummaryResponse(
    val status: String,
    val results: ReportsSummary
)

// Data Mining Record for Analytics
data class DataminingRecord(
    @SerializedName("batch_code") val batchCode: String,
    @SerializedName("pig_age_days") val age: Int,
    @SerializedName("avg_weight") val weight: Double,
    @SerializedName("sample_date") val date: String? = null
)

data class DataminingAllResponse(
    val message: String,
    val count: Int,
    val data: List<DataminingRecord>
)

interface ApiService {

    @FormUrlEncoded
    @POST("auth/login/")
    fun login(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Map<String, Any>>

    @GET("feeding/all/")
    fun getSchedules(): Call<List<FeedingSchedule>>

    @POST("feeding/add/")
    fun createSchedule(@Body schedule: FeedingSchedule): Call<Map<String, Any>>

    @GET("batch/all/")
    fun getBatches(): Call<BatchListResponse>

    @GET("api/dashboard/overview/")
    fun getDashboardOverview(): Call<DashboardOverviewResponse>

    @GET("api/dashboard/growth-trends/")
    fun getGrowthTrends(): Call<GrowthTrendsResponse>

    @GET("api/dashboard/feed-consumption/")
    fun getFeedConsumption(): Call<FeedConsumptionResponse>

    @GET("api/reports/summary/")
    fun getReportsSummary(): Call<ReportsSummaryResponse>

    @GET("api/datamining/all/")
    fun getDataminingAll(): Call<DataminingAllResponse>

    @GET("datamining/")
    fun getLiveDatamining(): Call<List<DataminingRecord>>

    // --- NEW: Sync Monitor Data with Web ---
    @GET("pigs/all/")
    fun getAllPigs(): Call<List<Pig>>

    @POST("pigs/add/")
    fun addPig(@Body pig: Pig): Call<Map<String, Any>>

    @PUT("pigs/update/{id}/")
    fun updatePig(@Path("id") id: String, @Body pig: Pig): Call<Map<String, Any>>
}

package com.example.smart_hog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MonitorViewModel : ViewModel() {

    private val _pigList = MutableLiveData<MutableList<Pig>>()
    val pigList: LiveData<MutableList<Pig>> get() = _pigList

    private val _batchOverview = MutableLiveData<DashboardOverview>()
    val batchOverview: LiveData<DashboardOverview> get() = _batchOverview

    private val _growthTrends = MutableLiveData<List<GrowthTrendBatch>>()
    val growthTrends: LiveData<List<GrowthTrendBatch>> get() = _growthTrends

    private val _apiBatches = MutableLiveData<List<BatchItem>>()
    val apiBatches: LiveData<List<BatchItem>> get() = _apiBatches

    private val _reportsSummary = MutableLiveData<ReportsSummary>()
    val reportsSummary: LiveData<ReportsSummary> get() = _reportsSummary

    private val _feedConsumption = MutableLiveData<List<FeedConsumptionBatch>>()
    val feedConsumption: LiveData<List<FeedConsumptionBatch>> get() = _feedConsumption

    private val _dataminingRecords = MutableLiveData<List<DataminingRecord>>()
    val dataminingRecords: LiveData<List<DataminingRecord>> get() = _dataminingRecords

    private val gson = Gson()

    fun fetchApiData() {
        RetrofitClient.instance.getAllPigs().enqueue(object : Callback<List<Pig>> {
            override fun onResponse(call: Call<List<Pig>>, response: Response<List<Pig>>) {
                if (response.isSuccessful) {
                    _pigList.value = response.body()?.toMutableList()
                }
            }
            override fun onFailure(call: Call<List<Pig>>, t: Throwable) {}
        })

        RetrofitClient.instance.getDashboardOverview().enqueue(object : Callback<DashboardOverviewResponse> {
            override fun onResponse(call: Call<DashboardOverviewResponse>, response: Response<DashboardOverviewResponse>) {
                if (response.isSuccessful) {
                    _batchOverview.value = response.body()?.results
                }
            }
            override fun onFailure(call: Call<DashboardOverviewResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getBatches().enqueue(object : Callback<BatchListResponse> {
            override fun onResponse(call: Call<BatchListResponse>, response: Response<BatchListResponse>) {
                if (response.isSuccessful) {
                    _apiBatches.value = response.body()?.data
                }
            }
            override fun onFailure(call: Call<BatchListResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getGrowthTrends().enqueue(object : Callback<GrowthTrendsResponse> {
            override fun onResponse(call: Call<GrowthTrendsResponse>, response: Response<GrowthTrendsResponse>) {
                if (response.isSuccessful) {
                    _growthTrends.value = response.body()?.results
                }
            }
            override fun onFailure(call: Call<GrowthTrendsResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getReportsSummary().enqueue(object : Callback<ReportsSummaryResponse> {
            override fun onResponse(call: Call<ReportsSummaryResponse>, response: Response<ReportsSummaryResponse>) {
                if (response.isSuccessful) {
                    _reportsSummary.value = response.body()?.results
                }
            }
            override fun onFailure(call: Call<ReportsSummaryResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getFeedConsumption().enqueue(object : Callback<FeedConsumptionResponse> {
            override fun onResponse(call: Call<FeedConsumptionResponse>, response: Response<FeedConsumptionResponse>) {
                if (response.isSuccessful) {
                    _feedConsumption.value = response.body()?.results
                }
            }
            override fun onFailure(call: Call<FeedConsumptionResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getDataminingAll().enqueue(object : Callback<DataminingAllResponse> {
            override fun onResponse(call: Call<DataminingAllResponse>, response: Response<DataminingAllResponse>) {
                if (response.isSuccessful) {
                    // We can keep this if needed or just use the live one
                }
            }
            override fun onFailure(call: Call<DataminingAllResponse>, t: Throwable) {}
        })

        RetrofitClient.instance.getLiveDatamining().enqueue(object : Callback<List<DataminingRecord>> {
            override fun onResponse(call: Call<List<DataminingRecord>>, response: Response<List<DataminingRecord>>) {
                if (response.isSuccessful) {
                    _dataminingRecords.value = response.body()
                }
            }
            override fun onFailure(call: Call<List<DataminingRecord>>, t: Throwable) {}
        })
    }

    fun loadData(context: Context) {
        val prefs = context.getSharedPreferences("pig_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("pig_list", null)
        
        val list = try {
            if (json != null) {
                val type = object : TypeToken<MutableList<Pig>>() {}.type
                val loadedList: MutableList<Pig> = gson.fromJson(json, type)
                
                // Reload bitmaps from paths
                loadedList.forEach { pig ->
                    pig.imagePath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            pig.imageBitmap = BitmapFactory.decodeFile(path)
                        }
                    }
                }
                loadedList
            } else {
                getDefaultPigList()
            }
        } catch (e: Exception) {
            // If data is corrupted (e.g. NumberFormatException), reset to default
            prefs.edit().remove("pig_list").apply()
            getDefaultPigList()
        }
        _pigList.value = list
    }

    private fun getDefaultPigList(): MutableList<Pig> {
        return mutableListOf(
            Pig("#PG-1024", "B-2024-MAR", 85.0, "Ahead"),
            Pig("#PG-1025", "B-2024-MAR", 82.0, "Normal"),
            Pig("#PG-1026", "B-2024-MAR", 80.0, "Below")
        )
    }

    private fun saveData(context: Context) {
        val list = _pigList.value ?: return
        val prefs = context.getSharedPreferences("pig_prefs", Context.MODE_PRIVATE)
        val json = gson.toJson(list)
        prefs.edit().putString("pig_list", json).apply()
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): String {
        val file = File(context.filesDir, "pigs")
        if (!file.exists()) file.mkdirs()
        
        val imageFile = File(file, "$fileName.jpg")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return imageFile.absolutePath
    }

    fun saveNewBatch(context: Context, batchName: String, image: Bitmap?) {
        val currentList = _pigList.value?.toMutableList() ?: mutableListOf()
        
        // Automatic ID generation: #PG-1024 + next index
        val nextNum = 1024 + currentList.size + 1
        val autoId = "#PG-$nextNum"

        // Use the batch name from user input
        val finalBatchName = if (batchName.isBlank()) "UNNAMED BATCH" else batchName.uppercase()

        // Register this batch name in the repository so it shows in the schedule dropdown
        BatchRepository.addBatch(context, finalBatchName)

        val newPig = Pig(autoId, finalBatchName, 0.0, "New")
        
        if (image != null) {
            val path = saveBitmapToFile(context, image, "pig_${System.currentTimeMillis()}")
            newPig.imagePath = path
            newPig.imageBitmap = image
        }

        // --- OPTIMISTIC UPDATE: Add locally first so user sees it immediately ---
        currentList.add(0, newPig)
        _pigList.value = currentList
        saveData(context)

        // --- SYNC TO WEB ---
        RetrofitClient.instance.addPig(newPig).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                // Server sync successful
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                // Handle failure if needed
            }
        })
    }

    fun updatePigData(context: Context, pig: Pig) {
        RetrofitClient.instance.updatePig(pig.id, pig).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    // Successfully updated on web
                    val currentList = _pigList.value ?: return
                    val index = currentList.indexOfFirst { it.id == pig.id }
                    if (index != -1) {
                        currentList[index] = pig
                        _pigList.postValue(currentList)
                        saveData(context)
                    }
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
        })
    }

    fun updatePigImage(context: Context, position: Int, bitmap: Bitmap) {
        val currentList = _pigList.value ?: return
        if (position in currentList.indices) {
            val path = saveBitmapToFile(context, bitmap, "pig_${System.currentTimeMillis()}")
            currentList[position].imagePath = path
            currentList[position].imageBitmap = bitmap
            _pigList.value = currentList
            saveData(context)
        }
    }
}

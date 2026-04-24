package com.example.smart_hog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import java.util.Locale
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class AnalyticsFragment : Fragment() {

    private lateinit var profileManager: ProfileManager
    private val monitorViewModel: MonitorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Profile Management
        val profileIcon = view.findViewById<ImageView>(R.id.profile_icon_analytics)
        if (profileIcon != null) {
            profileManager = ProfileManager(this, profileIcon)
            profileManager.init()
        }

        // Back Button Functionality
        val btnBack = view.findViewById<ImageButton>(R.id.btn_back_analytics)
        btnBack?.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // Observe Data from MonitorViewModel
        monitorViewModel.apiBatches.observe(viewLifecycleOwner) {
            updateUI(view)
        }
        monitorViewModel.batchOverview.observe(viewLifecycleOwner) {
            updateUI(view)
        }
        monitorViewModel.growthTrends.observe(viewLifecycleOwner) {
            updateUI(view)
        }
        monitorViewModel.feedConsumption.observe(viewLifecycleOwner) {
            updateUI(view)
        }
        monitorViewModel.dataminingRecords.observe(viewLifecycleOwner) {
            updateUI(view)
        }

        monitorViewModel.loadData(requireContext())
        monitorViewModel.fetchApiData()
    }

    private fun updateUI(view: View) {
        val tvPopulation = view.findViewById<TextView>(R.id.tv_population)
        val tvTotalFeed = view.findViewById<TextView>(R.id.tv_total_feed)
        val feedProgressBar = view.findViewById<ProgressBar>(R.id.feed_progress_bar)
        val tvEstWeightGain = view.findViewById<TextView>(R.id.tv_est_weight_gain)
        val tvActiveBatchesCount = view.findViewById<TextView>(R.id.tv_active_batches_count)
        val tvBatchesTracked = view.findViewById<TextView>(R.id.tv_batches_tracked)
        val tvPeakBatch = view.findViewById<TextView>(R.id.tv_peak_batch)
        val tvPeakWeight = view.findViewById<TextView>(R.id.tv_peak_weight)
        val tvAvgSystemGain = view.findViewById<TextView>(R.id.tv_avg_system_gain)

        val overview = monitorViewModel.batchOverview.value
        val batches = monitorViewModel.apiBatches.value
        val growth = monitorViewModel.growthTrends.value
        val feedData = monitorViewModel.feedConsumption.value
        val datamining = monitorViewModel.dataminingRecords.value

        // 1. Population & Batch Stats
        val activeBatchCount = batches?.size ?: 0
        val population = overview?.totalPigs ?: batches?.sumOf { it.noOfPigs } ?: 10
        tvPopulation.text = population.toString()
        tvActiveBatchesCount.text = "• $activeBatchCount Active Batches"
        tvBatchesTracked.text = "$activeBatchCount Active"

        // 2. Feed & Weight (Dynamic from Overview & Consumption Routes)
        val avgWeight = overview?.avgWeightToday ?: (batches?.map { it.avgWeight }?.average() ?: 0.0)
        
        // Use total_feed_today from dashboard/overview if available, otherwise sum the batches
        val totalFeedToday = overview?.totalFeedToday ?: feedData?.sumOf { it.totalFeedQuantity } ?: 0.0
        
        tvTotalFeed.text = String.format(Locale.US, "%,.1f KG", totalFeedToday)
        tvEstWeightGain.text = String.format(Locale.US, "%,.1f KG", avgWeight)
        
        // Dynamic Progress Bar (assume 100kg is "full" or calculate based on total population)
        val feedProgress = ((totalFeedToday / (population * 2.0)) * 100).coerceAtMost(100.0).toInt()
        view.findViewById<ProgressBar>(R.id.feed_progress_bar)?.progress = if (feedProgress > 0) feedProgress else 15

        // 3. Peak Performance & Gain Logic
        if (growth != null && growth.isNotEmpty()) {
            val peakBatch = growth.maxByOrNull { it.series.maxOfOrNull { p -> p.avgWeight } ?: 0.0 }
            val peakWeight = peakBatch?.series?.maxOfOrNull { it.avgWeight } ?: 24.0
            
            tvPeakBatch.text = peakBatch?.batchCode ?: "---"
            tvPeakWeight.text = String.format(Locale.US, "%.1f kg", peakWeight)
            
            val avgGain = growth.flatMap { it.series }.map { it.avgWeight }.average()
            tvAvgSystemGain.text = String.format(Locale.US, "%.1f kg", if (avgGain.isNaN()) 0.0 else avgGain)
        }

        // 4. Growth Intelligence Chart
        view.findViewById<ComposeView>(R.id.chart_compose_view)?.apply {
            setContent { 
                val chartData = if (!datamining.isNullOrEmpty()) {
                    datamining.groupBy { it.batchCode }.map { (code, records) ->
                        GrowthTrendBatch(
                            batchCode = code,
                            series = records.map { GrowthTrendPoint(it.date ?: "", it.weight, it.age) }
                        )
                    }
                } else {
                    growth ?: emptyList()
                }
                GrowthIntelligenceChart(chartData)
            }
        }
    }

    @Composable
    fun GrowthIntelligenceChart(batches: List<GrowthTrendBatch>) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val width = size.width
            val height = size.height
            
            // Draw Grid Lines
            val gridLines = 5
            for (i in 0..gridLines) {
                val y = height - (i * height / gridLines)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (batches.isEmpty()) return@Canvas

            val colors = listOf(Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800))
            val allPoints = batches.flatMap { it.series }
            val maxWeight = allPoints.maxOfOrNull { it.avgWeight }?.toFloat()?.coerceAtLeast(1f) ?: 1f
            val maxDays = allPoints.maxOfOrNull { it.pigAgeDays }?.coerceAtLeast(1) ?: 1

            batches.forEachIndexed { batchIndex, batch ->
                val color = colors[batchIndex % colors.size]
                val points = batch.series.sortedBy { it.pigAgeDays }
                
                if (points.isNotEmpty()) {
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = (point.pigAgeDays.toFloat() / maxDays) * width
                        val y = height - (point.avgWeight.toFloat() / maxWeight) * height
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = color, style = Stroke(width = 3.dp.toPx()))
                    points.forEach { point ->
                        val x = (point.pigAgeDays.toFloat() / maxDays) * width
                        val y = height - (point.avgWeight.toFloat() / maxWeight) * height
                        drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::profileManager.isInitialized) {
            profileManager.refresh()
        }
    }
}

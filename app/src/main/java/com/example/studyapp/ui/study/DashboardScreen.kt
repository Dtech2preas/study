package com.example.studyapp.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val todayDuration by viewModel.getTodayDuration().collectAsState(initial = 0L)
    val weekDuration by viewModel.getWeekDuration().collectAsState(initial = 0L)
    val monthDuration by viewModel.getMonthDuration().collectAsState(initial = 0L)
    val yearDuration by viewModel.getYearDuration().collectAsState(initial = 0L)
    val allTimeDuration by viewModel.getAllTimeDuration().collectAsState(initial = 0L)

    val longestSession by viewModel.getLongestSession().collectAsState(initial = 0L)
    val bestStudyDay by viewModel.getBestStudyDay().collectAsState(initial = null)
    val averageDaily by viewModel.getAverageDailyStudyTime().collectAsState(initial = 0L)
    val streak by viewModel.getCurrentStreak().collectAsState(initial = 0)

    val last7Days by viewModel.getLast7DaysStudyTime().collectAsState()

    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(last7Days) {
        if (last7Days.isNotEmpty()) {
            val entries = last7Days.mapIndexed { index, dailyTotal ->
                FloatEntry(x = index.toFloat(), y = dailyTotal.totalDuration / 3600f)
            }
            chartEntryModelProducer.setEntries(entries)
        } else {
            // Default empty entries so it doesn't crash if no data
            chartEntryModelProducer.setEntries(listOf(FloatEntry(0f, 0f)))
        }
    }

    val aiMotivation = """
        > **⚡ SYSTEM STATUS:** OPTIMAL
        >
        > Neural pathways primed for learning. Current objective: *Mastery*.
        >
        > "The future belongs to those who learn more skills and combine them in creative ways."
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Study Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // AI Motivation Card using RichTextView
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Fixed height to allow scrolling within if needed, or just enough to show content
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                RichTextView(
                    markdown = aiMotivation,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Vico Line Chart (Last 7 Days)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Last 7 Days (Hours)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (last7Days.isNotEmpty()) {
                    Chart(
                        chart = lineChart(),
                        chartModelProducer = chartEntryModelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index >= 0 && index < last7Days.size) {
                                    val date = Date(last7Days[index].date)
                                    val format = SimpleDateFormat("EEE", Locale.getDefault())
                                    format.format(date)
                                } else {
                                    ""
                                }
                            }
                        )
                    )
                } else {
                    Text("No data for the last 7 days yet.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Text(
            "Advanced Stats",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Advanced Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Current Streak", value = "$streak Days", modifier = Modifier.weight(1f))
            StatCard(
                title = "Best Day",
                value = bestStudyDay?.let { formatDuration(it.totalDuration) } ?: "0h 0m",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Longest Session",
                value = formatDuration(longestSession ?: 0L),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Daily Average",
                value = formatDuration(averageDaily ?: 0L),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Totals",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatRow("Today", todayDuration ?: 0L)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("This Week", weekDuration ?: 0L)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("This Month", monthDuration ?: 0L)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("This Year", yearDuration ?: 0L)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StatRow("All Time", allTimeDuration ?: 0L)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
fun StatRow(label: String, seconds: Long) {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp)
        Text(text = "${hours}h ${minutes}m", fontSize = 18.sp)
    }
}

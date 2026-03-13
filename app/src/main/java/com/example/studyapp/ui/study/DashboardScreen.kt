package com.example.studyapp.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val todayDuration by viewModel.getTodayDuration().collectAsState(initial = 0L)
    val weekDuration by viewModel.getWeekDuration().collectAsState(initial = 0L)
    val monthDuration by viewModel.getMonthDuration().collectAsState(initial = 0L)
    val allTimeDuration by viewModel.getAllTimeDuration().collectAsState(initial = 0L)

    // Convert durations from seconds to hours for the chart
    val todayHours = (todayDuration ?: 0L) / 3600f
    val weekHours = (weekDuration ?: 0L) / 3600f
    val monthHours = (monthDuration ?: 0L) / 3600f
    val allTimeHours = (allTimeDuration ?: 0L) / 3600f

    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(todayHours, weekHours, monthHours, allTimeHours) {
        chartEntryModelProducer.setEntries(
            listOf(
                entryOf(0f, todayHours),
                entryOf(1f, weekHours),
                entryOf(2f, monthHours),
                entryOf(3f, allTimeHours)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Study Dashboard", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        // Vico Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Study Time (Hours)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                Chart(
                    chart = columnChart(),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            when (value.toInt()) {
                                0 -> "Today"
                                1 -> "Week"
                                2 -> "Month"
                                3 -> "All Time"
                                else -> ""
                            }
                        }
                    )
                )
            }
        }

        StatRow("Today", todayDuration ?: 0L)
        StatRow("This Week", weekDuration ?: 0L)
        StatRow("This Month", monthDuration ?: 0L)
        StatRow("All Time", allTimeDuration ?: 0L)
    }
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

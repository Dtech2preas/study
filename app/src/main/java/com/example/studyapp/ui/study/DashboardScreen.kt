package com.example.studyapp.ui.study

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyapp.data.local.DailyStudyTotal
import com.example.studyapp.data.local.StudyDatabase
import com.example.studyapp.data.repository.StudyRepository
import com.example.studyapp.ui.theme.ElectricBlue
import com.example.studyapp.ui.theme.PremiumBlack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val streak by viewModel.getCurrentStreak().collectAsState()
    val bestStudyDay by viewModel.getBestStudyDay().collectAsState(initial = null)
    val longestSession by viewModel.getLongestSession().collectAsState(initial = null)
    val averageDaily by viewModel.getAverageDailyStudyTime().collectAsState(initial = null)

    val todayDuration by viewModel.getTodayDuration().collectAsState(initial = null)
    val weekDuration by viewModel.getWeekDuration().collectAsState(initial = null)
    val monthDuration by viewModel.getMonthDuration().collectAsState(initial = null)
    val yearDuration by viewModel.getYearDuration().collectAsState(initial = null)
    val allTimeDuration by viewModel.getAllTimeDuration().collectAsState(initial = null)

    val last7DaysData by viewModel.getLast7DaysStudyTime().collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Study Dashboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Activity (Last 7 Days)", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    StudyChart(last7DaysData)
                }
            }
        }

        item {
            Text("Performance Metrics", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard("🔥 Current Streak", "$streak Days", Modifier.weight(1f))
                MetricCard("⏱️ Longest Session", formatDuration(longestSession ?: 0L), Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard("⭐ Best Day", formatDuration(bestStudyDay?.totalDuration ?: 0L), Modifier.weight(1f))
                MetricCard("📊 Daily Average", formatDuration(averageDaily ?: 0L), Modifier.weight(1f))
            }
        }

        item {
            Text("Time Distribution", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TimeRow("Today", formatDuration(todayDuration ?: 0L))
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    TimeRow("This Week", formatDuration(weekDuration ?: 0L))
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    TimeRow("This Month", formatDuration(monthDuration ?: 0L))
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    TimeRow("This Year", formatDuration(yearDuration ?: 0L))
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))
                    TimeRow("All Time", formatDuration(allTimeDuration ?: 0L))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = ElectricBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TimeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = ElectricBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(ElectricBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun StudyChart(data: List<DailyStudyTotal>) {
    // A custom Canvas line chart replacing the WebView Chart.js
    val chartHeight = 200.dp

    if (data.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(chartHeight), contentAlignment = Alignment.Center) {
            Text("No study data in the last 7 days", color = Color.Gray)
        }
        return
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
        val maxDuration = data.maxOfOrNull { it.totalDuration }?.toFloat() ?: 1f
        val safeMax = if (maxDuration == 0f) 1f else maxDuration

        val width = size.width
        val height = size.height

        val pointSpacing = width / (data.size + 1)

        val points = data.mapIndexed { index, dailyStudyTotal ->
            val x = pointSpacing * (index + 1)
            val y = height - ((dailyStudyTotal.totalDuration.toFloat() / safeMax) * height)
            Offset(x, y)
        }

        // Draw Line
        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                // simple curve or straight line. Let's do straight lines with rounded points for simplicity and clean look
                path.lineTo(points[i].x, points[i].y)
            }

            drawPath(
                path = path,
                color = ElectricBlue,
                style = Stroke(width = 4.dp.toPx())
            )

            // Fill area under line
            val fillPath = Path()
            fillPath.addPath(path)
            fillPath.lineTo(points.last().x, height)
            fillPath.lineTo(points.first().x, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Points
            points.forEach { point ->
                drawCircle(
                    color = PremiumBlack,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = ElectricBlue,
                    radius = 6.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

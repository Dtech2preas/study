package com.studyapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyapp.data.StudyRepository
import com.studyapp.data.StudySession
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun DashboardScreen(repository: StudyRepository) {
    val totalTime by repository.totalStudyTime.collectAsState(initial = 0L)

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    val weekCalendar = Calendar.getInstance()
    weekCalendar.firstDayOfWeek = Calendar.MONDAY
    weekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    weekCalendar.set(Calendar.HOUR_OF_DAY, 0)
    weekCalendar.set(Calendar.MINUTE, 0)
    weekCalendar.set(Calendar.SECOND, 0)
    weekCalendar.set(Calendar.MILLISECOND, 0)
    val weekStart = weekCalendar.timeInMillis

    val monthCalendar = Calendar.getInstance()
    monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
    monthCalendar.set(Calendar.HOUR_OF_DAY, 0)
    monthCalendar.set(Calendar.MINUTE, 0)
    monthCalendar.set(Calendar.SECOND, 0)
    monthCalendar.set(Calendar.MILLISECOND, 0)
    val monthStart = monthCalendar.timeInMillis

    val todayTime by repository.getStudyTimeSince(todayStart).collectAsState(initial = 0L)
    val weekTime by repository.getStudyTimeSince(weekStart).collectAsState(initial = 0L)
    val monthTime by repository.getStudyTimeSince(monthStart).collectAsState(initial = 0L)

    val allSessions by repository.allSessions.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Study Dashboard", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Study Time: ${formatDuration(totalTime ?: 0L)}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Today's Study Time: ${formatDuration(todayTime ?: 0L)}", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (totalTime != null && totalTime!! > 0) {
            val maxTime = Math.max(monthTime ?: 0L, totalTime ?: 1L)

            Text("Study Comparison", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChartBar("Today", todayTime ?: 0L, maxTime)
                    ChartBar("Week", weekTime ?: 0L, maxTime)
                    ChartBar("Month", monthTime ?: 0L, maxTime)
                    ChartBar("Total", totalTime ?: 0L, maxTime)
                }
            }
        }

        Text("Recent Sessions", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allSessions) { session ->
                SessionItem(session)
            }
        }
    }
}

@Composable
fun ChartBar(label: String, value: Long, max: Long) {
    val fraction = if (max > 0) value.toFloat() / max.toFloat() else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, modifier = Modifier.width(60.dp), style = MaterialTheme.typography.bodySmall)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(text = formatDuration(value), modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
    }
}


@Composable
fun SessionItem(session: StudySession) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val date = Date(session.startTime)
            val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Text(text = format.format(date))
            Text(text = formatDuration(session.duration))
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

package com.studyapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyapp.data.StudySession
import com.studyapp.data.StudyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudyTrackerScreen(repository: StudyRepository) {
    var isTracking by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var elapsedTime by remember { mutableStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    // Timer effect
    LaunchedEffect(isTracking) {
        while (isTracking) {
            delay(1000L)
            elapsedTime = System.currentTimeMillis() - startTime
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Study Tracker",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Format time mm:ss
        val minutes = (elapsedTime / 1000) / 60
        val seconds = (elapsedTime / 1000) % 60
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!isTracking) {
            Button(onClick = {
                startTime = System.currentTimeMillis()
                elapsedTime = 0L
                isTracking = true
            }) {
                Text("Start Studying")
            }
        } else {
            Button(
                onClick = {
                    isTracking = false
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    coroutineScope.launch {
                        repository.insert(StudySession(
                            startTime = startTime,
                            endTime = endTime,
                            duration = duration
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Stop & Save")
            }
        }
    }
}

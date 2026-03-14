package com.example.studyapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.studyapp.alarms.AlarmScheduler
import com.example.studyapp.data.preferences.SettingsPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsPreferences = remember { SettingsPreferences(context) }
    val alarmScheduler = remember { AlarmScheduler(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted
            } else {
                // Permission denied - handle appropriately if needed
            }
        }
    )

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var apiKey by remember { mutableStateOf(settingsPreferences.getApiKey() ?: "") }
    var name by remember { mutableStateOf(settingsPreferences.getName()) }
    var customGoalsEnabled by remember { mutableStateOf(settingsPreferences.isCustomGoalsEnabled()) }
    var alarmsEnabled by remember { mutableStateOf(settingsPreferences.isAlarmsEnabled()) }
    var notificationsEnabled by remember { mutableStateOf(settingsPreferences.isNotificationsEnabled()) }

    var saveStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            SettingsSection(title = "Profile") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            SettingsSection(title = "Daily Study Goals") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Custom Daily Goals", modifier = Modifier.weight(1f))
                    Switch(
                        checked = customGoalsEnabled,
                        onCheckedChange = { customGoalsEnabled = it }
                    )
                }

                if (customGoalsEnabled) {
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    val dayIndexes = listOf(2, 3, 4, 5, 6, 7, 1) // Calendar constants

                    days.forEachIndexed { index, dayName ->
                        val dayIndex = dayIndexes[index]
                        var hours by remember { mutableStateOf(settingsPreferences.getDailyGoal(dayIndex).toString()) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(dayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = hours,
                                onValueChange = {
                                    hours = it
                                    it.toIntOrNull()?.let { h -> settingsPreferences.saveDailyGoal(dayIndex, h) }
                                },
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                            )
                            Text(" hrs", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Alarms") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Alarms", modifier = Modifier.weight(1f))
                    Switch(
                        checked = alarmsEnabled,
                        onCheckedChange = {
                            alarmsEnabled = it
                            if (it) requestNotificationPermission()
                            settingsPreferences.saveAlarmsEnabled(it)
                            alarmScheduler.scheduleAll()
                        }
                    )
                }

                if (alarmsEnabled) {
                    // Simple input for wake time
                    var wakeHour by remember { mutableStateOf(settingsPreferences.getWakeAlarmTime().first.toString()) }
                    var wakeMin by remember { mutableStateOf(settingsPreferences.getWakeAlarmTime().second.toString()) }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Wake Up Alarm", modifier = Modifier.weight(1f))
                        OutlinedTextField(value = wakeHour, onValueChange = { wakeHour = it }, modifier = Modifier.width(60.dp), singleLine = true)
                        Text(":", modifier = Modifier.padding(horizontal = 4.dp))
                        OutlinedTextField(value = wakeMin, onValueChange = { wakeMin = it }, modifier = Modifier.width(60.dp), singleLine = true)
                    }

                    // Simple input for study time
                    var studyHour by remember { mutableStateOf(settingsPreferences.getStudyAlarmTime().first.toString()) }
                    var studyMin by remember { mutableStateOf(settingsPreferences.getStudyAlarmTime().second.toString()) }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Study Alarm", modifier = Modifier.weight(1f))
                        OutlinedTextField(value = studyHour, onValueChange = { studyHour = it }, modifier = Modifier.width(60.dp), singleLine = true)
                        Text(":", modifier = Modifier.padding(horizontal = 4.dp))
                        OutlinedTextField(value = studyMin, onValueChange = { studyMin = it }, modifier = Modifier.width(60.dp), singleLine = true)
                    }

                    Button(
                        onClick = {
                            val wH = wakeHour.toIntOrNull() ?: 5
                            val wM = wakeMin.toIntOrNull() ?: 45
                            val sH = studyHour.toIntOrNull() ?: 18
                            val sM = studyMin.toIntOrNull() ?: 0
                            settingsPreferences.saveWakeAlarmTime(wH, wM)
                            settingsPreferences.saveStudyAlarmTime(sH, sM)
                            alarmScheduler.scheduleAll()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Alarm Times")
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Reminders") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Study Reminders", modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            if (it) requestNotificationPermission()
                            settingsPreferences.saveNotificationsEnabled(it)
                            alarmScheduler.scheduleAll()
                        }
                    )
                }
                Text("Periodic reminders if daily goal is not met.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            SettingsSection(title = "Groq API Setup") {
                Text(
                    text = "Enter your Groq API Key to enable online AI features.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            Button(
                onClick = {
                    settingsPreferences.saveApiKey(apiKey)
                    settingsPreferences.saveName(name)
                    settingsPreferences.saveCustomGoalsEnabled(customGoalsEnabled)
                    saveStatus = "Settings saved successfully!"
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save All Settings", fontSize = 16.sp)
            }

            if (saveStatus != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = saveStatus!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

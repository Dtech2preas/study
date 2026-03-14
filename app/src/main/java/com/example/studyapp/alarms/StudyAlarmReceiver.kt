package com.example.studyapp.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.studyapp.data.local.StudyDatabase
import com.example.studyapp.data.preferences.SettingsPreferences
import com.example.studyapp.data.repository.StudyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class StudyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler(context).scheduleAll()
            return
        }

        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Study Time!"
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, -1)

        val preferences = SettingsPreferences(context)
        val name = preferences.getName()
        val displayName = if (name.isNotEmpty()) name else "there"

        if (action == AlarmScheduler.ACTION_ALARM) {
            // High priority full screen notification for alarm
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showFullScreenAlarm(title, requestCode)

            // Reschedule for tomorrow
            AlarmScheduler(context).scheduleAll()
        } else if (action == AlarmScheduler.ACTION_REMINDER) {
            // Reminder Notification
            if (preferences.isStudying()) {
                Log.d("StudyAlarmReceiver", "User is currently studying. Skipping reminder.")
                return
            }

            val pendingResult = goAsync()

            // Check if user has reached daily goal
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val dailyGoalHours = preferences.getDailyGoal(dayOfWeek)
            val dailyGoalSeconds = dailyGoalHours * 3600L

            val database = StudyDatabase.getDatabase(context)
            val repository = StudyRepository(database.studySessionDao(), database.historyDao())

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val todayDuration = repository.getTodayStudyDurationDirectly()
                    if (todayDuration < dailyGoalSeconds) {
                        val notificationHelper = NotificationHelper(context)
                        val message = "Hello $displayName, don't forget today's study session!"
                        notificationHelper.showReminderNotification("Study Reminder", message)
                    }

                    // Reschedule for tomorrow
                    AlarmScheduler(context).scheduleAll()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

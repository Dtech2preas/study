package com.example.studyapp.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.studyapp.data.preferences.SettingsPreferences
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferences = SettingsPreferences(context)

    fun scheduleAll() {
        cancelAll()
        if (preferences.isAlarmsEnabled()) {
            scheduleWakeAlarm()
            scheduleStudyAlarm()
        }
        if (preferences.isNotificationsEnabled()) {
            scheduleReminders()
        }
    }

    fun cancelAll() {
        val intentAlarm = Intent(context, StudyAlarmReceiver::class.java).apply { action = ACTION_ALARM }
        val intentReminder = Intent(context, StudyAlarmReceiver::class.java).apply { action = ACTION_REMINDER }

        // Cancel Wake
        alarmManager.cancel(PendingIntent.getBroadcast(context, REQUEST_WAKE, intentAlarm, PendingIntent.FLAG_IMMUTABLE))
        // Cancel Study
        alarmManager.cancel(PendingIntent.getBroadcast(context, REQUEST_STUDY, intentAlarm, PendingIntent.FLAG_IMMUTABLE))
        // Cancel Reminders
        val reminderTimes = listOf(16 to 0, 18 to 30, 19 to 0, 20 to 0, 22 to 0)
        reminderTimes.forEachIndexed { index, _ ->
            alarmManager.cancel(PendingIntent.getBroadcast(context, REQUEST_REMINDER_BASE + index, intentReminder, PendingIntent.FLAG_IMMUTABLE))
        }
    }

    private fun scheduleWakeAlarm() {
        val (hour, minute) = preferences.getWakeAlarmTime()
        scheduleExactAlarm(hour, minute, REQUEST_WAKE, ACTION_ALARM, "Wake Up!")
    }

    private fun scheduleStudyAlarm() {
        val (hour, minute) = preferences.getStudyAlarmTime()
        scheduleExactAlarm(hour, minute, REQUEST_STUDY, ACTION_ALARM, "Time to Study!")
    }

    private fun scheduleReminders() {
        // 4 PM, 6:30 PM, 7:00 PM, 8:00 PM, 10:00 PM
        val reminderTimes = listOf(16 to 0, 18 to 30, 19 to 0, 20 to 0, 22 to 0)
        reminderTimes.forEachIndexed { index, (hour, minute) ->
            scheduleExactAlarm(hour, minute, REQUEST_REMINDER_BASE + index, ACTION_REMINDER, "Reminder")
        }
    }

    private fun scheduleExactAlarm(hour: Int, minute: Int, requestCode: Int, actionStr: String, extraTitle: String) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if time has passed
            }
        }

        val intent = Intent(context, StudyAlarmReceiver::class.java).apply {
            action = actionStr
            putExtra(EXTRA_TITLE, extraTitle)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Permission for exact alarms not granted
        }
    }

    companion object {
        const val ACTION_ALARM = "com.example.studyapp.ACTION_ALARM"
        const val ACTION_REMINDER = "com.example.studyapp.ACTION_REMINDER"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_REQUEST_CODE = "extra_request_code"

        const val REQUEST_WAKE = 100
        const val REQUEST_STUDY = 101
        const val REQUEST_REMINDER_BASE = 200
    }
}

package com.example.studyapp.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsPreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_GROQ_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_GROQ_API_KEY, null)
    }

    fun saveName(name: String) {
        sharedPreferences.edit().putString(KEY_NAME, name).apply()
    }

    fun getName(): String {
        return sharedPreferences.getString(KEY_NAME, "") ?: ""
    }

    fun saveCustomGoalsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_CUSTOM_GOALS_ENABLED, enabled).apply()
    }

    fun isCustomGoalsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_CUSTOM_GOALS_ENABLED, false)
    }

    fun saveDailyGoal(dayOfWeek: Int, hours: Int) {
        sharedPreferences.edit().putInt("$KEY_DAILY_GOAL_$dayOfWeek", hours).apply()
    }

    fun getDailyGoal(dayOfWeek: Int): Int {
        // default 2 hours weekdays (2-6), 4 hours weekends (1, 7)
        // Calendar.MONDAY = 2, Calendar.SUNDAY = 1
        val isWeekend = dayOfWeek == 1 || dayOfWeek == 7
        val defaultGoal = if (isWeekend) 4 else 2
        return sharedPreferences.getInt("$KEY_DAILY_GOAL_$dayOfWeek", defaultGoal)
    }

    fun saveAlarmsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ALARMS_ENABLED, enabled).apply()
    }

    fun isAlarmsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_ALARMS_ENABLED, false)
    }

    fun saveWakeAlarmTime(hour: Int, minute: Int) {
        sharedPreferences.edit().putInt(KEY_WAKE_ALARM_HOUR, hour).putInt(KEY_WAKE_ALARM_MINUTE, minute).apply()
    }

    fun getWakeAlarmTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt(KEY_WAKE_ALARM_HOUR, 5)
        val minute = sharedPreferences.getInt(KEY_WAKE_ALARM_MINUTE, 45)
        return Pair(hour, minute)
    }

    fun saveStudyAlarmTime(hour: Int, minute: Int) {
        sharedPreferences.edit().putInt(KEY_STUDY_ALARM_HOUR, hour).putInt(KEY_STUDY_ALARM_MINUTE, minute).apply()
    }

    fun getStudyAlarmTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt(KEY_STUDY_ALARM_HOUR, 18)
        val minute = sharedPreferences.getInt(KEY_STUDY_ALARM_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun saveNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun setStudying(studying: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_STUDYING, studying).apply()
    }

    fun isStudying(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_STUDYING, false)
    }

    companion object {
        private const val KEY_GROQ_API_KEY = "groq_api_key"
        private const val KEY_NAME = "user_name"
        private const val KEY_CUSTOM_GOALS_ENABLED = "custom_goals_enabled"
        private const val KEY_DAILY_GOAL_ = "daily_goal_"
        private const val KEY_ALARMS_ENABLED = "alarms_enabled"
        private const val KEY_WAKE_ALARM_HOUR = "wake_alarm_hour"
        private const val KEY_WAKE_ALARM_MINUTE = "wake_alarm_minute"
        private const val KEY_STUDY_ALARM_HOUR = "study_alarm_hour"
        private const val KEY_STUDY_ALARM_MINUTE = "study_alarm_minute"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_IS_STUDYING = "is_studying"
    }
}

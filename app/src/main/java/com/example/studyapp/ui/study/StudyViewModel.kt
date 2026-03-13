package com.example.studyapp.ui.study

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.local.StudyDatabase
import com.example.studyapp.data.local.StudySession
import com.example.studyapp.data.repository.StudyRepository
import com.example.studyapp.data.local.DailyStudyTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyRepository

    val allSessions = MutableStateFlow<List<StudySession>>(emptyList())

    private val _isStudying = MutableStateFlow(false)
    val isStudying: StateFlow<Boolean> = _isStudying.asStateFlow()

    private val _elapsedTimeSeconds = MutableStateFlow(0L)
    val elapsedTimeSeconds: StateFlow<Long> = _elapsedTimeSeconds.asStateFlow()

    private var startTime: Long = 0L

    val documentHistory = MutableStateFlow<List<com.example.studyapp.data.local.DocumentHistory>>(emptyList())

    init {
        val db = StudyDatabase.getDatabase(application)
        repository = StudyRepository(db.studySessionDao(), db.historyDao())
        viewModelScope.launch {
            repository.allSessions.collect { sessions ->
                allSessions.value = sessions
            }
        }
        viewModelScope.launch {
            repository.getAllDocumentHistory().collect { history ->
                documentHistory.value = history
            }
        }
        startTimer()
    }

    // History Operations exposed to UI
    private var currentDocumentId: Int? = null
    private var currentOriginalText: String? = null

    suspend fun saveDocumentWithSummary(title: String, originalText: String, summaryText: String) {
        val docId = getOrCreateDocumentId(title, originalText)
        repository.insertSummaryHistory(
            com.example.studyapp.data.local.SummaryHistory(documentId = docId, summaryText = summaryText, timestamp = System.currentTimeMillis())
        )
    }

    suspend fun saveDocumentWithQuiz(title: String, originalText: String, quizJson: String) {
        val docId = getOrCreateDocumentId(title, originalText)
        repository.insertQuizHistory(
            com.example.studyapp.data.local.QuizHistory(documentId = docId, quizJson = quizJson, timestamp = System.currentTimeMillis())
        )
    }

    private suspend fun getOrCreateDocumentId(title: String, originalText: String): Int {
        if (currentDocumentId != null && currentOriginalText == originalText) {
            return currentDocumentId!!
        }
        val documentId = repository.insertDocumentHistory(
            com.example.studyapp.data.local.DocumentHistory(title = title, originalText = originalText, timestamp = System.currentTimeMillis())
        ).toInt()
        currentDocumentId = documentId
        currentOriginalText = originalText
        return documentId
    }

    suspend fun getDocumentHistoryById(id: Int) = repository.getDocumentHistoryById(id)

    suspend fun deleteDocumentHistory(id: Int) = repository.deleteDocumentHistory(id)

    fun getSummariesForDocument(documentId: Int) = repository.getSummariesForDocument(documentId)

    fun getQuizzesForDocument(documentId: Int) = repository.getQuizzesForDocument(documentId)

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                if (_isStudying.value) {
                    _elapsedTimeSeconds.value = (System.currentTimeMillis() - startTime) / 1000
                }
                delay(1000)
            }
        }
    }

    fun toggleStudying() {
        if (_isStudying.value) {
            // Stop studying
            val endTime = System.currentTimeMillis()
            val duration = (endTime - startTime) / 1000
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startTime
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val session = StudySession(
                startTime = startTime,
                endTime = endTime,
                durationInSeconds = duration,
                date = calendar.timeInMillis
            )
            viewModelScope.launch {
                repository.insertSession(session)
            }
            _isStudying.value = false
            _elapsedTimeSeconds.value = 0L
        } else {
            // Start studying
            startTime = System.currentTimeMillis()
            _isStudying.value = true
        }
    }

    fun getTodayDuration() = repository.getTotalDurationInRange(getStartOfDay(), getEndOfDay())

    fun getWeekDuration() = repository.getTotalDurationInRange(getStartOfWeek(), getEndOfDay())

    fun getMonthDuration() = repository.getTotalDurationInRange(getStartOfMonth(), getEndOfDay())

    fun getYearDuration() = repository.getTotalDurationInRange(getStartOfYear(), getEndOfDay())

    fun getAllTimeDuration() = repository.getTotalDurationInRange(0, Long.MAX_VALUE)

    fun getLongestSession() = repository.getLongestSessionDuration()

    fun getBestStudyDay() = repository.getBestStudyDay()

    fun getAverageDailyStudyTime() = repository.getAverageDailyStudyTime()

    fun getLast7DaysStudyTime(): StateFlow<List<DailyStudyTotal>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
        val todayStart = calendar.timeInMillis
        val endOfDay = todayStart + 24 * 60 * 60 * 1000 - 1

        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startOf7Days = calendar.timeInMillis

        // Pre-fill the last 7 days with 0 duration so the chart doesn't skip days
        val last7DaysTemplate = mutableListOf<DailyStudyTotal>()
        val tempCal = Calendar.getInstance().apply { timeInMillis = startOf7Days }
        for (i in 0 until 7) {
            last7DaysTemplate.add(DailyStudyTotal(date = tempCal.timeInMillis, totalDuration = 0L))
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return repository.getDailyTotalsInRange(startOf7Days, endOfDay).map { dbTotals ->
            val mapped = last7DaysTemplate.map { templateDay ->
                val matchingDbDay = dbTotals.find { it.date == templateDay.date }
                matchingDbDay ?: templateDay
            }
            mapped
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = last7DaysTemplate
        )
    }

    fun getCurrentStreak(): StateFlow<Int> {
        return repository.getAllStudyDatesDesc().map { dates ->
            if (dates.isEmpty()) return@map 0

            var streak = 0
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.clear(Calendar.MINUTE)
            calendar.clear(Calendar.SECOND)
            calendar.clear(Calendar.MILLISECOND)

            val today = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.timeInMillis

            var currentDateToCheck = if (dates.contains(today)) today else {
                if (dates.contains(yesterday)) yesterday else null
            }

            if (currentDateToCheck == null) return@map 0

            for (date in dates) {
                if (date == currentDateToCheck) {
                    streak++
                    val cal = Calendar.getInstance().apply { timeInMillis = currentDateToCheck!! }
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    currentDateToCheck = cal.timeInMillis
                } else if (currentDateToCheck != null && date < currentDateToCheck) {
                    break
                }
            }
            streak
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        return cal.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        return cal.timeInMillis
    }

    private fun getStartOfYear(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        return cal.timeInMillis
    }
}

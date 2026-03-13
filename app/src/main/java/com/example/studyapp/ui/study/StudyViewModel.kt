package com.example.studyapp.ui.study

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.local.StudyDatabase
import com.example.studyapp.data.local.StudySession
import com.example.studyapp.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val latestSummary = repository.getLatestSummaryForDocument(docId)
        if (latestSummary != null) {
            repository.insertSummaryHistory(
                latestSummary.copy(summaryText = summaryText, timestamp = System.currentTimeMillis())
            )
        } else {
            repository.insertSummaryHistory(
                com.example.studyapp.data.local.SummaryHistory(documentId = docId, summaryText = summaryText, timestamp = System.currentTimeMillis())
            )
        }
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

    fun getAllTimeDuration() = repository.getTotalDurationInRange(0, Long.MAX_VALUE)

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
}

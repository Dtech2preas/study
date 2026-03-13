package com.example.studyapp.data.repository

import com.example.studyapp.data.local.DocumentHistory
import com.example.studyapp.data.local.HistoryDao
import com.example.studyapp.data.local.QuizHistory
import com.example.studyapp.data.local.StudySession
import com.example.studyapp.data.local.StudySessionDao
import com.example.studyapp.data.local.SummaryHistory
import com.example.studyapp.data.local.DailyStudyTotal
import kotlinx.coroutines.flow.Flow

class StudyRepository(
    private val studySessionDao: StudySessionDao,
    private val historyDao: HistoryDao
) {

    val allSessions: Flow<List<StudySession>> = studySessionDao.getAllSessions()

    suspend fun insertSession(session: StudySession) {
        studySessionDao.insertSession(session)
    }

    fun getTotalDurationInRange(startDate: Long, endDate: Long): Flow<Long?> {
        return studySessionDao.getTotalDurationInRange(startDate, endDate)
    }

    fun getLongestSessionDuration(): Flow<Long?> = studySessionDao.getLongestSessionDuration()

    fun getBestStudyDay(): Flow<DailyStudyTotal?> = studySessionDao.getBestStudyDay()

    fun getAverageDailyStudyTime(): Flow<Long?> = studySessionDao.getAverageDailyStudyTime()

    fun getDailyTotalsInRange(startDate: Long, endDate: Long): Flow<List<DailyStudyTotal>> =
        studySessionDao.getDailyTotalsInRange(startDate, endDate)

    fun getAllStudyDatesDesc(): Flow<List<Long>> = studySessionDao.getAllStudyDatesDesc()

    // History Operations
    suspend fun insertDocumentHistory(document: DocumentHistory): Long = historyDao.insertDocumentHistory(document)
    fun getAllDocumentHistory(): Flow<List<DocumentHistory>> = historyDao.getAllDocumentHistory()
    suspend fun getDocumentHistoryById(id: Int): DocumentHistory? = historyDao.getDocumentHistoryById(id)
    suspend fun deleteDocumentHistory(id: Int) = historyDao.deleteDocumentHistory(id)

    suspend fun insertSummaryHistory(summary: SummaryHistory): Long = historyDao.insertSummaryHistory(summary)
    fun getSummariesForDocument(documentId: Int): Flow<List<SummaryHistory>> = historyDao.getSummariesForDocument(documentId)
    suspend fun deleteSummaryHistory(id: Int) = historyDao.deleteSummaryHistory(id)

    suspend fun insertQuizHistory(quiz: QuizHistory): Long = historyDao.insertQuizHistory(quiz)
    fun getQuizzesForDocument(documentId: Int): Flow<List<QuizHistory>> = historyDao.getQuizzesForDocument(documentId)
    suspend fun deleteQuizHistory(id: Int) = historyDao.deleteQuizHistory(id)
}

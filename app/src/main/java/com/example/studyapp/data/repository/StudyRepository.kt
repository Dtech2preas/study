package com.example.studyapp.data.repository

import com.example.studyapp.data.local.StudySession
import com.example.studyapp.data.local.StudySessionDao
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studySessionDao: StudySessionDao) {

    val allSessions: Flow<List<StudySession>> = studySessionDao.getAllSessions()

    suspend fun insertSession(session: StudySession) {
        studySessionDao.insertSession(session)
    }

    fun getTotalDurationInRange(startDate: Long, endDate: Long): Flow<Long?> {
        return studySessionDao.getTotalDurationInRange(startDate, endDate)
    }
}

package com.studyapp.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {

    val allSessions: Flow<List<StudySession>> = studyDao.getAllSessions()
    val totalStudyTime: Flow<Long?> = studyDao.getTotalStudyTime()

    fun getStudyTimeSince(startTime: Long): Flow<Long?> {
        return studyDao.getStudyTimeSince(startTime)
    }

    suspend fun insert(session: StudySession) {
        studyDao.insertSession(session)
    }
}

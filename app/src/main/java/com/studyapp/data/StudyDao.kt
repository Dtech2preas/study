package com.studyapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Insert
    suspend fun insertSession(session: StudySession)

    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT SUM(duration) FROM study_sessions WHERE startTime >= :startOfDay")
    fun getStudyTimeSince(startOfDay: Long): Flow<Long?>

    @Query("SELECT SUM(duration) FROM study_sessions")
    fun getTotalStudyTime(): Flow<Long?>
}

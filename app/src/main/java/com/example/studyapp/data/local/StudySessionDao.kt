package com.example.studyapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Insert
    suspend fun insertSession(session: StudySession)

    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT SUM(durationInSeconds) FROM study_sessions WHERE date >= :startDate AND date <= :endDate")
    fun getTotalDurationInRange(startDate: Long, endDate: Long): Flow<Long?>
}

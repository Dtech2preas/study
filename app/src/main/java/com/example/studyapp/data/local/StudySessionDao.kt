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

    @Query("SELECT MAX(durationInSeconds) FROM study_sessions")
    fun getLongestSessionDuration(): Flow<Long?>

    @Query("SELECT date, SUM(durationInSeconds) as totalDuration FROM study_sessions GROUP BY date ORDER BY totalDuration DESC LIMIT 1")
    fun getBestStudyDay(): Flow<DailyStudyTotal?>

    @Query("SELECT AVG(dailyTotal) FROM (SELECT SUM(durationInSeconds) as dailyTotal FROM study_sessions GROUP BY date)")
    fun getAverageDailyStudyTime(): Flow<Long?>

    @Query("SELECT date, SUM(durationInSeconds) as totalDuration FROM study_sessions WHERE date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date ASC")
    fun getDailyTotalsInRange(startDate: Long, endDate: Long): Flow<List<DailyStudyTotal>>

    @Query("SELECT DISTINCT date FROM study_sessions ORDER BY date DESC")
    fun getAllStudyDatesDesc(): Flow<List<Long>>
}

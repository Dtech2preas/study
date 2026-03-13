package com.example.studyapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val durationInSeconds: Long,
    val date: Long // Timestamp representing the start of the day for easy grouping
)

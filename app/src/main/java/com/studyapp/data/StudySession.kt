package com.studyapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long, // timestamp in ms
    val endTime: Long,   // timestamp in ms
    val duration: Long   // duration in ms
)

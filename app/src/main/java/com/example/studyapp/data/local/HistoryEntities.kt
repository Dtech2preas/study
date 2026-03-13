package com.example.studyapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "document_history")
data class DocumentHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val originalText: String,
    val timestamp: Long
)

@Entity(
    tableName = "summary_history",
    foreignKeys = [
        ForeignKey(
            entity = DocumentHistory::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class SummaryHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val documentId: Int,
    val summaryText: String,
    val timestamp: Long
)

@Entity(
    tableName = "quiz_history",
    foreignKeys = [
        ForeignKey(
            entity = DocumentHistory::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class QuizHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val documentId: Int,
    val quizJson: String,
    val timestamp: Long
)

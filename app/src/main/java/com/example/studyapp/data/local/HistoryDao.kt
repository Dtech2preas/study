package com.example.studyapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentHistory(document: DocumentHistory): Long

    @Query("SELECT * FROM document_history ORDER BY timestamp DESC")
    fun getAllDocumentHistory(): Flow<List<DocumentHistory>>

    @Query("SELECT * FROM document_history WHERE id = :id")
    suspend fun getDocumentHistoryById(id: Int): DocumentHistory?

    @Query("DELETE FROM document_history WHERE id = :id")
    suspend fun deleteDocumentHistory(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummaryHistory(summary: SummaryHistory): Long

    @Query("SELECT * FROM summary_history WHERE documentId = :documentId ORDER BY timestamp DESC")
    fun getSummariesForDocument(documentId: Int): Flow<List<SummaryHistory>>

    @Query("DELETE FROM summary_history WHERE id = :id")
    suspend fun deleteSummaryHistory(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizHistory(quiz: QuizHistory): Long

    @Query("SELECT * FROM quiz_history WHERE documentId = :documentId ORDER BY timestamp DESC")
    fun getQuizzesForDocument(documentId: Int): Flow<List<QuizHistory>>

    @Query("DELETE FROM quiz_history WHERE id = :id")
    suspend fun deleteQuizHistory(id: Int)
}

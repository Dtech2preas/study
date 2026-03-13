package com.example.studyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudySession::class,
        DocumentHistory::class,
        SummaryHistory::class,
        QuizHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_database"
                )
                .fallbackToDestructiveMigration() // Simple migration strategy for this step
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

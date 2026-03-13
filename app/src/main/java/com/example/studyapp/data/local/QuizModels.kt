package com.example.studyapp.data.local

data class Quiz(
    val title: String,
    val questions: List<Question>
)

data class Question(
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

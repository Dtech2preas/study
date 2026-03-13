package com.studyapp.ai

class QuizGenerator(private val aiManager: AiManager) {
    fun generateQuiz(text: String): String {
        if (text.isBlank()) return "No text to generate quiz from."

        val result = aiManager.runInference("generate_quiz: $text")

        if (result == "Model not loaded") {
             // Mock quiz generator
             return """
                 Quiz based on text:
                 1. What is the main topic?
                 2. Explain the key concept mentioned.
             """.trimIndent()
        }
        return result
    }
}

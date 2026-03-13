package com.studyapp.ai

class Summarizer(private val aiManager: AiManager) {
    fun summarize(text: String): String {
        // In a real TFLite sequence-to-sequence implementation,
        // we would chunk the text, run inference on each, and combine.
        if (text.isBlank()) return "No text to summarize."

        // Use AiManager for real model, but fallback to dummy logic if model fails
        val result = aiManager.runInference("summarize: $text")

        if (result == "Model not loaded") {
            // Very naive extractive fallback
            val sentences = text.split(Regex("(?<=[.!?])\\s+"))
            if (sentences.size <= 3) return text
            return sentences.take(3).joinToString(" ") + "..."
        }
        return result
    }
}

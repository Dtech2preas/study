package com.example.studyapp.ai

import android.content.Context
import com.example.studyapp.data.preferences.SettingsPreferences
import com.example.studyapp.network.GroqApiService
import com.example.studyapp.network.GroqMessage
import com.example.studyapp.network.GroqRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class OnlineAIManager(context: Context) {
    private val settingsPreferences = SettingsPreferences(context)

    // We target roughly ~20,000 characters per chunk, estimating 1 token ~ 4 chars
    // to keep it under 5800 tokens. (20,000 / 4 = 5,000 tokens)
    private val MAX_CHARS_PER_CHUNK = 20000

    private val apiService: GroqApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(GroqApiService::class.java)
    }

    private fun chunkText(text: String): List<String> {
        val chunks = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            var end = (i + MAX_CHARS_PER_CHUNK).coerceAtMost(text.length)

            // Try to find a logical break (space or newline) to avoid cutting words in half
            if (end < text.length) {
                var lastSpace = text.lastIndexOf(' ', end)
                val lastNewline = text.lastIndexOf('\n', end)

                // Prioritize newline over space if it's reasonably close (within 1000 chars)
                if (lastNewline > i && (end - lastNewline) < 1000) {
                    end = lastNewline
                } else if (lastSpace > i) {
                    end = lastSpace
                }
            }

            chunks.add(text.substring(i, end))
            i = end

            // Skip leading whitespace for the next chunk
            while (i < text.length && text[i].isWhitespace()) {
                i++
            }
        }
        return chunks
    }

    fun generateSummaryStream(text: String): Flow<AiChunkResult> = flow {
        val apiKey = settingsPreferences.getApiKey()
        if (apiKey.isNullOrBlank()) {
            emit(AiChunkResult.Error("API Key is missing. Please set it in Settings."))
            return@flow
        }

        val chunks = chunkText(text)

        for ((index, chunk) in chunks.withIndex()) {
            val prompt = "Summarize the following text (Part ${index + 1} of ${chunks.size}):\n\n$chunk"

            try {
                val response = apiService.createChatCompletion(
                    authHeader = "Bearer $apiKey",
                    request = GroqRequest(
                        messages = listOf(
                            GroqMessage(role = "system", content = "You are a helpful study assistant."),
                            GroqMessage(role = "user", content = prompt)
                        )
                    )
                )

                val content = response.choices.firstOrNull()?.message?.content
                    ?: "No content generated."

                emit(AiChunkResult.Success(partNumber = index + 1, totalParts = chunks.size, text = content))

                // If this is not the last chunk, wait 1 minute to respect rate limits
                if (index < chunks.size - 1) {
                    emit(AiChunkResult.Waiting("Waiting 1 minute for rate limit (Part ${index + 2})..."))
                    delay(60_000L) // 60 seconds
                }
            } catch (e: Exception) {
                emit(AiChunkResult.Error("Error processing part ${index + 1}: ${e.localizedMessage}"))
                return@flow // Stop on error
            }
        }
    }

    fun generateQuizStream(text: String): Flow<AiChunkResult> = flow {
        val apiKey = settingsPreferences.getApiKey()
        if (apiKey.isNullOrBlank()) {
            emit(AiChunkResult.Error("API Key is missing. Please set it in Settings."))
            return@flow
        }

        val chunks = chunkText(text)

        for ((index, chunk) in chunks.withIndex()) {
            val prompt = "Generate a multiple choice quiz based on the following text (Part ${index + 1} of ${chunks.size}):\n\n$chunk"

            try {
                val response = apiService.createChatCompletion(
                    authHeader = "Bearer $apiKey",
                    request = GroqRequest(
                        messages = listOf(
                            GroqMessage(role = "system", content = "You are a helpful study assistant that creates engaging quizzes."),
                            GroqMessage(role = "user", content = prompt)
                        )
                    )
                )

                val content = response.choices.firstOrNull()?.message?.content
                    ?: "No content generated."

                emit(AiChunkResult.Success(partNumber = index + 1, totalParts = chunks.size, text = content))

                // If this is not the last chunk, wait 1 minute to respect rate limits
                if (index < chunks.size - 1) {
                    emit(AiChunkResult.Waiting("Waiting 1 minute for rate limit (Part ${index + 2})..."))
                    delay(60_000L) // 60 seconds
                }
            } catch (e: Exception) {
                emit(AiChunkResult.Error("Error processing part ${index + 1}: ${e.localizedMessage}"))
                return@flow // Stop on error
            }
        }
    }
}

sealed class AiChunkResult {
    data class Success(val partNumber: Int, val totalParts: Int, val text: String) : AiChunkResult()
    data class Waiting(val message: String) : AiChunkResult()
    data class Error(val message: String) : AiChunkResult()
}

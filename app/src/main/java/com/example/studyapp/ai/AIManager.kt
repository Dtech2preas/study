package com.example.studyapp.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object AIManager {
    private var interpreter: Interpreter? = null

    // This method simulates initializing the AI model from the assets folder.
    fun initModel(context: Context, modelName: String = "model.tflite") {
        try {
            val modelBuffer = loadModelFile(context, modelName)
            if (modelBuffer != null) {
                val options = Interpreter.Options()
                interpreter = Interpreter(modelBuffer, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Model might not exist yet if downloaded via CI
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer? {
        return try {
            val assetFileDescriptor = context.assets.openFd(modelName)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            null
        }
    }

    // Dummy method for generation using TFLite.
    // In a real SLM integration, this would use a Tokenizer to convert text to input tensors
    // and process output tensors back to text.
    fun generateSummary(text: String): String {
        if (text.isBlank()) return "Please provide valid text."

        // Simulating the TFLite execution.
        // val inputTensor = ...
        // val outputTensor = ...
        // interpreter?.run(inputTensor, outputTensor)

        // For the sake of this prototype, we'll return a pseudo-summary.
        val wordCount = text.split("\\s+".toRegex()).size
        val firstSentence = text.split(".").firstOrNull() ?: text.take(50)

        return "AI Summary:\nThis document is approximately $wordCount words long. It begins by discussing: '$firstSentence'.\n(Note: A real TFLite model inference will happen here once the 150MB model is added to assets.)"
    }

    fun generateQuiz(text: String): String {
        if (text.isBlank()) return "Please provide valid text."

        // Simulating quiz generation.
        val firstSentence = text.split(".").firstOrNull() ?: text.take(50)

        return """
            AI Quiz:
            1. What is the main topic of the document that begins with "$firstSentence"?
               A) Topic 1
               B) Topic 2
               C) Topic 3

            2. How many words are in the provided text?
               A) Under 100
               B) Over 100
        """.trimIndent()
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

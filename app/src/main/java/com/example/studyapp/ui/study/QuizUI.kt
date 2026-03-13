package com.example.studyapp.ui.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.studyapp.data.local.Quiz
import com.google.gson.Gson

@Composable
fun InteractiveQuizView(quizJson: String) {
    var quiz by remember { mutableStateOf<Quiz?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(quizJson) {
        try {
            var cleanJson = quizJson.replace("```json", "").replace("```", "").trim()

            // Fallback JSON extraction logic
            val startIndex = cleanJson.indexOf("{")
            val endIndex = cleanJson.lastIndexOf("}")
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                cleanJson = cleanJson.substring(startIndex, endIndex + 1)
            }

            quiz = Gson().fromJson(cleanJson, Quiz::class.java)
        } catch (e: Exception) {
            parseError = "Failed to parse quiz: ${e.message}"
        }
    }

    if (parseError != null) {
        Text("Error loading quiz: $parseError", color = MaterialTheme.colorScheme.error)
        return
    }

    if (quiz == null) {
        CircularProgressIndicator()
        return
    }

    var showResults by remember { mutableStateOf(false) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Text(
            text = "🧠 ${quiz!!.title}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!showResults) {
            val progress = selectedAnswers.size.toFloat() / quiz!!.questions.size.toFloat()
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(12.dp).padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Text(
                text = "${selectedAnswers.size} of ${quiz!!.questions.size} answered",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.End)
            )

            quiz!!.questions.forEachIndexed { index, question ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Q${index + 1}: ${question.questionText}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        question.options.forEachIndexed { optionIndex, optionText ->
                            val isSelected = selectedAnswers[index] == optionIndex
                            val optionBgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            val optionTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { selectedAnswers[index] = optionIndex },
                                        role = Role.RadioButton
                                    ),
                                shape = MaterialTheme.shapes.medium,
                                color = optionBgColor,
                                tonalElevation = if (isSelected) 4.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = optionText, style = MaterialTheme.typography.bodyLarge, color = optionTextColor)
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showResults = true },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp),
                enabled = selectedAnswers.size == quiz!!.questions.size,
                shape = MaterialTheme.shapes.large
            ) {
                Text("Submit Quiz & See Results", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            // Results View
            var correctCount = 0
            quiz!!.questions.forEachIndexed { index, question ->
                if (selectedAnswers[index] == question.correctOptionIndex) {
                    correctCount++
                }
            }
            val total = quiz!!.questions.size
            val percentage = (correctCount.toFloat() / total * 100).toInt()

            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quiz Results", style = MaterialTheme.typography.headlineSmall)
                    Text("Score: $correctCount / $total", style = MaterialTheme.typography.titleLarge)
                    Text("Percentage: $percentage%", style = MaterialTheme.typography.titleMedium)
                    if (percentage >= 70) {
                        Text("Status: Passed!", color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("Status: Failed. Keep studying!", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            quiz!!.questions.forEachIndexed { index, question ->
                val userAnswer = selectedAnswers[index]
                val isCorrect = userAnswer == question.correctOptionIndex
                val cardColor = if (isCorrect) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer

                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "${index + 1}. ${question.questionText}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Your Answer: ${userAnswer?.let { question.options[it] } ?: "None"}")
                        if (!isCorrect) {
                            Text(text = "Correct Answer: ${question.options[question.correctOptionIndex]}", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Explanation: ${question.explanation}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = {
                    showResults = false
                    selectedAnswers.clear()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Retake Quiz")
            }
        }
    }
}

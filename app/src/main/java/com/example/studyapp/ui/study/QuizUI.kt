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
            val cleanJson = quizJson.replace("```json", "").replace("```", "").trim()
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

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = quiz!!.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        if (!showResults) {
            quiz!!.questions.forEachIndexed { index, question ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "${index + 1}. ${question.questionText}", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        question.options.forEachIndexed { optionIndex, optionText ->
                            val isSelected = selectedAnswers[index] == optionIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { selectedAnswers[index] = optionIndex },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null // handled by row
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = optionText, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showResults = true },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = selectedAnswers.size == quiz!!.questions.size
            ) {
                Text("Submit Quiz")
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

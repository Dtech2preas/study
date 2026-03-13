package com.example.studyapp.ui.study

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyapp.ai.AiChunkResult
import com.example.studyapp.ai.OnlineAIManager
import com.example.studyapp.utils.DocumentParser
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudyScreen(viewModel: StudyViewModel) {
    val isStudying by viewModel.isStudying.collectAsState()
    val elapsedTime by viewModel.elapsedTimeSeconds.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var parsedText by remember { mutableStateOf<String?>(null) }
    var documentTitle by remember { mutableStateOf("Untitled Document") }
    var isLoading by remember { mutableStateOf(false) }
    var aiOutput by remember { mutableStateOf<String?>(null) }
    var isAiProcessing by remember { mutableStateOf(false) }
    var showQuiz by remember { mutableStateOf(false) }
    var isFullScreenSummary by remember { mutableStateOf(false) }

    val onlineAIManager = remember { OnlineAIManager(context) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                isLoading = true
                aiOutput = null
                coroutineScope.launch {
                    val text = withContext(Dispatchers.IO) {
                        DocumentParser.extractTextFromUri(context, it)
                    }
                    parsedText = text
                    if (text != null && text.isNotBlank()) {
                        documentTitle = onlineAIManager.generateTitle(text)
                    } else {
                        Toast.makeText(context, "Failed to extract text or file is empty", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }
            }
        }
    )

    val hours = elapsedTime / 3600
    val minutes = (elapsedTime % 3600) / 60
    val seconds = elapsedTime % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Study Timer Section
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            fontSize = 48.sp,
            modifier = Modifier.padding(16.dp)
        )

        Button(onClick = { viewModel.toggleStudying() }) {
            Text(if (isStudying) "Stop Studying" else "Start Studying")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Document Section
        Text("AI Assistant", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
        Text("Upload a .txt or .pdf file to summarize or generate quizzes.")

        Button(onClick = { filePickerLauncher.launch(arrayOf("application/pdf", "text/plain")) }) {
            Text("Upload Document")
        }

        if (isLoading) {
            CircularProgressIndicator()
            Text("Processing document...")
        } else if (parsedText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Document: $documentTitle", style = MaterialTheme.typography.titleMedium)
                    Text("${parsedText!!.length} characters", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                aiOutput = ""
                                showQuiz = false
                                isAiProcessing = true
                                coroutineScope.launch {
                                    onlineAIManager.generateSummaryStream(parsedText!!).collect { result ->
                                        handleAiResult(result) { newText ->
                                            aiOutput = (aiOutput ?: "") + newText + "\n\n"
                                        }
                                    }
                                    isAiProcessing = false
                                    // Save history
                                    aiOutput?.let { summary ->
                                        viewModel.saveDocumentWithSummary(documentTitle, parsedText!!, summary)
                                        Toast.makeText(context, "Summary saved to History", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isAiProcessing
                        ) {
                            Text("Summarize")
                        }
                        Button(
                            onClick = {
                                aiOutput = ""
                                showQuiz = false
                                isAiProcessing = true
                                coroutineScope.launch {
                                    onlineAIManager.generateQuizStream(parsedText!!).collect { result ->
                                        handleAiResult(result) { newText ->
                                            aiOutput = (aiOutput ?: "") + newText
                                        }
                                    }
                                    isAiProcessing = false
                                    showQuiz = true
                                    // Save history
                                    aiOutput?.let { quizJson ->
                                        viewModel.saveDocumentWithQuiz(documentTitle, parsedText!!, quizJson)
                                        Toast.makeText(context, "Quiz saved to History", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isAiProcessing
                        ) {
                            Text("Generate Quiz")
                        }
                    }
                }
            }
        }

        if (isAiProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            Text("AI is thinking...")
        }

        if (!aiOutput.isNullOrEmpty()) {
            if (showQuiz) {
                InteractiveQuizView(quizJson = aiOutput!!)
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isFullScreenSummary = true }) {
                                Text("Full Screen")
                            }
                        }
                        MarkdownText(markdown = aiOutput!!)
                    }
                }
            }
        }
    }

    if (isFullScreenSummary && !aiOutput.isNullOrEmpty() && !showQuiz) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { isFullScreenSummary = false }) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { isFullScreenSummary = false }) {
                            Text("X", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        MarkdownText(markdown = aiOutput!!)
                    }
                }
            }
        }
    }
}

private fun handleAiResult(result: AiChunkResult, appendText: (String) -> Unit) {
    when (result) {
        is AiChunkResult.Success -> {
            appendText("--- Part ${result.partNumber} of ${result.totalParts} ---\n${result.text}")
        }
        is AiChunkResult.Waiting -> {
            appendText("⏳ ${result.message}")
        }
        is AiChunkResult.Error -> {
            appendText("❌ Error: ${result.message}")
        }
    }
}

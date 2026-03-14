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
    var aiWaitMessage by remember { mutableStateOf<String?>(null) }
    var showQuiz by remember { mutableStateOf(false) }
    var isFullScreenSummary by remember { mutableStateOf(false) }
    var showQuizOptionsDialog by remember { mutableStateOf(false) }

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
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Document Section
        Text("AI Assistant", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
        Text("Upload a document (.txt, .pdf, .docx, .pptx, etc) to explain or generate quizzes.")

        Button(onClick = { filePickerLauncher.launch(arrayOf(
            "application/pdf",
            "text/plain",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-powerpoint", // .ppt
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
        )) }) {
            Text("Upload Document")
        }

        if (isLoading) {
            DynamicWaveLoader(text = "Processing document...")
        } else if (parsedText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📄 Document Loaded",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        documentTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text("${parsedText!!.length} characters extracted", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                aiOutput = ""
                                showQuiz = false
                                isAiProcessing = true
                                aiWaitMessage = null
                                coroutineScope.launch {
                                    onlineAIManager.generateSummaryStream(parsedText!!).collect { result ->
                                        if (result is AiChunkResult.Waiting) {
                                            aiWaitMessage = result.message
                                        } else {
                                            aiWaitMessage = null
                                            handleAiResult(result) { newText ->
                                                aiOutput = (aiOutput ?: "") + newText + "\n\n"
                                            }
                                        }
                                    }
                                    isAiProcessing = false
                                    aiWaitMessage = null
                                    // Save history
                                    aiOutput?.let { summary ->
                                        viewModel.saveDocumentWithSummary(documentTitle, parsedText!!, summary)
                                        Toast.makeText(context, "Explanation saved to History", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isAiProcessing
                        ) {
                            Text("📝 Explain")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                showQuizOptionsDialog = true
                            },
                            enabled = !isAiProcessing
                        ) {
                            Text("🧠 Quiz")
                        }
                    }
                }
            }
        }

        if (isAiProcessing) {
            DynamicWaveLoader(text = "AI is thinking...")
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
                        RichTextView(markdown = aiOutput!!)
                    }
                }
            }
        }
    }

    if (showQuizOptionsDialog && parsedText != null) {
        val maxQuestions = when {
            parsedText!!.length > 30000 -> 100
            parsedText!!.length > 15000 -> 50
            parsedText!!.length > 8000 -> 30
            else -> 15
        }
        val options = listOf(15, 30, 50, 100).filter { it <= maxQuestions || it == 15 } // Ensure at least 15 is always available

        AlertDialog(
            onDismissRequest = { showQuizOptionsDialog = false },
            title = { Text("Generate Quiz") },
            text = {
                Column {
                    Text("Select the number of questions to generate. (Max based on document length: $maxQuestions)")
                    Spacer(modifier = Modifier.height(16.dp))
                    options.forEach { numQuestions ->
                        Button(
                            onClick = {
                                showQuizOptionsDialog = false
                                aiOutput = ""
                                showQuiz = false
                                isAiProcessing = true
                                aiWaitMessage = null
                                coroutineScope.launch {
                                    onlineAIManager.generateQuizStream(parsedText!!, numQuestions).collect { result ->
                                        if (result is AiChunkResult.Waiting) {
                                            aiWaitMessage = result.message
                                        } else {
                                            aiWaitMessage = null
                                            handleAiResult(result) { newText ->
                                                aiOutput = (aiOutput ?: "") + newText
                                            }
                                        }
                                    }
                                    isAiProcessing = false
                                    aiWaitMessage = null
                                    showQuiz = true
                                    // Save history
                                    aiOutput?.let { quizJson ->
                                        viewModel.saveDocumentWithQuiz(documentTitle, parsedText!!, quizJson)
                                        Toast.makeText(context, "Quiz saved to History", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("$numQuestions Questions")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showQuizOptionsDialog = false }) {
                    Text("Cancel")
                }
            },
            icon = { Text("🧠") },
            properties = androidx.compose.ui.window.DialogProperties(),
        )
    }

    if (isFullScreenSummary && !aiOutput.isNullOrEmpty() && !showQuiz) {
        // Overlay a full-screen surface that ignores standard dialog margins
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { isFullScreenSummary = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Explanation Document", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { isFullScreenSummary = false }) {
                            Text("❌", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxSize()) {
                        RichTextView(markdown = aiOutput!!)
                    }
                }
            }
            if (aiWaitMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        text = "⏳ " + aiWaitMessage!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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
            // Handled in UI
        }
        is AiChunkResult.Error -> {
            appendText("❌ Error: ${result.message}")
        }
    }
}

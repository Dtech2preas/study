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
    var aiSummarySections by remember { mutableStateOf<List<com.example.studyapp.data.local.SummarySection>>(emptyList()) }
    var waitingMessage by remember { mutableStateOf<String?>(null) }
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
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Document Section
        Text("AI Assistant", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
        Text("Upload a document (.txt, .pdf, .docx, .pptx, etc) to summarize or generate quizzes.")

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
            CircularProgressIndicator()
            Text("Processing document...")
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
                                aiSummarySections = emptyList()
                                waitingMessage = null
                                showQuiz = false
                                isAiProcessing = true
                                coroutineScope.launch {
                                    onlineAIManager.generateSummaryStream(parsedText!!).collect { result ->
                                        when (result) {
                                            is AiChunkResult.Success -> {
                                                waitingMessage = null
                                                try {
                                                    val partialSummary = com.google.gson.Gson().fromJson(result.text, com.example.studyapp.data.local.SummaryResponse::class.java)
                                                    aiSummarySections = partialSummary.sections
                                                    aiOutput = result.text
                                                } catch (e: Exception) {
                                                    // Ignore parsing error for intermediate states
                                                }
                                            }
                                            is AiChunkResult.Waiting -> {
                                                waitingMessage = result.message
                                            }
                                            is AiChunkResult.Error -> {
                                                waitingMessage = "❌ Error: ${result.message}"
                                            }
                                        }
                                    }
                                    isAiProcessing = false
                                    waitingMessage = null
                                    // Save history
                                    aiOutput?.let { summary ->
                                        viewModel.saveDocumentWithSummary(documentTitle, parsedText!!, summary)
                                        Toast.makeText(context, "Summary saved to History", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isAiProcessing
                        ) {
                            Text("📝 Summarize")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                aiOutput = ""
                                aiSummarySections = emptyList()
                                waitingMessage = null
                                showQuiz = false
                                isAiProcessing = true
                                coroutineScope.launch {
                                    onlineAIManager.generateQuizStream(parsedText!!).collect { result ->
                                        when (result) {
                                            is AiChunkResult.Success -> {
                                                aiOutput = result.text
                                                waitingMessage = null
                                            }
                                            is AiChunkResult.Waiting -> {
                                                waitingMessage = result.message
                                            }
                                            is AiChunkResult.Error -> {
                                                waitingMessage = "❌ Error: ${result.message}"
                                            }
                                        }
                                    }
                                    isAiProcessing = false
                                    waitingMessage = null
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
                            Text("🧠 Quiz")
                        }
                    }
                }
            }
        }

        if (isAiProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            Text("AI is thinking...")
        }

        if (waitingMessage != null) {
            Text(waitingMessage!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }

        if (!aiOutput.isNullOrEmpty() || aiSummarySections.isNotEmpty()) {
            if (showQuiz) {
                InteractiveQuizView(quizJson = aiOutput!!)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { isFullScreenSummary = true }) {
                        Text("Full Screen")
                    }
                }

                aiSummarySections.forEachIndexed { index, section ->
                    var isExplaining by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            RichTextView(markdown = section.content, modifier = Modifier.heightIn(min = 100.dp, max = 1000.dp).wrapContentHeight())

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (isExplaining) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    TextButton(onClick = {
                                        isExplaining = true
                                        coroutineScope.launch {
                                            val newContent = onlineAIManager.explainBetter(section)
                                            val updatedSections = aiSummarySections.toMutableList()
                                            updatedSections[index] = section.copy(content = newContent)
                                            aiSummarySections = updatedSections

                                            val updatedJson = com.google.gson.Gson().toJson(com.example.studyapp.data.local.SummaryResponse(updatedSections))
                                            aiOutput = updatedJson

                                            // Update history
                                            viewModel.saveDocumentWithSummary(documentTitle, parsedText!!, updatedJson)
                                            isExplaining = false
                                        }
                                    }) {
                                        Text("Explain Better")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFullScreenSummary && aiSummarySections.isNotEmpty() && !showQuiz) {
        // Overlay a full-screen surface that ignores standard dialog margins
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { isFullScreenSummary = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Summary Document", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { isFullScreenSummary = false }) {
                            Text("❌", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    aiSummarySections.forEachIndexed { index, section ->
                        var isExplaining by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                RichTextView(markdown = section.content, modifier = Modifier.heightIn(min = 100.dp, max = 1000.dp).wrapContentHeight())

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (isExplaining) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        TextButton(onClick = {
                                            isExplaining = true
                                            coroutineScope.launch {
                                                val newContent = onlineAIManager.explainBetter(section)
                                                val updatedSections = aiSummarySections.toMutableList()
                                                updatedSections[index] = section.copy(content = newContent)
                                                aiSummarySections = updatedSections

                                                val updatedJson = com.google.gson.Gson().toJson(com.example.studyapp.data.local.SummaryResponse(updatedSections))
                                                aiOutput = updatedJson

                                                viewModel.saveDocumentWithSummary(documentTitle, parsedText!!, updatedJson)
                                                isExplaining = false
                                            }
                                        }) {
                                            Text("Explain Better")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

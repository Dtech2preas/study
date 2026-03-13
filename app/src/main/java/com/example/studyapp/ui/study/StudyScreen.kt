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
import com.example.studyapp.ai.AIManager
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
    var isLoading by remember { mutableStateOf(false) }
    var aiOutput by remember { mutableStateOf<String?>(null) }

    // Initialize AI (will silently fail if dummy model isn't present, which is fine for now)
    LaunchedEffect(Unit) {
        AIManager.initModel(context)
    }

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
                    isLoading = false
                    if (text == null || text.isBlank()) {
                        Toast.makeText(context, "Failed to extract text or file is empty", Toast.LENGTH_SHORT).show()
                    }
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
            Text("Parsing document...")
        } else if (parsedText != null) {
            Text("Document Parsed (${parsedText!!.length} characters)", style = MaterialTheme.typography.labelMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    aiOutput = "Processing summary..."
                    coroutineScope.launch(Dispatchers.Default) {
                        aiOutput = AIManager.generateSummary(parsedText!!)
                    }
                }) {
                    Text("Summarize")
                }
                Button(onClick = {
                    aiOutput = "Generating quiz..."
                    coroutineScope.launch(Dispatchers.Default) {
                        aiOutput = AIManager.generateQuiz(parsedText!!)
                    }
                }) {
                    Text("Generate Quiz")
                }
            }
        }

        if (aiOutput != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = aiOutput!!,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

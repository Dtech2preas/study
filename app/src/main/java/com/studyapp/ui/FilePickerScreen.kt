package com.studyapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.studyapp.ai.AiManager
import com.studyapp.ai.QuizGenerator
import com.studyapp.ai.Summarizer
import com.studyapp.util.FileParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FilePickerScreen(aiManager: AiManager) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var parsedText by remember { mutableStateOf("") }
    var aiOutput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        uri?.let {
            coroutineScope.launch {
                isLoading = true
                try {
                    val text = withContext(Dispatchers.IO) {
                        FileParser.extractTextFromUri(context, it)
                    }
                    parsedText = text
                    aiOutput = "File uploaded and parsed. Ready to process."
                } catch (e: Exception) {
                    aiOutput = "Failed to parse file: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Upload Study Material", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = { filePickerLauncher.launch("*/*") }) {
            Text("Select File (.txt, .pdf)")
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else if (parsedText.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val summarizer = Summarizer(aiManager)
                    aiOutput = summarizer.summarize(parsedText)
                }) {
                    Text("Summarize")
                }
                Button(onClick = {
                    val quizGen = QuizGenerator(aiManager)
                    aiOutput = quizGen.generateQuiz(parsedText)
                }) {
                    Text("Generate Quiz")
                }
            }
        }

        if (aiOutput.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                Text(
                    text = aiOutput,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

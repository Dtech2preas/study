package com.example.studyapp.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.studyapp.ai.AiChunkResult
import com.example.studyapp.ai.OnlineAIManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: StudyViewModel) {
    val documents by viewModel.documentHistory.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedDocumentId by remember { mutableStateOf<Int?>(null) }

    if (selectedDocumentId != null) {
        HistoryDetailScreen(
            documentId = selectedDocumentId!!,
            viewModel = viewModel,
            onBack = { selectedDocumentId = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Document History", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

            if (documents.isEmpty()) {
                Text("No history yet. Start studying!", modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(documents) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedDocumentId = doc.id },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = doc.title, style = MaterialTheme.typography.titleMedium)
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(doc.timestamp))
                                    Text(text = dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch { viewModel.deleteDocumentHistory(doc.id) }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(documentId: Int, viewModel: StudyViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val onlineAIManager = remember { OnlineAIManager(context) }

    // Fetch details
    var document by remember { mutableStateOf<com.example.studyapp.data.local.DocumentHistory?>(null) }
    var summaries by remember { mutableStateOf(emptyList<com.example.studyapp.data.local.SummaryHistory>()) }
    var quizzes by remember { mutableStateOf(emptyList<com.example.studyapp.data.local.QuizHistory>()) }

    LaunchedEffect(documentId) {
        document = viewModel.getDocumentHistoryById(documentId)
    }
    LaunchedEffect(documentId) {
        viewModel.getSummariesForDocument(documentId).collect { summaries = it }
    }
    LaunchedEffect(documentId) {
        viewModel.getQuizzesForDocument(documentId).collect { quizzes = it }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var isGeneratingQuiz by remember { mutableStateOf(false) }
    var newlyGeneratedQuizJson by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<-") // Replace with back icon later
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Summaries") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Quizzes") })
            }

            if (selectedTab == 0) {
                LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(summaries) { summary ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                dev.jeziellago.compose.markdowntext.MarkdownText(markdown = summary.summaryText)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (document != null) {
                                            isGeneratingQuiz = true
                                            newlyGeneratedQuizJson = ""
                                            selectedTab = 1 // Switch to quiz tab to show loading
                                            coroutineScope.launch {
                                                onlineAIManager.generateQuizStream(document!!.originalText).collect { result ->
                                                    if (result is AiChunkResult.Success) {
                                                        newlyGeneratedQuizJson = result.text
                                                    }
                                                }
                                                isGeneratingQuiz = false
                                            }
                                        }
                                    },
                                    enabled = !isGeneratingQuiz
                                ) {
                                    Text("Generate Quiz from Document")
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isGeneratingQuiz) {
                        item {
                            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                            Text("Generating new quiz...")
                        }
                    } else if (newlyGeneratedQuizJson != null && newlyGeneratedQuizJson!!.isNotBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("New Unsaved Quiz", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    InteractiveQuizView(newlyGeneratedQuizJson!!)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Button(onClick = {
                                            coroutineScope.launch {
                                                viewModel.saveDocumentWithQuiz(document?.title ?: "Untitled", document?.originalText ?: "", newlyGeneratedQuizJson!!)
                                                newlyGeneratedQuizJson = null
                                                Toast.makeText(context, "Quiz Saved!", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Text("Save this Quiz")
                                        }
                                        OutlinedButton(onClick = { newlyGeneratedQuizJson = null }) {
                                            Text("Discard")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(quizzes) { quiz ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                               InteractiveQuizView(quiz.quizJson)
                            }
                        }
                    }
                }
            }
        }
    }
}

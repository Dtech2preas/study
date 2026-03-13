package com.studyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studyapp.ai.AiManager
import com.studyapp.data.AppDatabase
import com.studyapp.data.StudyRepository
import com.studyapp.ui.DashboardScreen
import com.studyapp.ui.FilePickerScreen
import com.studyapp.ui.StudyTrackerScreen
import com.studyapp.util.FileParser

class MainActivity : ComponentActivity() {
    private lateinit var aiManager: AiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileParser.init(this)

        aiManager = AiManager(this)
        aiManager.loadModel()

        val database = AppDatabase.getDatabase(this)
        val repository = StudyRepository(database.studyDao())

        setContent {
            StudyAppTheme {
                MainScreen(aiManager, repository)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        aiManager.closeModel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

@Composable
fun MainScreen(aiManager: AiManager, repository: StudyRepository) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("AI Study", "Tracker", "Dashboard")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.List
                                    1 -> Icons.Default.Home
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = item
                            )
                        },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "AI Study",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("AI Study") { FilePickerScreen(aiManager) }
            composable("Tracker") { StudyTrackerScreen(repository) }
            composable("Dashboard") { DashboardScreen(repository) }
        }
    }
}

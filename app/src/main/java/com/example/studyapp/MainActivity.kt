package com.example.studyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.navigation.compose.rememberNavController
import com.example.studyapp.ui.study.AnswersScreen
import com.example.studyapp.ui.study.DashboardScreen
import com.example.studyapp.ui.study.StudyScreen
import com.example.studyapp.ui.study.StudyViewModel
import com.example.studyapp.ui.settings.SettingsScreen
import com.example.studyapp.ui.theme.StudyAppTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        setContent {
            StudyAppTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Study,
        Screen.Dashboard,
        Screen.Answers,
        Screen.History,
        Screen.Settings
    )
    val studyViewModel: StudyViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val icon = when (screen.route) {
                        "study" -> Icons.Filled.Home
                        "dashboard" -> Icons.Filled.Info
                        "answers" -> Icons.Filled.Search
                        "history" -> Icons.Filled.List
                        else -> Icons.Filled.Settings
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Study.route, Modifier.padding(innerPadding)) {
            composable(Screen.Study.route) { StudyScreen(studyViewModel) }
            composable(Screen.Dashboard.route) { DashboardScreen(studyViewModel) }
            composable(Screen.Answers.route) { AnswersScreen(studyViewModel) }
            composable(Screen.History.route) { com.example.studyapp.ui.study.HistoryScreen(studyViewModel) }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

sealed class Screen(val route: String) {
    object Study : Screen("study")
    object Dashboard : Screen("dashboard")
    object Answers : Screen("answers")
    object History : Screen("history")
    object Settings : Screen("settings")
}

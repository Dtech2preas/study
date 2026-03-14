package com.example.studyapp.ui.study

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val todayDuration by viewModel.getTodayDuration().collectAsState(initial = 0L)
    val weekDuration by viewModel.getWeekDuration().collectAsState(initial = 0L)
    val monthDuration by viewModel.getMonthDuration().collectAsState(initial = 0L)
    val yearDuration by viewModel.getYearDuration().collectAsState(initial = 0L)
    val allTimeDuration by viewModel.getAllTimeDuration().collectAsState(initial = 0L)

    val longestSession by viewModel.getLongestSession().collectAsState(initial = 0L)
    val bestStudyDay by viewModel.getBestStudyDay().collectAsState(initial = null)
    val averageDaily by viewModel.getAverageDailyStudyTime().collectAsState(initial = 0L)
    val streak by viewModel.getCurrentStreak().collectAsState(initial = 0)

    val last7Days by viewModel.getLast7DaysStudyTime().collectAsState()

    // Generate JSON for chart data
    val labels = last7Days.map {
        val date = Date(it.date)
        SimpleDateFormat("EEE", Locale.getDefault()).format(date)
    }
    val data = last7Days.map { it.totalDuration / 3600f } // Convert to hours

    val labelsJson = labels.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]") { it }
    val dataJson = data.joinToString(prefix = "[", separator = ",", postfix = "]") { it.toString() }


    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
            <style>
                :root {
                    --bg-color: #0D0D0D;
                    --card-bg: #1A1A1A;
                    --text-main: #FFFFFF;
                    --text-muted: #A0A0A0;
                    --accent-blue: #00E5FF;
                    --dark-blue: #005B9F;
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    background-color: var(--bg-color);
                    color: var(--text-main);
                    padding: 20px;
                    margin: 0;
                }
                h1 {
                    color: var(--accent-blue);
                    font-size: 28px;
                    margin-bottom: 24px;
                }
                h2 {
                    font-size: 20px;
                    margin-top: 32px;
                    margin-bottom: 16px;
                    color: var(--text-main);
                }
                .card {
                    background-color: var(--card-bg);
                    border-radius: 12px;
                    padding: 20px;
                    margin-bottom: 20px;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
                    border: 1px solid #333;
                }
                .system-status {
                    border-left: 4px solid var(--accent-blue);
                }
                .status-title {
                    color: var(--accent-blue);
                    font-weight: bold;
                    margin-bottom: 8px;
                    text-transform: uppercase;
                    letter-spacing: 1px;
                }
                .status-text {
                    color: var(--text-muted);
                    font-style: italic;
                    line-height: 1.5;
                }

                .grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 16px;
                }
                .stat-box {
                    background-color: var(--card-bg);
                    border-radius: 12px;
                    padding: 16px;
                    border: 1px solid #333;
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                }
                .stat-label {
                    color: var(--text-muted);
                    font-size: 14px;
                    margin-bottom: 8px;
                }
                .stat-value {
                    color: var(--accent-blue);
                    font-size: 24px;
                    font-weight: bold;
                }

                .totals-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 12px 0;
                    border-bottom: 1px solid #333;
                }
                .totals-row:last-child {
                    border-bottom: none;
                }
                .totals-label {
                    font-size: 16px;
                }
                .totals-val {
                    font-size: 16px;
                    font-weight: bold;
                    color: var(--text-main);
                }

                #chart-container {
                    width: 100%;
                    height: 250px;
                }
            </style>
        </head>
        <body>
            <h1>Study Dashboard</h1>

            <div class="card system-status">
                <div class="status-title">⚡ System Status: Optimal</div>
                <div class="status-text">
                    Neural pathways primed for learning. Current objective: Mastery.<br><br>
                    "The future belongs to those who learn more skills and combine them in creative ways."
                </div>
            </div>

            <div class="card">
                <div class="stat-label" style="margin-bottom: 16px;">Last 7 Days (Hours)</div>
                <div id="chart-container">
                    <canvas id="studyChart"></canvas>
                </div>
            </div>

            <h2>Advanced Stats</h2>
            <div class="grid">
                <div class="stat-box">
                    <div class="stat-label">Current Streak</div>
                    <div class="stat-value">$streak Days</div>
                </div>
                <div class="stat-box">
                    <div class="stat-label">Best Day</div>
                    <div class="stat-value">${formatDurationHtml(bestStudyDay?.totalDuration ?: 0L)}</div>
                </div>
                <div class="stat-box">
                    <div class="stat-label">Longest Session</div>
                    <div class="stat-value">${formatDurationHtml(longestSession ?: 0L)}</div>
                </div>
                <div class="stat-box">
                    <div class="stat-label">Daily Average</div>
                    <div class="stat-value">${formatDurationHtml(averageDaily ?: 0L)}</div>
                </div>
            </div>

            <h2>Totals</h2>
            <div class="card">
                <div class="totals-row">
                    <div class="totals-label">Today</div>
                    <div class="totals-val">${formatDurationHtml(todayDuration ?: 0L)}</div>
                </div>
                <div class="totals-row">
                    <div class="totals-label">This Week</div>
                    <div class="totals-val">${formatDurationHtml(weekDuration ?: 0L)}</div>
                </div>
                <div class="totals-row">
                    <div class="totals-label">This Month</div>
                    <div class="totals-val">${formatDurationHtml(monthDuration ?: 0L)}</div>
                </div>
                <div class="totals-row">
                    <div class="totals-label">This Year</div>
                    <div class="totals-val">${formatDurationHtml(yearDuration ?: 0L)}</div>
                </div>
                <div class="totals-row">
                    <div class="totals-label">All Time</div>
                    <div class="totals-val">${formatDurationHtml(allTimeDuration ?: 0L)}</div>
                </div>
            </div>

            <script>
                const ctx = document.getElementById('studyChart').getContext('2d');

                // If there's no data, ensure we don't crash Chart.js
                let labels = $labelsJson;
                let data = $dataJson;

                if (labels.length === 0 || labels[0] === "") {
                    labels = ["No Data"];
                    data = [0];
                }

                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Hours Studied',
                            data: data,
                            borderColor: '#00E5FF',
                            backgroundColor: 'rgba(0, 229, 255, 0.1)',
                            borderWidth: 2,
                            pointBackgroundColor: '#00E5FF',
                            pointBorderColor: '#fff',
                            pointHoverBackgroundColor: '#fff',
                            pointHoverBorderColor: '#00E5FF',
                            fill: true,
                            tension: 0.4
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                display: false
                            },
                            tooltip: {
                                backgroundColor: '#1A1A1A',
                                titleColor: '#fff',
                                bodyColor: '#A0A0A0',
                                borderColor: '#333',
                                borderWidth: 1
                            }
                        },
                        scales: {
                            x: {
                                grid: {
                                    display: false,
                                    drawBorder: false
                                },
                                ticks: {
                                    color: '#A0A0A0'
                                }
                            },
                            y: {
                                grid: {
                                    color: '#333',
                                    drawBorder: false
                                },
                                ticks: {
                                    color: '#A0A0A0',
                                    beginAtZero: true
                                }
                            }
                        }
                    }
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    HtmlWebView(htmlContent = htmlContent, modifier = Modifier.fillMaxSize())
}

fun formatDurationHtml(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

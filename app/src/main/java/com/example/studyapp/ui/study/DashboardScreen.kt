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
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap');

                :root {
                    --bg-color: #0D0D0D;
                    --card-bg: #1A1A1A;
                    --card-border: #2A2A2A;
                    --text-main: #FFFFFF;
                    --text-muted: #888888;
                    --accent-blue: #00E5FF;
                    --accent-blue-glow: rgba(0, 229, 255, 0.4);
                    --dark-blue: #005B9F;
                    --gradient-start: #111111;
                    --gradient-end: #1A1A1A;
                }

                * {
                    box-sizing: border-box;
                }

                body {
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    background-color: var(--bg-color);
                    color: var(--text-main);
                    padding: 24px 20px;
                    margin: 0;
                    line-height: 1.6;
                }

                .header-container {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    margin-bottom: 28px;
                }

                h1 {
                    color: var(--text-main);
                    font-size: 32px;
                    font-weight: 800;
                    margin: 0;
                    letter-spacing: -0.5px;
                }

                .h1-accent {
                    color: var(--accent-blue);
                }

                h2 {
                    font-size: 22px;
                    font-weight: 600;
                    margin-top: 36px;
                    margin-bottom: 16px;
                    color: var(--text-main);
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }

                h2::before {
                    content: '';
                    display: inline-block;
                    width: 4px;
                    height: 20px;
                    background-color: var(--accent-blue);
                    border-radius: 4px;
                }

                .card {
                    background: linear-gradient(145deg, var(--gradient-start), var(--gradient-end));
                    border-radius: 16px;
                    padding: 24px;
                    margin-bottom: 24px;
                    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
                    border: 1px solid var(--card-border);
                    position: relative;
                    overflow: hidden;
                }

                .card::after {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    height: 1px;
                    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
                }

                .system-status {
                    background: rgba(0, 229, 255, 0.03);
                    border: 1px solid rgba(0, 229, 255, 0.1);
                }

                .status-header {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    margin-bottom: 12px;
                }

                .status-indicator {
                    width: 10px;
                    height: 10px;
                    border-radius: 50%;
                    background-color: var(--accent-blue);
                    box-shadow: 0 0 10px var(--accent-blue-glow);
                    animation: pulse 2s infinite;
                }

                @keyframes pulse {
                    0% { box-shadow: 0 0 0 0 var(--accent-blue-glow); }
                    70% { box-shadow: 0 0 0 10px rgba(0, 229, 255, 0); }
                    100% { box-shadow: 0 0 0 0 rgba(0, 229, 255, 0); }
                }

                .status-title {
                    color: var(--accent-blue);
                    font-weight: 600;
                    font-size: 14px;
                    text-transform: uppercase;
                    letter-spacing: 1.5px;
                    margin: 0;
                }

                .status-text {
                    color: var(--text-muted);
                    font-size: 15px;
                }

                .quote {
                    margin-top: 12px;
                    padding-left: 12px;
                    border-left: 2px solid rgba(255,255,255,0.1);
                    font-style: italic;
                    color: #A0A0A0;
                }

                .grid {
                    display: grid;
                    grid-template-columns: repeat(2, 1fr);
                    gap: 16px;
                }

                .stat-box {
                    background: var(--card-bg);
                    border-radius: 16px;
                    padding: 20px;
                    border: 1px solid var(--card-border);
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    transition: transform 0.2s ease, border-color 0.2s ease;
                }

                .stat-box:hover {
                    transform: translateY(-2px);
                    border-color: rgba(0, 229, 255, 0.3);
                }

                .stat-icon {
                    font-size: 20px;
                    margin-bottom: 8px;
                    opacity: 0.8;
                }

                .stat-label {
                    color: var(--text-muted);
                    font-size: 13px;
                    font-weight: 600;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    margin-bottom: 4px;
                }

                .stat-value {
                    color: var(--text-main);
                    font-size: 28px;
                    font-weight: 800;
                    text-shadow: 0 2px 10px rgba(0,0,0,0.5);
                }

                .stat-value.highlight {
                    color: var(--accent-blue);
                }

                .totals-list {
                    display: flex;
                    flex-direction: column;
                    gap: 4px;
                }

                .totals-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 16px;
                    background: rgba(255,255,255,0.02);
                    border-radius: 12px;
                    transition: background 0.2s;
                }

                .totals-row:hover {
                    background: rgba(255,255,255,0.05);
                }

                .totals-label {
                    font-size: 15px;
                    font-weight: 600;
                    color: var(--text-muted);
                }

                .totals-val {
                    font-size: 18px;
                    font-weight: 800;
                    color: var(--text-main);
                    background: rgba(0, 229, 255, 0.1);
                    padding: 4px 12px;
                    border-radius: 20px;
                    border: 1px solid rgba(0, 229, 255, 0.2);
                }

                .chart-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 20px;
                }

                .chart-title {
                    color: var(--text-muted);
                    font-size: 14px;
                    font-weight: 600;
                    text-transform: uppercase;
                    letter-spacing: 1px;
                }

                #chart-container {
                    width: 100%;
                    height: 280px;
                    position: relative;
                }
            </style>
        </head>
        <body>
            <div class="header-container">
                <h1>Study <span class="h1-accent">Dashboard</span></h1>
            </div>

            <div class="card system-status">
                <div class="status-header">
                    <div class="status-indicator"></div>
                    <div class="status-title">System Status: Optimal</div>
                </div>
                <div class="status-text">
                    Neural pathways primed for learning. Current objective: Mastery.
                    <div class="quote">"The future belongs to those who learn more skills and combine them in creative ways."</div>
                </div>
            </div>

            <div class="card">
                <div class="chart-header">
                    <div class="chart-title">Activity (Last 7 Days)</div>
                </div>
                <div id="chart-container">
                    <canvas id="studyChart"></canvas>
                </div>
            </div>

            <h2>Performance Metrics</h2>
            <div class="grid">
                <div class="stat-box">
                    <div class="stat-icon">🔥</div>
                    <div class="stat-label">Current Streak</div>
                    <div class="stat-value highlight">$streak <span style="font-size: 16px; color: var(--text-muted); font-weight: 600;">Days</span></div>
                </div>
                <div class="stat-box">
                    <div class="stat-icon">⭐</div>
                    <div class="stat-label">Best Day</div>
                    <div class="stat-value">${formatDurationHtml(bestStudyDay?.totalDuration ?: 0L)}</div>
                </div>
                <div class="stat-box">
                    <div class="stat-icon">⏱️</div>
                    <div class="stat-label">Longest Session</div>
                    <div class="stat-value">${formatDurationHtml(longestSession ?: 0L)}</div>
                </div>
                <div class="stat-box">
                    <div class="stat-icon">📊</div>
                    <div class="stat-label">Daily Average</div>
                    <div class="stat-value">${formatDurationHtml(averageDaily ?: 0L)}</div>
                </div>
            </div>

            <h2>Time Distribution</h2>
            <div class="card" style="padding: 12px;">
                <div class="totals-list">
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
                            backgroundColor: (context) => {
                                const ctx = context.chart.ctx;
                                const gradient = ctx.createLinearGradient(0, 0, 0, 300);
                                gradient.addColorStop(0, 'rgba(0, 229, 255, 0.3)');
                                gradient.addColorStop(1, 'rgba(0, 229, 255, 0.0)');
                                return gradient;
                            },
                            borderWidth: 3,
                            pointBackgroundColor: '#1A1A1A',
                            pointBorderColor: '#00E5FF',
                            pointBorderWidth: 2,
                            pointRadius: 4,
                            pointHoverBackgroundColor: '#00E5FF',
                            pointHoverBorderColor: '#fff',
                            pointHoverRadius: 6,
                            fill: true,
                            tension: 0.4
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        interaction: {
                            mode: 'index',
                            intersect: false,
                        },
                        plugins: {
                            legend: {
                                display: false
                            },
                            tooltip: {
                                backgroundColor: 'rgba(26, 26, 26, 0.9)',
                                titleColor: '#fff',
                                bodyColor: '#00E5FF',
                                bodyFont: {
                                    weight: 'bold'
                                },
                                padding: 12,
                                borderColor: 'rgba(0, 229, 255, 0.2)',
                                borderWidth: 1,
                                displayColors: false,
                                callbacks: {
                                    label: function(context) {
                                        return context.parsed.y.toFixed(1) + ' Hours';
                                    }
                                }
                            }
                        },
                        scales: {
                            x: {
                                grid: {
                                    display: false,
                                    drawBorder: false
                                },
                                ticks: {
                                    color: '#888',
                                    font: {
                                        family: 'Inter',
                                        size: 11
                                    }
                                }
                            },
                            y: {
                                grid: {
                                    color: 'rgba(255, 255, 255, 0.05)',
                                    drawBorder: false,
                                    borderDash: [5, 5]
                                },
                                ticks: {
                                    color: '#888',
                                    font: {
                                        family: 'Inter',
                                        size: 11
                                    },
                                    beginAtZero: true,
                                    maxTicksLimit: 6
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

package com.example.studyapp.ui.study

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun RichTextView(markdown: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val encodedMarkdown = Base64.encodeToString(markdown.toByteArray(), Base64.NO_WRAP)
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
        }
    }

    // A lightweight HTML template using Marked.js for markdown, Highlight.js for code,
    // and MathJax for math formatting.
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

            <!-- Marked.js for Markdown parsing -->
            <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>

            <!-- Highlight.js for Code Syntax Highlighting -->
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/styles/github-dark.min.css">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.8.0/highlight.min.js"></script>

            <!-- MathJax for Math/LaTeX -->
            <script>
                MathJax = {
                    tex: {
                        inlineMath: [['$', '$'], ['\\(', '\\)']],
                        displayMath: [['$$', '$$'], ['\\[', '\\]']]
                    },
                    svg: {
                        fontCache: 'global'
                    }
                };
            </script>
            <script type="text/javascript" id="MathJax-script" async
                src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-svg.js">
            </script>

            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    line-height: 1.6;
                    padding: 16px;
                    color: #333;
                    background-color: #f9f9f9;
                }
                h1, h2, h3 {
                    color: #222;
                    border-bottom: 1px solid #ddd;
                    padding-bottom: 5px;
                }
                pre {
                    background: #282c34;
                    color: #abb2bf;
                    padding: 15px;
                    border-radius: 8px;
                    overflow-x: auto;
                    font-size: 14px;
                }
                code {
                    font-family: "Courier New", Courier, monospace;
                    background-color: #eee;
                    padding: 2px 4px;
                    border-radius: 4px;
                }
                pre code {
                    background-color: transparent;
                    padding: 0;
                }
                blockquote {
                    border-left: 4px solid #007bff;
                    padding-left: 15px;
                    color: #555;
                    font-style: italic;
                    margin-left: 0;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 15px;
                }
                th, td {
                    border: 1px solid #ddd;
                    padding: 8px;
                    text-align: left;
                }
                th {
                    background-color: #f2f2f2;
                }

                @media (prefers-color-scheme: dark) {
                    body {
                        color: #e0e0e0;
                        background-color: #121212;
                    }
                    h1, h2, h3 {
                        color: #fff;
                        border-bottom: 1px solid #444;
                    }
                    code {
                        background-color: #333;
                        color: #f8f8f2;
                    }
                    th {
                        background-color: #333;
                    }
                    th, td {
                        border: 1px solid #444;
                    }
                    blockquote {
                        border-left-color: #bb86fc;
                        color: #ccc;
                    }
                }
            </style>
        </head>
        <body>
            <div id="content"></div>

            <script>
                // Decode the base64 markdown string safely
                const encodedMarkdown = "$encodedMarkdown";
                const rawMarkdown = decodeURIComponent(escape(atob(encodedMarkdown)));

                document.getElementById('content').innerHTML = marked.parse(rawMarkdown);

                // Apply syntax highlighting
                hljs.highlightAll();

                // Tell MathJax to typeset the newly added math elements
                if (window.MathJax) {
                    MathJax.typesetPromise();
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { webView },
        update = {
            it.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    )
}

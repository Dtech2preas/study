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
fun HtmlWebView(htmlContent: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { webView },
        update = {
            it.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }
    )
}

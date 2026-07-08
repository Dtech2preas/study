package com.example.studyapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.studyapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object AppUpdater {
    private const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/Dtech2preas/study/releases/latest"
    private val client = OkHttpClient()

    suspend fun checkForUpdates(context: Context, showToasts: Boolean = true) {
        withContext(Dispatchers.IO) {
            try {
                if (showToasts) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
                    }
                }

                val request = Request.Builder()
                    .url(GITHUB_LATEST_RELEASE_URL)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    if (showToasts) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to check for updates.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@withContext
                }

                val responseBody = response.body?.string() ?: return@withContext
                val jsonResponse = JSONObject(responseBody)
                val latestTag = jsonResponse.optString("tag_name")

                val currentTag = BuildConfig.GIT_TAG

                if (currentTag == "dev-build") {
                    if (showToasts) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Running dev build. Updates disabled.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@withContext
                }

                if (latestTag != currentTag && latestTag.isNotEmpty()) {
                    val assets = jsonResponse.optJSONArray("assets")
                    var downloadUrl: String? = null

                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.optJSONObject(i)
                            if (asset != null) {
                                val name = asset.optString("name")
                                if (name.endsWith(".apk")) {
                                    downloadUrl = asset.optString("browser_download_url")
                                    break
                                }
                            }
                        }
                    }

                    if (downloadUrl != null) {
                        if (showToasts) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Downloading update...", Toast.LENGTH_SHORT).show()
                            }
                        }
                        downloadAndInstallApk(context, downloadUrl)
                    } else {
                         if (showToasts) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "No APK found in the release.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    if (showToasts) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "App is up-to-date.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (showToasts) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error checking for updates.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private suspend fun downloadAndInstallApk(context: Context, downloadUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(downloadUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val body = response.body
                if (body != null) {
                    val file = File(context.cacheDir, "update.apk")
                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(file)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }

                    installApk(context, file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                 withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error downloading update.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun installApk(context: Context, file: File) {
        withContext(Dispatchers.Main) {
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to launch installer.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
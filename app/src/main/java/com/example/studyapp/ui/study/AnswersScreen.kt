package com.example.studyapp.ui.study

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.studyapp.ai.OnlineAIManager
import com.example.studyapp.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.File
import java.io.IOException
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AnswersScreen(viewModel: StudyViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentCameraUri by remember { mutableStateOf<Uri?>(null) }

    var isProcessing by remember { mutableStateOf(false) }
    var extractedText by remember { mutableStateOf("") }
    var aiAnswer by remember { mutableStateOf("") }
    val onlineAIManager = remember { OnlineAIManager(context) }

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val pickMultipleMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(3)) { uris ->
        if (uris.isNotEmpty()) {
            val total = selectedImageUris.size + uris.size
            if (total > 3) {
                Toast.makeText(context, "You can select up to 3 images total.", Toast.LENGTH_SHORT).show()
                val spaceLeft = 3 - selectedImageUris.size
                if (spaceLeft > 0) {
                    selectedImageUris = selectedImageUris + uris.take(spaceLeft)
                }
            } else {
                selectedImageUris = selectedImageUris + uris
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentCameraUri != null) {
            if (selectedImageUris.size < 3) {
                selectedImageUris = selectedImageUris + currentCameraUri!!
            } else {
                Toast.makeText(context, "Maximum 3 images allowed.", Toast.LENGTH_SHORT).show()
            }
        }
        currentCameraUri = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Answers", color = TextWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumBlack)
            )
        },
        containerColor = SurfaceBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Upload or snap up to 3 images of your question to get an AI answer.", color = TextGray)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (selectedImageUris.size >= 3) {
                            Toast.makeText(context, "Max 3 images reached", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cameraPermissionState.status.isGranted) {
                            val uri = createImageUri(context)
                            if (uri != null) {
                                currentCameraUri = uri
                                takePictureLauncher.launch(uri)
                            }
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Take Photo")
                }

                Button(
                    onClick = {
                        if (selectedImageUris.size >= 3) {
                            Toast.makeText(context, "Max 3 images reached", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Upload Photo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Preview
            if (selectedImageUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImageUris) { uri ->
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                modifier = Modifier.size(100.dp),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    selectedImageUris = selectedImageUris.filter { it != uri }
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            isProcessing = true
                            try {
                                val text = processImagesForText(context, selectedImageUris)
                                extractedText = text
                                if (text.isNotBlank()) {
                                    val answer = onlineAIManager.generateAnswerForQuestion(text)
                                    aiAnswer = answer
                                } else {
                                    aiAnswer = "Could not find any text in the images."
                                }
                            } catch (e: Exception) {
                                aiAnswer = "Error processing image: ${e.localizedMessage}"
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Get Answer")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    DynamicWaveLoader()
                }
            } else if (aiAnswer.isNotEmpty()) {
                // Show Answer using RichTextView
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Result", color = TextWhite, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))

                        val answerText = """
                            **Extracted Text:**
                            $extractedText

                            **AI Answer:**
                            $aiAnswer
                        """.trimIndent()

                        RichTextView(
                            markdown = answerText,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

suspend fun processImagesForText(context: Context, uris: List<Uri>): String {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val stringBuilder = StringBuilder()

    for (uri in uris) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(image).await()
            stringBuilder.append(result.text).append("\n\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return stringBuilder.toString()
}

fun createImageUri(context: Context): Uri? {
    val imageFile = File(context.cacheDir, "camera_capture_${UUID.randomUUID()}.jpg")
    return try {
        imageFile.createNewFile()
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

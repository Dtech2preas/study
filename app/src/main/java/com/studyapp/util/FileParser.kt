package com.studyapp.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStreamReader

object FileParser {
    fun init(context: Context) {
        PDFBoxResourceLoader.init(context)
    }

    fun extractTextFromUri(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: ""

        return try {
            when {
                mimeType.contains("pdf") || uri.toString().endsWith(".pdf", ignoreCase = true) -> {
                    extractTextFromPdf(context, uri)
                }
                mimeType.contains("text") || uri.toString().endsWith(".txt", ignoreCase = true) -> {
                    extractTextFromTxt(context, uri)
                }
                else -> {
                    "Unsupported file format"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error extracting text: ${e.message}"
        }
    }

    private fun extractTextFromTxt(context: Context, uri: Uri): String {
        val stringBuilder = java.lang.StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun extractTextFromPdf(context: Context, uri: Uri): String {
        var document: PDDocument? = null
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                stripper.getText(document) ?: ""
            } ?: "Could not open stream"
        } finally {
            document?.close()
        }
    }
}

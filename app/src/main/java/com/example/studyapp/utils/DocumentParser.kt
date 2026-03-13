package com.example.studyapp.utils

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

object DocumentParser {
    fun extractTextFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri) ?: ""

        return try {
            when {
                type.contains("pdf") || uri.path?.endsWith(".pdf") == true -> {
                    extractTextFromPdf(context, uri)
                }
                type.contains("text") || uri.path?.endsWith(".txt") == true -> {
                    extractTextFromTxt(context, uri)
                }
                else -> {
                    // Fallback
                    extractTextFromTxt(context, uri)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractTextFromPdf(context: Context, uri: Uri): String {
        var text = ""
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            text = stripper.getText(document)
            document.close()
        }
        return text
    }

    private fun extractTextFromTxt(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
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
}

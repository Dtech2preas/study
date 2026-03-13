package com.example.studyapp.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.apache.poi.extractor.ExtractorFactory
import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader

object DocumentParser {
    fun extractTextFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri) ?: ""

        val fileName = getFileName(context, uri) ?: ""

        return try {
            when {
                type.contains("pdf") || fileName.endsWith(".pdf", ignoreCase = true) -> {
                    extractTextFromPdf(context, uri)
                }
                type.contains("text") || fileName.endsWith(".txt", ignoreCase = true) -> {
                    extractTextFromTxt(context, uri)
                }
                fileName.endsWith(".doc", ignoreCase = true) ||
                fileName.endsWith(".docx", ignoreCase = true) ||
                fileName.endsWith(".xls", ignoreCase = true) ||
                fileName.endsWith(".xlsx", ignoreCase = true) ||
                fileName.endsWith(".ppt", ignoreCase = true) ||
                fileName.endsWith(".pptx", ignoreCase = true) ||
                type.contains("msword") ||
                type.contains("vnd.openxmlformats-officedocument") ||
                type.contains("vnd.ms-excel") ||
                type.contains("vnd.ms-powerpoint") -> {
                    extractTextFromOffice(context, uri)
                }
                else -> {
                    // Fallback
                    extractTextFromTxt(context, uri)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun extractTextFromOffice(context: Context, uri: Uri): String {
        var text = ""
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val extractor = ExtractorFactory.createExtractor(inputStream)
            text = extractor.text
            extractor.close()
        }
        return text
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

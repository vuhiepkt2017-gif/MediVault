package com.example.medivault.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OcrAnalyzer {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Analyzes a Bitmap image to extract text using Google ML Kit.
     * Invokes [onSuccess] with the list of detected text blocks, or [onFailure] upon error.
     */
    fun analyzeImage(
        bitmap: Bitmap,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val textLines = mutableListOf<String>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        textLines.add(line.text)
                    }
                }
                onSuccess(textLines)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}

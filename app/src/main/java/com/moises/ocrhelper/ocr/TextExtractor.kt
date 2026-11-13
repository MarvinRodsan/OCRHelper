package com.moises.ocrhelper.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Contrato del OCR. Los ViewModels solo conocen esta interfaz, no ML Kit.
 * Recibe la URI como String para que los ViewModels no dependan de clases
 * de Android y se puedan probar con tests unitarios normales.
 */
interface TextExtractor {
    suspend fun extract(imageUri: String): String
}

/** Implementación real con ML Kit (modelo latino, funciona offline). */
class MlKitTextExtractor(context: Context) : TextExtractor {

    // applicationContext para no retener la Activity
    private val appContext = context.applicationContext
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extract(imageUri: String): String {
        // Leer y decodificar la imagen es trabajo pesado: fuera del hilo principal.
        // fromFilePath también corrige la rotación según el EXIF de la foto.
        val image = withContext(Dispatchers.IO) {
            InputImage.fromFilePath(appContext, Uri.parse(imageUri))
        }

        // ML Kit devuelve un Task con callbacks; aquí lo convertimos en función suspend
        val result: Text = suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { text -> continuation.resume(text) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
        }

        return toOrderedText(result)
    }

    private fun toOrderedText(result: Text): String {
        val mlKitLines = result.textBlocks.flatMap { it.lines }
        val lines = mlKitLines.mapNotNull { line ->
            line.boundingBox?.let { box -> TextLine(line.text, box.left, box.top, box.bottom) }
        }
        // Si alguna línea vino sin posición, no podemos ordenar con seguridad:
        // usamos el texto tal como lo entrega ML Kit.
        return if (lines.size == mlKitLines.size) orderLines(lines) else result.text
    }
}

package com.moises.ocrhelper

import android.app.Application
import android.content.Context
import com.moises.ocrhelper.data.AppDatabase
import com.moises.ocrhelper.ocr.MlKitTextExtractor
import com.moises.ocrhelper.ocr.TextExtractor
import com.moises.ocrhelper.repository.DocumentRepository
import com.moises.ocrhelper.repository.RoomDocumentRepository

/** Clase Application: vive mientras la app esté abierta y guarda las dependencias compartidas. */
class OCRHelperApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

/**
 * Inyección de dependencias manual: un solo lugar donde se crean
 * el OCR y el repositorio, para que los ViewModels los reciban ya hechos.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val textExtractor: TextExtractor by lazy { MlKitTextExtractor(appContext) }

    val documentRepository: DocumentRepository by lazy {
        RoomDocumentRepository(AppDatabase.getInstance(appContext).documentDao())
    }
}

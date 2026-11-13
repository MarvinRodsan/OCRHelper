package com.moises.ocrhelper.util

import com.moises.ocrhelper.data.DocumentEntity
import com.moises.ocrhelper.ocr.TextExtractor
import com.moises.ocrhelper.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** OCR falso: devuelve un texto fijo o lanza el error indicado. */
class FakeTextExtractor(
    private val text: String = "",
    private val error: Exception? = null
) : TextExtractor {
    override suspend fun extract(imageUri: String): String {
        error?.let { throw it }
        return text
    }
}

/** Repositorio en memoria que imita el comportamiento de Room. */
class FakeDocumentRepository : DocumentRepository {
    private val documents = MutableStateFlow<List<DocumentEntity>>(emptyList())
    private var nextId = 1L

    val saved: List<DocumentEntity> get() = documents.value

    override fun observeDocuments(query: String): Flow<List<DocumentEntity>> =
        documents.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }.sortedByDescending { it.createdAt }
        }

    override suspend fun getDocument(id: Long): DocumentEntity? =
        documents.value.firstOrNull { it.id == id }

    override suspend fun save(title: String, content: String): Long {
        val id = nextId++
        documents.value = documents.value +
            DocumentEntity(id = id, title = title, content = content, createdAt = id)
        return id
    }

    override suspend fun delete(id: Long) {
        documents.value = documents.value.filterNot { it.id == id }
    }
}

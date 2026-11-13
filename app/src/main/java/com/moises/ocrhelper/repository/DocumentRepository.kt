package com.moises.ocrhelper.repository

import com.moises.ocrhelper.data.DocumentDao
import com.moises.ocrhelper.data.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Lo que los ViewModels necesitan saber sobre los documentos.
 * Al ser una interfaz, en los tests se sustituye por una versión en memoria.
 */
interface DocumentRepository {
    fun observeDocuments(query: String): Flow<List<DocumentEntity>>
    suspend fun getDocument(id: Long): DocumentEntity?
    suspend fun save(title: String, content: String): Long
    suspend fun delete(id: Long)
}

/** Implementación real, respaldada por Room. */
class RoomDocumentRepository(
    private val dao: DocumentDao,
    private val now: () -> Long = System::currentTimeMillis
) : DocumentRepository {

    override fun observeDocuments(query: String): Flow<List<DocumentEntity>> = dao.search(query)

    override suspend fun getDocument(id: Long): DocumentEntity? = dao.getById(id)

    override suspend fun save(title: String, content: String): Long =
        dao.insert(DocumentEntity(title = title, content = content, createdAt = now()))

    override suspend fun delete(id: Long) = dao.deleteById(id)
}

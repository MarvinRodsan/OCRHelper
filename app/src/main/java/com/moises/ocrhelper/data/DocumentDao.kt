package com.moises.ocrhelper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    /**
     * Busca en título y contenido. Con una búsqueda vacía devuelve todos.
     * Al devolver Flow, la lista se actualiza sola cuando cambia la tabla.
     */
    @Query(
        "SELECT * FROM documents " +
            "WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' " +
            "ORDER BY createdAt DESC"
    )
    fun search(query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: Long): DocumentEntity?

    @Insert
    suspend fun insert(document: DocumentEntity): Long

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: Long)
}

package com.moises.ocrhelper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un documento guardado: una fila de la tabla "documents". */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    /** Fecha de creación en milisegundos (System.currentTimeMillis()). */
    val createdAt: Long
)

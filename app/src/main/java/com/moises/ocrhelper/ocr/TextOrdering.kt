package com.moises.ocrhelper.ocr

/** Una línea de texto detectada y su posición en la imagen (en píxeles). */
data class TextLine(
    val text: String,
    val left: Int,
    val top: Int,
    val bottom: Int
)

/**
 * Ordena las líneas como se leen: de arriba hacia abajo y, dentro de la
 * misma fila, de izquierda a derecha.
 *
 * ML Kit a veces devuelve los bloques en un orden distinto al visual
 * (por ejemplo, un texto corto alineado a un lado aparece antes de tiempo).
 * Dos líneas se consideran de la misma fila si el centro vertical de una
 * cae dentro de la altura de la primera línea de esa fila; así "Producto" y
 * su precio a la derecha quedan en el mismo renglón.
 */
fun orderLines(lines: List<TextLine>): String {
    val rows = mutableListOf<MutableList<TextLine>>()

    for (line in lines.sortedBy { it.top }) {
        val reference = rows.lastOrNull()?.first()
        val centerY = (line.top + line.bottom) / 2
        if (reference != null && centerY in reference.top..reference.bottom) {
            rows.last().add(line)
        } else {
            rows.add(mutableListOf(line))
        }
    }

    return rows.joinToString("\n") { row ->
        row.sortedBy { it.left }.joinToString(" ") { it.text }
    }
}

private const val MAX_TITLE_LENGTH = 40
const val DEFAULT_TITLE = "Documento sin título"

/** Propone un título a partir de la primera línea con contenido del texto. */
fun suggestTitle(text: String): String {
    val firstLine = text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }
        ?: return DEFAULT_TITLE
    return firstLine.take(MAX_TITLE_LENGTH).trim()
}

package com.moises.ocrhelper.ocr

import org.junit.Assert.assertEquals
import org.junit.Test

class TextOrderingTest {

    @Test
    fun `sin lineas devuelve texto vacio`() {
        assertEquals("", orderLines(emptyList()))
    }

    @Test
    fun `ordena de arriba hacia abajo aunque lleguen desordenadas`() {
        val lines = listOf(
            TextLine("tercera", left = 0, top = 200, bottom = 230),
            TextLine("primera", left = 0, top = 0, bottom = 30),
            TextLine("segunda", left = 0, top = 100, bottom = 130)
        )

        assertEquals("primera\nsegunda\ntercera", orderLines(lines))
    }

    @Test
    fun `une en un renglon las lineas a la misma altura, de izquierda a derecha`() {
        val lines = listOf(
            TextLine("B/. 4.50", left = 400, top = 102, bottom = 131),
            TextLine("Cemento", left = 10, top = 100, bottom = 130),
            TextLine("Total", left = 10, top = 160, bottom = 190)
        )

        assertEquals("Cemento B/. 4.50\nTotal", orderLines(lines))
    }

    @Test
    fun `lineas consecutivas que no se solapan quedan en renglones distintos`() {
        val lines = listOf(
            TextLine("uno", left = 0, top = 0, bottom = 30),
            TextLine("dos", left = 300, top = 35, bottom = 65)
        )

        assertEquals("uno\ndos", orderLines(lines))
    }

    @Test
    fun `el titulo sugerido es la primera linea con contenido`() {
        assertEquals("Factura 001", suggestTitle("\n   \n  Factura 001  \nCliente: X"))
    }

    @Test
    fun `el titulo sugerido se recorta a 40 caracteres`() {
        val title = suggestTitle("a".repeat(100))

        assertEquals(40, title.length)
    }

    @Test
    fun `texto vacio usa el titulo por defecto`() {
        assertEquals(DEFAULT_TITLE, suggestTitle("   \n  "))
    }
}

package com.moises.ocrhelper.viewmodel

import com.moises.ocrhelper.ocr.DEFAULT_TITLE
import com.moises.ocrhelper.util.FakeDocumentRepository
import com.moises.ocrhelper.util.FakeTextExtractor
import com.moises.ocrhelper.util.MainDispatcherRule
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeDocumentRepository()

    private fun createViewModel(text: String = "", error: Exception? = null) =
        ResultViewModel("content://imagen", FakeTextExtractor(text, error), repository)

    @Test
    fun `al reconocer muestra el texto y sugiere un titulo`() {
        val viewModel = createViewModel(text = "Recibo 123\nTotal 10.00")

        assertEquals(
            ResultUiState.Ready(title = "Recibo 123", text = "Recibo 123\nTotal 10.00"),
            viewModel.uiState.value
        )
    }

    @Test
    fun `si el OCR falla muestra el error`() {
        val viewModel = createViewModel(error = IOException("imagen ilegible"))

        assertEquals(ResultUiState.Error("imagen ilegible"), viewModel.uiState.value)
    }

    @Test
    fun `guardar almacena el texto corregido por el usuario`() {
        val viewModel = createViewModel(text = "henmos cubierto")
        viewModel.onTitleChange("Mi nota")
        viewModel.onTextChange("hemos cubierto")

        viewModel.save()

        val saved = repository.saved.single()
        assertEquals("Mi nota", saved.title)
        assertEquals("hemos cubierto", saved.content)
        assertTrue((viewModel.uiState.value as ResultUiState.Ready).isSaved)
    }

    @Test
    fun `guardar dos veces crea un solo documento`() {
        val viewModel = createViewModel(text = "texto")

        viewModel.save()
        viewModel.save()

        assertEquals(1, repository.saved.size)
    }

    @Test
    fun `no guarda documentos sin texto`() {
        val viewModel = createViewModel(text = "")

        viewModel.save()

        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `con titulo vacio usa uno sugerido a partir del texto`() {
        val viewModel = createViewModel(text = "Primera linea\nSegunda")
        viewModel.onTitleChange("   ")

        viewModel.save()

        assertEquals("Primera linea", repository.saved.single().title)
    }

    @Test
    fun `sin texto reconocido el titulo es el de por defecto`() {
        val viewModel = createViewModel(text = "")

        assertEquals(DEFAULT_TITLE, (viewModel.uiState.value as ResultUiState.Ready).title)
    }
}

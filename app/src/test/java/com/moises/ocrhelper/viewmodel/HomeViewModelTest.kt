package com.moises.ocrhelper.viewmodel

import com.moises.ocrhelper.util.FakeDocumentRepository
import com.moises.ocrhelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeDocumentRepository()

    @Test
    fun `lista los documentos del mas reciente al mas antiguo`() = runTest {
        repository.save("Factura", "cemento y arena")
        repository.save("Nota", "reunión el lunes")
        val viewModel = HomeViewModel(repository)
        // documents solo consulta mientras alguien lo observa (como hace la pantalla)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.documents.collect {}
        }

        assertEquals(listOf("Nota", "Factura"), viewModel.documents.value.map { it.title })
    }

    @Test
    fun `la busqueda filtra por titulo y por contenido`() = runTest {
        repository.save("Factura", "cemento y arena")
        repository.save("Nota", "reunión el lunes")
        val viewModel = HomeViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.documents.collect {}
        }

        viewModel.onQueryChange("cemento")
        assertEquals(listOf("Factura"), viewModel.documents.value.map { it.title })

        viewModel.onQueryChange("  nota ")
        assertEquals(listOf("Nota"), viewModel.documents.value.map { it.title })

        viewModel.onQueryChange("no existe")
        assertTrue(viewModel.documents.value.isEmpty())
    }

    @Test
    fun `eliminar quita el documento de la lista`() = runTest {
        val id = repository.save("Factura", "cemento")
        val viewModel = HomeViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.documents.collect {}
        }

        viewModel.delete(id)

        assertTrue(viewModel.documents.value.isEmpty())
    }
}

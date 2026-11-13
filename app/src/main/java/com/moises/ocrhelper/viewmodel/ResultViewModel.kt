package com.moises.ocrhelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moises.ocrhelper.ocr.TextExtractor
import com.moises.ocrhelper.ocr.suggestTitle
import com.moises.ocrhelper.repository.DocumentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Todos los estados posibles de la pantalla de resultado. */
sealed interface ResultUiState {
    data object Loading : ResultUiState
    data class Error(val message: String) : ResultUiState

    /** OCR terminado. El usuario puede corregir título y texto antes de guardar. */
    data class Ready(
        val title: String,
        val text: String,
        val isSaved: Boolean = false
    ) : ResultUiState
}

class ResultViewModel(
    private val imageUri: String,
    private val textExtractor: TextExtractor,
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        recognize()
    }

    private fun recognize() {
        viewModelScope.launch {
            _uiState.value = ResultUiState.Loading
            _uiState.value = try {
                val text = textExtractor.extract(imageUri)
                ResultUiState.Ready(title = suggestTitle(text), text = text)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ResultUiState.Error(e.message ?: "No se pudo procesar la imagen")
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { if (it is ResultUiState.Ready) it.copy(title = newTitle) else it }
    }

    fun onTextChange(newText: String) {
        _uiState.update { if (it is ResultUiState.Ready) it.copy(text = newText) else it }
    }

    fun save() {
        val state = _uiState.value as? ResultUiState.Ready ?: return
        // No se guarda dos veces ni se guardan documentos vacíos
        if (state.isSaved || state.text.isBlank()) return

        viewModelScope.launch {
            val title = state.title.trim().ifEmpty { suggestTitle(state.text) }
            repository.save(title = title, content = state.text)
            _uiState.update { if (it is ResultUiState.Ready) it.copy(isSaved = true) else it }
        }
    }

    companion object {
        fun factory(
            imageUri: String,
            textExtractor: TextExtractor,
            repository: DocumentRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ResultViewModel(imageUri, textExtractor, repository) }
        }
    }
}

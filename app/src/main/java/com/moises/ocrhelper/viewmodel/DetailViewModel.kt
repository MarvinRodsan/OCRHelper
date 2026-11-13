package com.moises.ocrhelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.moises.ocrhelper.data.DocumentEntity
import com.moises.ocrhelper.repository.DocumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    /** null después de cargar significa que el documento no existe. */
    val document: DocumentEntity? = null,
    val isDeleted: Boolean = false
)

class DetailViewModel(
    private val documentId: Long,
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val document = repository.getDocument(documentId)
            _uiState.value = DetailUiState(isLoading = false, document = document)
        }
    }

    fun delete() {
        viewModelScope.launch {
            repository.delete(documentId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    companion object {
        fun factory(documentId: Long, repository: DocumentRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { DetailViewModel(documentId, repository) }
            }
    }
}

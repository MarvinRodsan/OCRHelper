package com.moises.ocrhelper.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moises.ocrhelper.R
import com.moises.ocrhelper.data.DocumentEntity
import com.moises.ocrhelper.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onScan: () -> Unit,
    onImagePicked: (String) -> Unit,
    onOpenDocument: (Long) -> Unit
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    var documentToDelete by remember { mutableStateOf<DocumentEntity?>(null) }

    // Photo Picker del sistema: no necesita permisos
    val pickImage = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        // uri es null si el usuario cierra el selector sin elegir nada
        if (uri != null) onImagePicked(uri.toString())
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onScan, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.scan_document))
                }
                OutlinedButton(
                    onClick = {
                        pickImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.select_image))
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_search)
                            )
                        }
                    }
                }
            )

            Text(
                text = stringResource(R.string.documents_title),
                style = MaterialTheme.typography.titleMedium
            )

            if (documents.isEmpty()) {
                Text(
                    text = stringResource(
                        if (query.isBlank()) R.string.empty_documents else R.string.empty_search
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(documents, key = { it.id }) { document ->
                        DocumentRow(
                            document = document,
                            onClick = { onOpenDocument(document.id) },
                            onDelete = { documentToDelete = document }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    documentToDelete?.let { document ->
        DeleteDialog(
            documentTitle = document.title,
            onConfirm = {
                viewModel.delete(document.id)
                documentToDelete = null
            },
            onDismiss = { documentToDelete = null }
        )
    }
}

@Composable
private fun DocumentRow(
    document: DocumentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        overlineContent = { Text(formatDate(document.createdAt)) },
        headlineContent = {
            Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(
                text = document.content.replace('\n', ' '),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    )
}

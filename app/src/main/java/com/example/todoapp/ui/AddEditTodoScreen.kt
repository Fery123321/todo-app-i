package com.example.todoapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.Todo

/**
 * Screen for adding a new todo or editing an existing one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTodoScreen(
    todo: Todo? = null,
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val isEditing = todo != null
    val snackbarHostState = remember { SnackbarHostState() }

    // Form state
    var title by remember { mutableStateOf(todo?.title ?: "") }
    var description by remember { mutableStateOf(todo?.description ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }

    // Handle successful save
    LaunchedEffect(Unit) {
        // This would be triggered by ViewModel state changes in a real app
        // For now, we'll handle navigation in the save action
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit Todo" else "Add Todo")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null // Clear error when user types
                },
                label = { Text("Title") },
                placeholder = { Text("Enter todo title") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Enter todo description") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Show creation/update info for editing
            if (isEditing && todo != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Created: ${todo.getFormattedCreatedDate()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (todo.updatedAt != todo.createdAt) {
                        Text(
                            text = "Updated: ${todo.getFormattedUpdatedDate()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (validateAndSave(title, description, isEditing, todo, viewModel)) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (isEditing) "Update" else "Save")
                }
            }
        }
    }
}

/**
 * Validates the form and saves the todo if valid.
 * Returns true if save was successful, false otherwise.
 */
private fun validateAndSave(
    title: String,
    description: String,
    isEditing: Boolean,
    existingTodo: Todo?,
    viewModel: TodoViewModel
): Boolean {
    // Trim whitespace
    val trimmedTitle = title.trim()
    val trimmedDescription = description.trim()

    // Validate title
    if (trimmedTitle.isBlank()) {
        return false
    }

    if (isEditing && existingTodo != null) {
        // Update existing todo
        val updatedTodo = existingTodo.copy(
            title = trimmedTitle,
            description = trimmedDescription
        )
        viewModel.onEvent(TodoEvent.UpdateTodo(updatedTodo))
    } else {
        // Add new todo
        viewModel.onEvent(TodoEvent.AddTodo(trimmedTitle, trimmedDescription))
    }

    return true
}
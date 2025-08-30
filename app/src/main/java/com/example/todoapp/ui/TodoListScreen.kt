package com.example.todoapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.todoapp.data.Todo
import com.example.todoapp.ui.theme.TodoAppTheme
import java.time.LocalDateTime

/**
 * Main screen displaying the list of todos with filtering and CRUD operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel,
    onAddTodo: () -> Unit,
    onEditTodo: (Todo) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages
    LaunchedEffect(uiState) {
        if (uiState is TodoUiState.Error) {
            val errorMessage = (uiState as TodoUiState.Error).message
            snackbarHostState.showSnackbar(errorMessage)
            // Clear error after showing
            viewModel.onEvent(TodoEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Todos") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTodo) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TodoUiState.Loading -> {
                    LoadingScreen()
                }
                is TodoUiState.Success -> {
                    val successState = uiState as TodoUiState.Success
                    TodoListContent(
                        todos = successState.todos,
                        currentFilter = successState.filter,
                        onFilterChange = { filter ->
                            viewModel.onEvent(TodoEvent.ChangeFilter(filter))
                        },
                        onTodoToggle = { todo ->
                            viewModel.onEvent(TodoEvent.ToggleTodoCompletion(todo))
                        },
                        onTodoEdit = onEditTodo,
                        onTodoDelete = { todo ->
                            viewModel.onEvent(TodoEvent.DeleteTodo(todo))
                        },
                        onDeleteCompleted = {
                            viewModel.onEvent(TodoEvent.DeleteCompletedTodos)
                        }
                    )
                }
                is TodoUiState.Error -> {
                    ErrorScreen(
                        message = (uiState as TodoUiState.Error).message,
                        onRetry = { viewModel.onEvent(TodoEvent.LoadTodos) }
                    )
                }
            }
        }
    }
}

/**
 * Content of the todo list screen with filtering and list display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoListContent(
    todos: List<Todo>,
    currentFilter: TodoFilter,
    onFilterChange: (TodoFilter) -> Unit,
    onTodoToggle: (Todo) -> Unit,
    onTodoEdit: (Todo) -> Unit,
    onTodoDelete: (Todo) -> Unit,
    onDeleteCompleted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == TodoFilter.ALL,
                onClick = { onFilterChange(TodoFilter.ALL) },
                label = { Text("All (${todos.size})") }
            )

            val completedCount = todos.count { it.isCompleted }
            FilterChip(
                selected = currentFilter == TodoFilter.COMPLETED,
                onClick = { onFilterChange(TodoFilter.COMPLETED) },
                label = { Text("Completed ($completedCount)") }
            )

            val uncompletedCount = todos.count { !it.isCompleted }
            FilterChip(
                selected = currentFilter == TodoFilter.UNCOMPLETED,
                onClick = { onFilterChange(TodoFilter.UNCOMPLETED) },
                label = { Text("Pending ($uncompletedCount)") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (todos.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoItem(
                        todo = todo,
                        onToggle = { onTodoToggle(todo) },
                        onEdit = { onTodoEdit(todo) },
                        onDelete = { onTodoDelete(todo) }
                    )
                }
            }
        }
    }
}

/**
 * Individual todo item card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                )

                if (todo.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Created: ${todo.getFormattedCreatedDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Todo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Todo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Loading screen displayed while data is being fetched.
 */
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error screen displayed when an error occurs.
 */
@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
            androidx.compose.material3.Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Empty state displayed when there are no todos.
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "No todos yet",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Tap the + button to add your first todo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Preview functions for TodoListScreen
@Preview(showBackground = true)
@Composable
fun TodoListScreenPreview() {
    val sampleTodos = listOf(
        Todo(
            id = 1,
            title = "Complete project documentation",
            description = "Write comprehensive documentation for the TODO app",
            isCompleted = false,
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now()
        ),
        Todo(
            id = 2,
            title = "Review code changes",
            description = "Review the latest pull request for bug fixes",
            isCompleted = true,
            createdAt = LocalDateTime.now().minusHours(2),
            updatedAt = LocalDateTime.now().minusHours(1)
        ),
        Todo(
            id = 3,
            title = "Plan next sprint",
            description = "Discuss and plan tasks for the upcoming sprint",
            isCompleted = false,
            createdAt = LocalDateTime.now().minusMinutes(30),
            updatedAt = LocalDateTime.now().minusMinutes(30)
        )
    )

    TodoAppTheme {
        TodoListContent(
            todos = sampleTodos,
            currentFilter = TodoFilter.ALL,
            onFilterChange = {},
            onTodoToggle = {},
            onTodoEdit = {},
            onTodoDelete = {},
            onDeleteCompleted = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListScreenEmptyPreview() {
    TodoAppTheme {
        TodoListContent(
            todos = emptyList(),
            currentFilter = TodoFilter.ALL,
            onFilterChange = {},
            onTodoToggle = {},
            onTodoEdit = {},
            onTodoDelete = {},
            onDeleteCompleted = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
    val sampleTodo = Todo(
        id = 1,
        title = "Sample Todo Item",
        description = "This is a sample todo item for preview",
        isCompleted = false,
        createdAt = LocalDateTime.now().minusHours(1),
        updatedAt = LocalDateTime.now().minusMinutes(30)
    )

    TodoAppTheme {
        TodoItem(
            todo = sampleTodo,
            onToggle = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemCompletedPreview() {
    val sampleTodo = Todo(
        id = 1,
        title = "Completed Todo Item",
        description = "This todo item is completed",
        isCompleted = true,
        createdAt = LocalDateTime.now().minusDays(1),
        updatedAt = LocalDateTime.now()
    )

    TodoAppTheme {
        TodoItem(
            todo = sampleTodo,
            onToggle = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    TodoAppTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    TodoAppTheme {
        ErrorScreen(
            message = "Failed to load todos. Please check your connection and try again.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    TodoAppTheme {
        EmptyState()
    }
}
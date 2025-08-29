package com.example.todoapp.ui

import com.example.todoapp.data.Todo

/**
 * Sealed class representing the different states of the Todo UI.
 * Used by ViewModel to communicate UI state changes to the Compose UI.
 */
sealed class TodoUiState {
    /**
     * Loading state while data is being fetched.
     */
    data object Loading : TodoUiState()

    /**
     * Success state with the list of todos.
     */
    data class Success(
        val todos: List<Todo> = emptyList(),
        val filter: TodoFilter = TodoFilter.ALL
    ) : TodoUiState()

    /**
     * Error state when an operation fails.
     */
    data class Error(val message: String) : TodoUiState()
}

/**
 * Enum representing different filter options for todos.
 */
enum class TodoFilter {
    ALL,
    COMPLETED,
    UNCOMPLETED
}

/**
 * Extension function to filter todos based on the current filter.
 */
fun List<Todo>.filterBy(filter: TodoFilter): List<Todo> {
    return when (filter) {
        TodoFilter.ALL -> this
        TodoFilter.COMPLETED -> this.filter { it.isCompleted }
        TodoFilter.UNCOMPLETED -> this.filter { !it.isCompleted }
    }
}
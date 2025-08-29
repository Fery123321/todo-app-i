package com.example.todoapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Todo-related UI state and business logic.
 * Handles user events, communicates with the repository, and updates UI state.
 */
class TodoViewModel(
    private val repository: TodoRepository
) : ViewModel() {

    // Private mutable state flow for internal updates
    private val _uiState = MutableStateFlow<TodoUiState>(TodoUiState.Loading)

    // Public immutable state flow for UI observation
    val uiState: StateFlow<TodoUiState> = _uiState

    init {
        loadTodos()
    }

    /**
     * Handles user events from the UI.
     */
    fun onEvent(event: TodoEvent) {
        when (event) {
            is TodoEvent.LoadTodos -> loadTodos()
            is TodoEvent.AddTodo -> addTodo(event.title, event.description)
            is TodoEvent.UpdateTodo -> updateTodo(event.todo)
            is TodoEvent.DeleteTodo -> deleteTodo(event.todo)
            is TodoEvent.ToggleTodoCompletion -> toggleTodoCompletion(event.todo)
            is TodoEvent.ChangeFilter -> changeFilter(event.filter)
            is TodoEvent.DeleteCompletedTodos -> deleteCompletedTodos()
            is TodoEvent.ShowError -> showError(event.message)
            is TodoEvent.ClearError -> clearError()
        }
    }

    /**
     * Loads todos from the repository and updates the UI state.
     */
    private fun loadTodos() {
        viewModelScope.launch {
            _uiState.value = TodoUiState.Loading

            repository.getAllTodos()
                .catch { exception ->
                    _uiState.value = TodoUiState.Error(
                        exception.localizedMessage ?: "Failed to load todos"
                    )
                }
                .collectLatest { todos ->
                    val currentState = _uiState.value
                    val currentFilter = if (currentState is TodoUiState.Success) {
                        currentState.filter
                    } else {
                        TodoFilter.ALL
                    }

                    _uiState.value = TodoUiState.Success(
                        todos = todos.filterBy(currentFilter),
                        filter = currentFilter
                    )
                }
        }
    }

    /**
     * Adds a new todo to the repository.
     */
    private fun addTodo(title: String, description: String) {
        if (title.isBlank()) {
            showError("Title cannot be empty")
            return
        }

        viewModelScope.launch {
            repository.insertTodo(title, description)
                .onFailure { exception ->
                    showError(exception.localizedMessage ?: "Failed to add todo")
                }
                // Success case is handled by the Flow collector in loadTodos()
        }
    }

    /**
     * Updates an existing todo in the repository.
     */
    private fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo)
                .onFailure { exception ->
                    showError(exception.localizedMessage ?: "Failed to update todo")
                }
        }
    }

    /**
     * Deletes a todo from the repository.
     */
    private fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
                .onFailure { exception ->
                    showError(exception.localizedMessage ?: "Failed to delete todo")
                }
        }
    }

    /**
     * Toggles the completion status of a todo.
     */
    private fun toggleTodoCompletion(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodoCompletion(todo.id, !todo.isCompleted)
                .onFailure { exception ->
                    showError(exception.localizedMessage ?: "Failed to update todo status")
                }
        }
    }

    /**
     * Changes the current filter and updates the displayed todos.
     */
    private fun changeFilter(filter: TodoFilter) {
        val currentState = _uiState.value
        if (currentState is TodoUiState.Success) {
            _uiState.update { state ->
                if (state is TodoUiState.Success) {
                    state.copy(
                        todos = state.todos.filterBy(filter),
                        filter = filter
                    )
                } else state
            }
        }
    }

    /**
     * Deletes all completed todos from the repository.
     */
    private fun deleteCompletedTodos() {
        viewModelScope.launch {
            repository.deleteCompletedTodos()
                .onFailure { exception ->
                    showError(exception.localizedMessage ?: "Failed to delete completed todos")
                }
        }
    }

    /**
     * Shows an error message in the UI state.
     */
    private fun showError(message: String) {
        _uiState.value = TodoUiState.Error(message)
    }

    /**
     * Clears any current error and reloads the todos.
     */
    private fun clearError() {
        loadTodos()
    }
}
package com.example.todoapp.ui

import com.example.todoapp.data.Todo

/**
 * Sealed class representing user actions/events in the Todo application.
 * Used to communicate user interactions from the UI to the ViewModel.
 */
sealed class TodoEvent {

    /**
     * Event to load all todos.
     */
    data object LoadTodos : TodoEvent()

    /**
     * Event to add a new todo.
     */
    data class AddTodo(
        val title: String,
        val description: String = ""
    ) : TodoEvent()

    /**
     * Event to update an existing todo.
     */
    data class UpdateTodo(val todo: Todo) : TodoEvent()

    /**
     * Event to delete a todo.
     */
    data class DeleteTodo(val todo: Todo) : TodoEvent()

    /**
     * Event to toggle the completion status of a todo.
     */
    data class ToggleTodoCompletion(val todo: Todo) : TodoEvent()

    /**
     * Event to change the current filter.
     */
    data class ChangeFilter(val filter: TodoFilter) : TodoEvent()

    /**
     * Event to delete all completed todos.
     */
    data object DeleteCompletedTodos : TodoEvent()

    /**
     * Event to show an error message.
     */
    data class ShowError(val message: String) : TodoEvent()

    /**
     * Event to clear any current error.
     */
    data object ClearError : TodoEvent()
}
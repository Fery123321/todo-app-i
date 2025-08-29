package com.example.todoapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

/**
 * Repository for Todo data operations.
 * Acts as an abstraction layer between the data source and the ViewModel.
 * Handles business logic and provides a clean interface for data operations.
 */
class TodoRepository(
    private val todoDao: TodoDao
) {

    /**
     * Gets all todos as a Flow that emits whenever data changes.
     */
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    /**
     * Gets only completed todos.
     */
    fun getCompletedTodos(): Flow<List<Todo>> = todoDao.getCompletedTodos()

    /**
     * Gets only uncompleted todos.
     */
    fun getUncompletedTodos(): Flow<List<Todo>> = todoDao.getUncompletedTodos()

    /**
     * Gets a specific todo by ID.
     * Returns a Flow that emits the todo or null if not found.
     */
    fun getTodoById(id: Long): Flow<Todo?> = flow {
        val todo = todoDao.getTodoById(id)
        emit(todo)
    }

    /**
     * Inserts a new todo into the database.
     * Returns the ID of the inserted todo.
     */
    suspend fun insertTodo(title: String, description: String = ""): Result<Long> {
        return try {
            val trimmedTitle = title.trim()
            if (trimmedTitle.isBlank()) {
                return Result.failure(IllegalArgumentException("Title cannot be blank"))
            }

            val todo = Todo(
                title = trimmedTitle,
                description = description.trim(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            val id = todoDao.insertTodo(todo)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing todo.
     */
    suspend fun updateTodo(todo: Todo): Result<Unit> {
        return try {
            val updatedTodo = todo.copy(updatedAt = LocalDateTime.now())
            todoDao.updateTodo(updatedTodo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates the completion status of a todo.
     */
    suspend fun updateTodoCompletion(id: Long, isCompleted: Boolean): Result<Unit> {
        return try {
            todoDao.updateTodoCompletion(id, isCompleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a todo from the database.
     */
    suspend fun deleteTodo(todo: Todo): Result<Unit> {
        return try {
            todoDao.deleteTodo(todo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a todo by ID.
     */
    suspend fun deleteTodoById(id: Long): Result<Unit> {
        return try {
            val todo = todoDao.getTodoById(id)
            if (todo != null) {
                todoDao.deleteTodo(todo)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Todo with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes all completed todos.
     */
    suspend fun deleteCompletedTodos(): Result<Unit> {
        return try {
            todoDao.deleteCompletedTodos()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the total count of todos.
     */
    fun getTodosCount(): Flow<Int> = todoDao.getTodosCount()

    /**
     * Gets the count of completed todos.
     */
    fun getCompletedTodosCount(): Flow<Int> = todoDao.getCompletedTodosCount()

    /**
     * Gets the count of uncompleted todos as a computed Flow.
     */
    fun getUncompletedTodosCount(): Flow<Int> = combine(
        getTodosCount(),
        getCompletedTodosCount()
    ) { total, completed ->
        total - completed
    }
}
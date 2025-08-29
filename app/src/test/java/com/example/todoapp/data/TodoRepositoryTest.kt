package com.example.todoapp.data

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TodoRepositoryTest {

    private lateinit var todoDao: TodoDao
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        todoDao = mockk()
        repository = TodoRepository(todoDao)
    }

    @Test
    fun `getAllTodos returns flow from dao`() = runTest {
        // Given
        val todos = listOf(
            Todo(id = 1, title = "Test Todo 1", isCompleted = false),
            Todo(id = 2, title = "Test Todo 2", isCompleted = true)
        )
        every { todoDao.getAllTodos() } returns flowOf(todos)

        // When & Then
        repository.getAllTodos().test {
            assertEquals(todos, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getCompletedTodos returns flow from dao`() = runTest {
        // Given
        val completedTodos = listOf(
            Todo(id = 2, title = "Completed Todo", isCompleted = true)
        )
        every { todoDao.getCompletedTodos() } returns flowOf(completedTodos)

        // When & Then
        repository.getCompletedTodos().test {
            assertEquals(completedTodos, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getUncompletedTodos returns flow from dao`() = runTest {
        // Given
        val uncompletedTodos = listOf(
            Todo(id = 1, title = "Uncompleted Todo", isCompleted = false)
        )
        every { todoDao.getUncompletedTodos() } returns flowOf(uncompletedTodos)

        // When & Then
        repository.getUncompletedTodos().test {
            assertEquals(uncompletedTodos, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertTodo with valid data returns success`() = runTest {
        // Given
        val title = "New Todo"
        val description = "Description"
        val expectedId = 123L
        coEvery { todoDao.insertTodo(any()) } returns expectedId

        // When
        val result = repository.insertTodo(title, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { todoDao.insertTodo(match { todo ->
            todo.title == title.trim() &&
            todo.description == description.trim() &&
            !todo.isCompleted
        }) }
    }

    @Test
    fun `insertTodo with blank title returns failure`() = runTest {
        // Given
        val title = "   "
        val description = "Description"

        // When
        val result = repository.insertTodo(title, description)

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { todoDao.insertTodo(any()) }
    }

    @Test
    fun `insertTodo with dao error returns failure`() = runTest {
        // Given
        val title = "New Todo"
        val exception = RuntimeException("Database error")
        coEvery { todoDao.insertTodo(any()) } throws exception

        // When
        val result = repository.insertTodo(title)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `updateTodo with valid todo returns success`() = runTest {
        // Given
        val todo = Todo(id = 1, title = "Updated Todo", isCompleted = true)
        coEvery { todoDao.updateTodo(any()) } returns Unit

        // When
        val result = repository.updateTodo(todo)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todoDao.updateTodo(match { updatedTodo ->
            updatedTodo.id == todo.id &&
            updatedTodo.title == todo.title &&
            updatedTodo.description == todo.description &&
            updatedTodo.isCompleted == todo.isCompleted
        }) }
    }

    @Test
    fun `updateTodo with dao error returns failure`() = runTest {
        // Given
        val todo = Todo(id = 1, title = "Updated Todo")
        val exception = RuntimeException("Update failed")
        coEvery { todoDao.updateTodo(any()) } throws exception

        // When
        val result = repository.updateTodo(todo)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `deleteTodo returns success`() = runTest {
        // Given
        val todo = Todo(id = 1, title = "Todo to delete")
        coEvery { todoDao.deleteTodo(any()) } returns Unit

        // When
        val result = repository.deleteTodo(todo)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todoDao.deleteTodo(todo) }
    }

    @Test
    fun `deleteTodoById with existing todo returns success`() = runTest {
        // Given
        val todoId = 1L
        val existingTodo = Todo(id = todoId, title = "Existing Todo")
        coEvery { todoDao.getTodoById(todoId) } returns existingTodo
        coEvery { todoDao.deleteTodo(any()) } returns Unit

        // When
        val result = repository.deleteTodoById(todoId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todoDao.deleteTodo(existingTodo) }
    }

    @Test
    fun `deleteTodoById with non-existing todo returns failure`() = runTest {
        // Given
        val todoId = 999L
        coEvery { todoDao.getTodoById(todoId) } returns null

        // When
        val result = repository.deleteTodoById(todoId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }

    @Test
    fun `updateTodoCompletion returns success`() = runTest {
        // Given
        val todoId = 1L
        val isCompleted = true
        coEvery { todoDao.updateTodoCompletion(any(), any(), any()) } returns Unit

        // When
        val result = repository.updateTodoCompletion(todoId, isCompleted)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todoDao.updateTodoCompletion(todoId, isCompleted, any()) }
    }

    @Test
    fun `deleteCompletedTodos returns success`() = runTest {
        // Given
        coEvery { todoDao.deleteCompletedTodos() } returns Unit

        // When
        val result = repository.deleteCompletedTodos()

        // Then
        assertTrue(result.isSuccess)
        coVerify { todoDao.deleteCompletedTodos() }
    }

    @Test
    fun `getTodosCount returns flow from dao`() = runTest {
        // Given
        val count = 5
        every { todoDao.getTodosCount() } returns flowOf(count)

        // When & Then
        repository.getTodosCount().test {
            assertEquals(count, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getCompletedTodosCount returns flow from dao`() = runTest {
        // Given
        val count = 3
        every { todoDao.getCompletedTodosCount() } returns flowOf(count)

        // When & Then
        repository.getCompletedTodosCount().test {
            assertEquals(count, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getUncompletedTodosCount combines total and completed counts correctly`() = runTest {
        // Given
        val totalCount = 10
        val completedCount = 4
        every { todoDao.getTodosCount() } returns flowOf(totalCount)
        every { todoDao.getCompletedTodosCount() } returns flowOf(completedCount)

        // When & Then
        repository.getUncompletedTodosCount().test {
            assertEquals(totalCount - completedCount, awaitItem())
            awaitComplete()
        }
    }
}
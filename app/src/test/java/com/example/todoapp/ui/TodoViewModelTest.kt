package com.example.todoapp.ui

import app.cash.turbine.test
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Rule

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    private lateinit var repository: TodoRepository
    private lateinit var viewModel: TodoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        // Mock the repository methods that ViewModel uses
        every { repository.getAllTodos() } returns flowOf(emptyList())
        viewModel = TodoViewModel(repository)
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        every { repository.getAllTodos() } returns flowOf(emptyList())

        // When - ViewModel is created

        // Then
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            // The flow will continue with success state due to repository call
        }
    }

    @Test
    fun `loadTodos success updates state with todos`() = runTest {
        // Given
        val todos = listOf(
            Todo(id = 1, title = "Test Todo 1", isCompleted = false),
            Todo(id = 2, title = "Test Todo 2", isCompleted = true)
        )
        every { repository.getAllTodos() } returns flowOf(todos)

        // When
        viewModel.onEvent(TodoEvent.LoadTodos)

        // Then
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            assertEquals(
                TodoUiState.Success(todos = todos, filter = TodoFilter.ALL),
                awaitItem()
            )
        }
    }

    @Test
    fun `loadTodos failure updates state with error`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        every { repository.getAllTodos() } returns kotlinx.coroutines.flow.flow {
            throw exception
        }

        // When
        viewModel.onEvent(TodoEvent.LoadTodos)

        // Then
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            val errorState = awaitItem() as TodoUiState.Error
            assertTrue(errorState.message.contains("Database error"))
        }
    }

    @Test
    fun `addTodo with valid data calls repository and succeeds`() = runTest {
        // Given
        val title = "New Todo"
        val description = "Description"
        val expectedId = 123L
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.insertTodo(title, description) } returns Result.success(expectedId)

        // When
        viewModel.onEvent(TodoEvent.AddTodo(title, description))

        // Then
        coVerify { repository.insertTodo(title, description) }
    }

    @Test
    fun `addTodo with blank title does not call repository`() = runTest {
        // Given
        val title = "   "
        val description = "Description"
        every { repository.getAllTodos() } returns flowOf(emptyList())

        // When
        viewModel.onEvent(TodoEvent.AddTodo(title, description))

        // Then
        coVerify(exactly = 0) { repository.insertTodo(any(), any()) }
    }

    @Test
    fun `addTodo failure shows error state`() = runTest {
        // Given
        val title = "New Todo"
        val exception = RuntimeException("Insert failed")
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.insertTodo(title, "") } returns Result.failure(exception)

        // When
        viewModel.onEvent(TodoEvent.AddTodo(title))

        // Then
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            val errorState = awaitItem() as TodoUiState.Error
            assertTrue(errorState.message.contains("Insert failed"))
        }
    }

    @Test
    fun `updateTodo calls repository with updated todo`() = runTest {
        // Given
        val todo = Todo(id = 1, title = "Original", isCompleted = false)
        val updatedTodo = todo.copy(title = "Updated")
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.updateTodo(updatedTodo) } returns Result.success(Unit)

        // When
        viewModel.onEvent(TodoEvent.UpdateTodo(updatedTodo))

        // Then
        coVerify { repository.updateTodo(updatedTodo) }
    }

    @Test
    fun `deleteTodo calls repository and succeeds`() = runTest {
        // Given
        val todo = Todo(id = 1, title = "Todo to delete")
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.deleteTodo(todo) } returns Result.success(Unit)

        // When
        viewModel.onEvent(TodoEvent.DeleteTodo(todo))

        // Then
        coVerify { repository.deleteTodo(todo) }
    }

    @Test
    fun `toggleTodoCompletion calls repository with correct parameters`() = runTest {
        // Given
        val todo = Todo(id = 1, title = "Test Todo", isCompleted = false)
        every { repository.getAllTodos() } returns flowOf(listOf(todo))
        coEvery { repository.updateTodoCompletion(1L, true) } returns Result.success(Unit)

        // When
        viewModel.onEvent(TodoEvent.ToggleTodoCompletion(todo))

        // Then
        coVerify { repository.updateTodoCompletion(1L, true) }
    }

    @Test
    fun `changeFilter updates state with filtered todos`() = runTest {
        // Given
        val todos = listOf(
            Todo(id = 1, title = "Completed Todo", isCompleted = true),
            Todo(id = 2, title = "Uncompleted Todo", isCompleted = false)
        )
        every { repository.getAllTodos() } returns flowOf(todos)

        // When - Load initial data
        viewModel.onEvent(TodoEvent.LoadTodos)

        // Then - Initial state should have all todos
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            val successState = awaitItem() as TodoUiState.Success
            assertEquals(2, successState.todos.size)
            assertEquals(TodoFilter.ALL, successState.filter)
        }

        // When - Change filter to completed
        viewModel.onEvent(TodoEvent.ChangeFilter(TodoFilter.COMPLETED))

        // Then - Should show only completed todos
        viewModel.uiState.test {
            val successState = awaitItem() as TodoUiState.Success
            assertEquals(1, successState.todos.size)
            assertEquals(TodoFilter.COMPLETED, successState.filter)
            assertTrue(successState.todos.all { it.isCompleted })
        }
    }

    @Test
    fun `deleteCompletedTodos calls repository method`() = runTest {
        // Given
        every { repository.getAllTodos() } returns flowOf(emptyList())
        coEvery { repository.deleteCompletedTodos() } returns Result.success(Unit)

        // When
        viewModel.onEvent(TodoEvent.DeleteCompletedTodos)

        // Then
        coVerify { repository.deleteCompletedTodos() }
    }

    @Test
    fun `showError updates state with error message`() = runTest {
        // Given
        val errorMessage = "Test error"
        every { repository.getAllTodos() } returns flowOf(emptyList())

        // When
        viewModel.onEvent(TodoEvent.ShowError(errorMessage))

        // Then
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            val errorState = awaitItem() as TodoUiState.Error
            assertEquals(errorMessage, errorState.message)
        }
    }

    @Test
    fun `clearError reloads todos and clears error state`() = runTest {
        // Given
        val todos = listOf(Todo(id = 1, title = "Test Todo"))
        every { repository.getAllTodos() } returns flowOf(todos)

        // When - First show error
        viewModel.onEvent(TodoEvent.ShowError("Test error"))

        // Then - Should be in error state
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            assertTrue(awaitItem() is TodoUiState.Error)
        }

        // When - Clear error
        viewModel.onEvent(TodoEvent.ClearError)

        // Then - Should reload and show success state
        viewModel.uiState.test {
            assertEquals(TodoUiState.Loading, awaitItem())
            val successState = awaitItem() as TodoUiState.Success
            assertEquals(todos, successState.todos)
        }
    }
}
package com.example.todoapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Todo entity.
 * Provides database operations for Todo items using Room and Coroutines.
 */
@Dao
interface TodoDao {

    /**
     * Inserts a new Todo item into the database.
     * Uses REPLACE strategy to handle conflicts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    /**
     * Updates an existing Todo item in the database.
     */
    @Update
    suspend fun updateTodo(todo: Todo)

    /**
     * Deletes a Todo item from the database.
     */
    @Delete
    suspend fun deleteTodo(todo: Todo)

    /**
     * Retrieves all Todo items ordered by creation date (newest first).
     * Returns a Flow that emits updates whenever the data changes.
     */
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>

    /**
     * Retrieves only completed Todo items.
     */
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTodos(): Flow<List<Todo>>

    /**
     * Retrieves only uncompleted Todo items.
     */
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getUncompletedTodos(): Flow<List<Todo>>

    /**
     * Retrieves a specific Todo item by its ID.
     */
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): Todo?

    /**
     * Updates the completion status of a Todo item.
     */
    @Query("UPDATE todos SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTodoCompletion(id: Long, isCompleted: Boolean, updatedAt: LocalDateTime = LocalDateTime.now())

    /**
     * Deletes all completed Todo items.
     */
    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun deleteCompletedTodos()

    /**
     * Gets the total count of Todo items.
     */
    @Query("SELECT COUNT(*) FROM todos")
    fun getTodosCount(): Flow<Int>

    /**
     * Gets the count of completed Todo items.
     */
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1")
    fun getCompletedTodosCount(): Flow<Int>
}
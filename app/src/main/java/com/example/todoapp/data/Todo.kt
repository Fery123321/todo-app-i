package com.example.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Todo entity representing a task in the database.
 * Uses Room annotations for database mapping.
 */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Formats the creation date for display purposes.
     */
    fun getFormattedCreatedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        return createdAt.format(formatter)
    }

    /**
     * Formats the updated date for display purposes.
     */
    fun getFormattedUpdatedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        return updatedAt.format(formatter)
    }
}
package com.example.todoapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todoapp.data.Todo
import com.example.todoapp.ui.AddEditTodoScreen
import com.example.todoapp.ui.TodoListScreen
import com.example.todoapp.ui.TodoViewModel

/**
 * Navigation routes for the Todo app.
 */
object TodoRoutes {
    const val TODO_LIST = "todo_list"
    const val ADD_EDIT_TODO = "add_edit_todo"

    /**
     * Creates the route for editing a specific todo.
     */
    fun createEditTodoRoute(todoId: Long): String {
        return "$ADD_EDIT_TODO/$todoId"
    }
}

/**
 * Main navigation host for the Todo application.
 */
@Composable
fun TodoNavHost(viewModel: TodoViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TodoRoutes.TODO_LIST
    ) {
        // Main todo list screen
        composable(TodoRoutes.TODO_LIST) {
            TodoListScreen(
                viewModel = viewModel,
                onAddTodo = {
                    navController.navigate(TodoRoutes.ADD_EDIT_TODO)
                },
                onEditTodo = { todo ->
                    navController.navigate(TodoRoutes.createEditTodoRoute(todo.id))
                }
            )
        }

        // Add new todo screen
        composable(TodoRoutes.ADD_EDIT_TODO) {
            AddEditTodoScreen(
                todo = null, // null means we're adding a new todo
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Edit existing todo screen
        composable(
            route = "${TodoRoutes.ADD_EDIT_TODO}/{todoId}",
            arguments = listOf(
                navArgument("todoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getLong("todoId")

            // In a real app, you'd fetch the todo from the ViewModel
            // For now, we'll pass null and let the screen handle it
            // This is a simplified approach - in production you'd want to
            // pass the todo data through navigation or fetch it in the screen
            AddEditTodoScreen(
                todo = null, // TODO: Pass actual todo data
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
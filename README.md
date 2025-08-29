# Todo App - Android

A modern Android TODO list application built with Jetpack Compose, following MVVM architecture and best practices.

## Features

- ✅ **View Todos**: Display all tasks with completion status
- ✅ **Add Todos**: Create new tasks with title and optional description
- ✅ **Edit Todos**: Modify existing tasks
- ✅ **Delete Todos**: Remove individual tasks or all completed tasks
- ✅ **Mark Complete**: Toggle task completion status
- ✅ **Filter Todos**: View all, completed, or pending tasks
- ✅ **Persistent Storage**: Local data storage using Room database
- ✅ **Modern UI**: Material3 design with Compose
- ✅ **Navigation**: Smooth screen transitions
- ✅ **Error Handling**: Proper error states and user feedback

## Architecture

This app follows the **MVVM (Model-View-ViewModel)** architectural pattern:

```
📱 UI Layer (Compose)
├── TodoListScreen (main list view)
├── AddEditTodoScreen (add/edit form)
└── Navigation setup

🎯 ViewModel Layer
├── TodoViewModel (state management, business logic)
├── TodoUiState (UI state classes)
└── TodoEvent (user actions)

💾 Data Layer
├── TodoEntity (Room entity)
├── TodoDao (database operations)
├── TodoDatabase (Room database)
└── TodoRepository (data abstraction)
```

### Key Technologies

- **Jetpack Compose**: Modern UI toolkit for building native Android interfaces
- **Material3**: Latest Material Design components
- **Room**: SQLite database abstraction layer
- **Navigation Compose**: Single-activity architecture with Compose navigation
- **ViewModel**: Lifecycle-aware state management
- **Kotlin Coroutines**: Asynchronous programming
- **Flow**: Reactive data streams
- **Hilt**: Dependency injection (setup ready)

## Project Structure

```
app/src/main/java/com/example/todoapp/
├── data/
│   ├── Todo.kt                 # Data model entity
│   ├── TodoDao.kt              # Database access object
│   ├── TodoDatabase.kt         # Room database configuration
│   ├── Converters.kt           # Type converters for Room
│   └── TodoRepository.kt       # Data repository layer
├── ui/
│   ├── TodoUiState.kt          # UI state classes
│   ├── TodoEvent.kt            # User event classes
│   ├── TodoViewModel.kt        # ViewModel for business logic
│   ├── TodoListScreen.kt       # Main todo list screen
│   └── AddEditTodoScreen.kt    # Add/edit todo screen
├── navigation/
│   └── TodoNavigation.kt       # Navigation setup
├── MainActivity.kt             # Main activity
└── TodoViewModelFactory.kt     # ViewModel factory
```

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36 (Android 15)

### Running the App

1. Clone the repository
2. Open the project in Android Studio
3. Build and run on device/emulator

### Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Key Implementation Details

### Data Persistence

- Uses Room database for local storage
- Automatic data migration support
- Type converters for LocalDateTime serialization

### State Management

- ViewModel manages UI state using StateFlow
- Event-driven architecture for user interactions
- Proper error handling with Result types

### UI/UX

- Material3 components for modern look
- Responsive design with proper spacing
- Loading states and error handling
- Snackbar notifications for user feedback

### Asynchronous Operations

- Kotlin Coroutines for background tasks
- Flow for reactive data streams
- Proper threading with Dispatchers.IO

## Best Practices Implemented

- ✅ **Separation of Concerns**: Clear MVVM architecture
- ✅ **Dependency Injection**: Manual DI with factory pattern
- ✅ **Error Handling**: Comprehensive error states
- ✅ **Code Documentation**: Well-documented classes and functions
- ✅ **Unit Testing**: Repository and ViewModel test coverage
- ✅ **Material Design**: Consistent Material3 implementation
- ✅ **Accessibility**: Proper content descriptions and semantics

## Future Enhancements

- [ ] Dark/Light theme toggle
- [ ] Todo categories/tags
- [ ] Due dates and reminders
- [ ] Search functionality
- [ ] Cloud synchronization
- [ ] Widget support
- [ ] Backup and restore

## Contributing

1. Follow the existing code style and architecture
2. Add unit tests for new features
3. Update documentation as needed
4. Ensure compatibility with minimum SDK requirements

## License

This project is for educational purposes. Feel free to use and modify as needed.
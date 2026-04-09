Search the TraiScore2 Android project for the following:

$ARGUMENTS

## Search strategy

Base path: `C:\Users\newge\AndroidStudioProjects\TraiScore2\app\src\main\java\com\develop\traiscore\`

### Where to look by type

| Looking for | Search here first |
|-------------|-------------------|
| Screen / UI composable | `presentation/screens/` |
| ViewModel | `presentation/viewmodels/` |
| Reusable UI component | `presentation/components/` |
| Bottom nav / navigation setup | `presentation/navigation/` |
| Route definitions | `presentation/navigation/NavigationRoutes.kt` |
| Main tab switching logic | `presentation/MainScreen.kt` |
| App entry point / NavHost | `presentation/MainActivity.kt` |
| Theme, colors, typography | `presentation/theme/` |
| Room DAO | `data/local/dao/` |
| Room Entity | `data/local/entity/` |
| Room Database | `data/local/TraiScoreDatabase.kt` |
| Repository implementation | `data/repository/` |
| Repository interface | `domain/repository/` |
| Domain model / business logic | `domain/model/` |
| Hilt DI modules | `di/` |
| Firebase data models | `data/firebaseData/` |
| App-level config | `TraiScoreApp.kt` |
| Build config / dependencies | `app/build.gradle.kts` |
| Version catalog | `gradle/libs.versions.toml` |
| String resources | `app/src/main/res/values/strings.xml` |
| Colors (XML) | `app/src/main/res/values/colors.xml` |
| Themes (XML) | `app/src/main/res/values/themes.xml` |

### Key files to check for cross-cutting concerns

- **Dark/light theme toggle**: `presentation/viewmodels/ThemeViewModel.kt`
- **Exercise catalog**: `data/repository/ExerciseRepository.kt`
- **Workout saving**: `data/repository/WorkoutRepository.kt`
- **Session management**: `data/repository/SessionRepository.kt`
- **Stats/graphs data**: `presentation/viewmodels/StatScreenViewModel.kt`
- **Routine creation**: `presentation/viewmodels/RoutineViewModel.kt`

## Instructions

1. Use `Glob` with the specific subdirectory pattern to find files by name
2. Use `Grep` with the search term and appropriate `path` to find code by content
3. Read only the relevant sections of large files (use `offset` + `limit`)
4. Report exact file path and line number for every match found

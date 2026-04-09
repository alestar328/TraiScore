Create a new Compose screen for the TraiScore2 Android project.

## What to create

$ARGUMENTS

## Project structure

Base package: `com.develop.traiscore`
Base path: `C:\Users\newge\AndroidStudioProjects\TraiScore2\app\src\main\java\com\develop\traiscore\`

| Type | Path |
|------|------|
| Screens | `presentation/screens/` |
| ViewModels | `presentation/viewmodels/` |
| Components | `presentation/components/` |
| Navigation routes | `presentation/navigation/NavigationRoutes.kt` |
| MainScreen (tab nav) | `presentation/MainScreen.kt` |
| Theme/Colors | `presentation/theme/` |

## Patterns to follow

### ViewModel
```kotlin
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val repository: SomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
}
```

### Screen signature
All screens that live inside MainScreen must accept `onConfigureTopBar` and `onConfigureFAB`:
```kotlin
@Composable
fun XxxScreen(
    navController: NavController,
    viewModel: XxxViewModel = hiltViewModel(),
    onConfigureTopBar: (left: @Composable () -> Unit, right: @Composable () -> Unit) -> Unit = { _, _ -> },
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onConfigureTopBar({ /* left icon */ }, { /* right icon */ })
        onConfigureFAB(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TraiScoreTheme.dimens.paddingMedium)
    ) { ... }
}
```

### Colors — always use theme tokens
- Background: `MaterialTheme.colorScheme.background` → `#1C1C1D` dark
- Surface: `MaterialTheme.colorScheme.surface` → `#39383B` dark
- Text primary: `MaterialTheme.colorScheme.onBackground`
- Accent/primary: `traiBlue` (`#43f4ff`)
- Secondary accent: `traiOrange`
- Never use `Color.White`, `Color.Black`, or `Color.Yellow` directly

### Navigation
- **Tab-based screens** (ExercisesScreen, StatScreen, etc.): add to `AthleteContent`/`TrainerContent` `when(currentIndex)` in `MainScreen.kt`
- **Stack screens** (pushed on top): add route to `NavigationRoutes.kt` and composable to `AppNavigation` in `MainActivity.kt`

## Steps to follow

1. Read existing similar screen and ViewModel for reference before writing
2. Create ViewModel file in `presentation/viewmodels/`
3. Create Screen file in `presentation/screens/`
4. Add navigation: either to `MainScreen.kt` (tab) or `NavigationRoutes.kt` + `MainActivity.kt` (stack)
5. Register in Hilt if new repository/use case is needed

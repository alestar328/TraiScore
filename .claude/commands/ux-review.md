Review or design UX/UI for the following in TraiScore2:

$ARGUMENTS

## TraiScore design philosophy

TraiScore competes against apps that overwhelm users with features, dashboards, settings, and options. Our answer is the opposite: **do less, but do it perfectly.**

> "A child can use it. An elderly person can use it. Anyone who trains can use it."

Every decision must pass this filter before being implemented.

---

## Core principles

### 1. One action per screen
Each screen has ONE primary purpose. The user should never wonder "what do I do here?".
- ✅ ExercisesScreen → log a workout
- ✅ StatScreen → see your progress
- ✅ RoutineMenuScreen → pick or create a routine
- ❌ Never combine logging + stats + settings in one screen

### 2. Zero learning curve
If a user needs to read instructions or tap more than 2 times to complete a task, the design is wrong.
- Primary actions must be immediately visible (FAB, large button, clear label)
- Destructive actions (delete, exit) require a swipe or confirmation — never a small icon
- Icons always have a label or are universally recognizable

### 3. Organic over mechanical
The app should feel like a training journal, not a spreadsheet.
- Prefer cards and rounded shapes over tables and grids
- Animations are smooth and purposeful (280ms slide, not instant cuts)
- Colors are accent-based: one cyan, one orange — not a rainbow of status colors
- Empty states are encouraging, not technical ("¡Empieza tu primer entreno!" not "No data found")

### 4. The 3-tap rule
Any core action — logging a set, checking stats, finding a routine — must be reachable in 3 taps from the home screen. If it takes more, simplify the navigation or restructure the feature.

### 5. Trust the user's body
TraiScore is for people who train. They know their exercises, their weight, their reps.
- Don't over-validate input. Trust that they know what 200kg means.
- Don't add tutorial overlays, tooltips, or onboarding wizards.
- Don't ask for confirmation on low-stakes actions.

### 6. The base is the feature
What exists is intentional. New features must earn their place by:
- Being used in the first 30 seconds of opening the app, OR
- Replacing 2 or more taps with 1
- If neither applies: don't add it.

---

## What NOT to do (anti-patterns)

| ❌ Avoid | ✅ Instead |
|----------|-----------|
| Settings screens with 10+ options | 2-3 meaningful toggles max |
| Nested navigation deeper than 2 levels | Flatten or use bottom sheet |
| Empty graph with "No data available" | Show encouraging placeholder or hide the graph |
| Multiple FABs or action buttons | One primary action, secondary via swipe or long-press |
| Modal dialogs for simple confirmations | Swipe-to-dismiss, undo snackbar |
| Tabs inside tabs | Rethink the information architecture |
| Feature flags / "Coming soon" placeholders | Don't show what doesn't work |
| Hardcoded text explaining how to use a feature | The UI explains itself |

---

## Visual language

```
Background:   #1C1C1D  (dark, near black — feels like a gym at night)
Surface:      #39383B  (slightly lighter — cards, sheets)
Primary:      #43F4FF  (cyan — the one accent that says "action")
Secondary:    #FFA726  (orange — progress, warmth, energy)
Text:         #FFFFFF  (primary), #96979F (secondary/labels)
Danger:       #FF4444  (delete only — never use red for anything else)
```

Typography: Poppins (headings, labels) + Inter (data, numbers)
Shape: rounded corners everywhere — `16dp` for cards, `12dp` for chips, `8dp` for inputs

---

## Review checklist

Before implementing or approving any UX change, verify:

- [ ] Can a person unfamiliar with fitness apps understand this screen in 5 seconds?
- [ ] Is the primary action obvious without reading anything?
- [ ] Does this screen do exactly one thing?
- [ ] Are there fewer than 3 interactive elements visible at once (excluding nav)?
- [ ] Does the empty state feel encouraging rather than broken?
- [ ] Does it work at font size +2 (accessibility)?
- [ ] Does every animation serve a purpose (not just decoration)?
- [ ] Is this simpler than what the competition does?

---

## Project paths for UX work

| Resource | Path |
|----------|------|
| Screen files | `presentation/screens/` |
| Reusable components | `presentation/components/` |
| Theme & colors | `presentation/theme/Theme.kt` |
| Dimensions | `presentation/theme/Dimens.kt` |
| String resources | `app/src/main/res/values/strings.xml` |
| Drawables / icons | `app/src/main/res/drawable/` |

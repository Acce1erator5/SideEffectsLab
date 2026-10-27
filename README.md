# Side Effects Lab - Jetpack Compose Effect APIs

This repository demonstrates the correct usage of Jetpack Compose Effect APIs and state management techniques across various common Android development scenarios.

---

## Task Summary & Effect API Usage

| Task / Screen | Effect API Chosen | Purpose & Selection Rationale |
| :--- | :--- | :--- |
| **Task 1: Landing Screen** | `rememberUpdatedState` | Keeps long-running coroutines (`LaunchedEffect`) updated with the latest callback reference without cancelling and restarting the timer. |
| **Task 2: Snackbar Screen** | `rememberCoroutineScope` | Launches suspend functions (`SnackbarHostState.showSnackbar`) safely from non-composable user interaction callbacks (`onClick`). |
| **Task 3: Lifecycle Logger** | `DisposableEffect` | Observes Android lifecycle events and guarantees cleanup (`removeObserver`) in `onDispose` when the composable leaves composition. |
| **Task 4: Analytics Counter** | `SideEffect` | Synchronizes Compose state changes with external non-Compose state/analytics objects after every successful recomposition commit. |
| **Task 5: Scroll Optimization** | `derivedStateOf` & `snapshotFlow` | Optimizes performance by reducing recompositions during scrolling (`derivedStateOf`) and converting state changes to reactive flows for one-time side effects (`snapshotFlow`). |
| **Task 6 (Bonus): Network Image** | `produceState` | Bridges asynchronous suspend calls (fake network image fetch) into Compose `State` with automatic cancellation upon key change. |

---

## Detailed Task Breakdown

### Task 1: Landing Screen (`LandingScreen.kt`)
* **API Chosen:** `rememberUpdatedState` (used alongside `LaunchedEffect`)
* **Why it was chosen:**
  `LaunchedEffect` captures values and parameters at the time the coroutine starts. If a parent composable passes a new lambda reference (e.g. capturing an updated `version` state) on recomposition, adding that lambda directly as a key to `LaunchedEffect(onTimeout)` would cause the effect to cancel and restart on every state update. In a splash screen scenario with a `delay(2000)`, restarting the effect on every version update causes the timer to reset constantly and freeze the splash screen.
  
  By wrapping `onTimeout` with `val currentOnTimeout by rememberUpdatedState(onTimeout)`, the coroutine inside `LaunchedEffect(Unit)` retains a stable reference that always points to the latest lambda instance without needing to restart the timer.

---

### Task 2: Snackbar Screen (`SnackbarScreen.kt`)
* **API Chosen:** `rememberCoroutineScope`
* **Why it was chosen:**
  Showing a Snackbar via `SnackbarHostState.showSnackbar()` is a suspending operation. Because `LaunchedEffect` is a `@Composable` function, it can only be invoked at the top level of composable functions during the composition phase—calling it inside event callbacks like `Button(onClick = { ... })` is prohibited by the Compose compiler.
  
  `rememberCoroutineScope()` provides a `CoroutineScope` bound to the lifecycle of `SnackbarScreen` in composition, allowing `scope.launch { ... }` to be called safely inside `onClick` handlers.

---

### Task 3: Lifecycle Logger (`LifecycleLoggerScreen.kt`)
* **API Chosen:** `DisposableEffect`
* **Why it was chosen:**
  `DisposableEffect` is designed for side effects that require cleanup when keys change or when the composable leaves composition. Registering a `LifecycleEventObserver` on a `LifecycleOwner` creates an active subscription.
  
  `DisposableEffect` mandates an `onDispose` block as its final statement. Placing `lifecycleOwner.lifecycle.removeObserver(observer)` inside `onDispose` guarantees that the listener is removed when the screen is removed from the composition, preventing memory leaks and orphaned listeners.

---

### Task 4: Analytics Counter (`AnalyticsScreen.kt`)
* **API Chosen:** `SideEffect`
* **Why it was chosen:**
  `SideEffect` runs immediately after every successful recomposition and commit to the UI tree. It is used to push updates from Compose state to external non-Compose objects (such as an analytics SDK singleton).
  
  During recomposition, Compose may abort or restart execution if states change mid-composition (Attempt Model). `SideEffect` guarantees that the code inside it executes **only** when the composition attempt succeeds and is committed to the tree, preventing duplicate or premature analytics calls during aborted recompositions.

---

### Task 5: Scroll Optimization (`ScrollOptimizationScreen.kt`)
* **APIs Chosen:** `derivedStateOf` & `snapshotFlow`
* **Why they were chosen:**
  1. **`derivedStateOf`:** Reading rapidly changing state like `LazyListState.firstVisibleItemIndex` directly in composition triggers recomposition on every pixel scrolled. Wrapping the calculation in `derivedStateOf { listState.firstVisibleItemIndex > 0 }` creates a derived state that notifies composition **only** when the resulting boolean changes (`false` $\rightarrow$ `true` or `true` $\rightarrow$ `false`), drastically improving scroll performance.
  2. **`snapshotFlow`:** Converts Compose state (`firstVisibleItemIndex`) into a reactive Kotlin `Flow`. Combined with operators like `map`, `distinctUntilChanged`, and `filter`, `snapshotFlow` allows triggering one-time side effects (e.g. logging an analytics event when scrolling past item 0) inside a `LaunchedEffect` without triggering UI recompositions.

---

### Task 6 (Bonus): Network Image Status (`NetworkImageScreen.kt`)
* **API Chosen:** `produceState`
* **Why it was chosen:**
  `produceState` converts non-Compose asynchronous data streams or suspend calls into Compose `State<T>`.
  
  When fetching a network image using `repository.loadNetworkImage(url)`, `produceState` launches a coroutine bound to the composition, sets the initial value to `ImageState.Loading`, executes the suspend function, and updates the value to `ImageState.Success` or `ImageState.Error`. When the `url` key changes, `produceState` automatically cancels the previous network job and restarts the producer block.

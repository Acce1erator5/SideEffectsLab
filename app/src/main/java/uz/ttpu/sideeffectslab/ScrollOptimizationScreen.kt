package uz.ttpu.sideeffectslab

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun ScrollOptimizationScreen() {
    val listState = rememberLazyListState()

    var badRecompositionCount by remember { mutableIntStateOf(0) }
    var goodRecompositionCount by remember { mutableIntStateOf(0) }

    // Variant 1: Direct reading (Unoptimized) - triggers recomposition whenever firstVisibleItemIndex changes
    @SuppressLint("FrequentlyChangingStateReadInComposition")
    val showButtonDirect = listState.firstVisibleItemIndex > 0
    SideEffect { badRecompositionCount++ }

    // Variant 2: Optimized using derivedStateOf - recomposes only when the boolean result changes (true <-> false)
    val showButtonOptimized by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    SideEffect { goodRecompositionCount++ }

    // snapshotFlow to send an analytics event strictly once when scrolling past the first item
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> index > 0 }
            .distinctUntilChanged()
            .filter { isPastFirstItem -> isPastFirstItem }
            .collect {
                println("Logcat: Analytics EVENT - User scrolled past item 0!")
            }
    }

    Column {
        Text("Direct reading value: $showButtonDirect (Recompositions: $badRecompositionCount)")
        Text("derivedStateOf value: $showButtonOptimized (Recompositions: $goodRecompositionCount)")

        if (showButtonOptimized) {
            Button(onClick = { /* scroll to top */ }) {
                Text("Scroll to top")
            }
        }

        LazyColumn(state = listState) {
            items(100) { index ->
                Text("List item #$index")
            }
        }
    }
}

/*
TASK COMMENT:
derivedStateOf vs snapshotFlow:
- derivedStateOf is used inside Composable functions to derive a new Compose State from other rapidly-changing
  states (like scroll position), recalculating only when necessary and minimizing recompositions.
- snapshotFlow is used to convert Compose State into a Kotlin Coroutine Flow, allowing reactive operators
  (like map, filter, distinctUntilChanged, debounce) to be applied in coroutine blocks for side effects like analytics logging.
*/

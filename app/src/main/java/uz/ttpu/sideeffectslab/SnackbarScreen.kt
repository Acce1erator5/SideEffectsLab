package uz.ttpu.sideeffectslab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun SnackbarScreen(onNavigateAway: () -> Unit = {}) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Create a CoroutineScope bound to the lifecycle of this composable in Composition
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    // Launching a coroutine directly from the click handler
                    scope.launch {
                        snackbarHostState.showSnackbar("Hello from CoroutineScope!")
                    }
                }
            ) {
                Text("Show Snackbar")
            }
        }
    }
}

/*
TASK COMMENT:
Why LaunchedEffect does not work in onClick: LaunchedEffect is a Composable function.
It can only be called at the top level of other Composable functions during the composition phase.
onClick is a regular lambda callback executed at click runtime; calling a Composable inside it is prohibited by the compiler.

What a second tap does: If you press the button again before the animation finishes,
the previous snackbar is immediately dismissed and the new one is displayed right away.
*/

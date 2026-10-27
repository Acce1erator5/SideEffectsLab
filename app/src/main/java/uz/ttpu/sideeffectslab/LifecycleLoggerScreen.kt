package uz.ttpu.sideeffectslab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun LifecycleLoggerParent() {
    var isVisible by remember { mutableStateOf(true) }

    Column {
        Switch(checked = isVisible, onCheckedChange = { isVisible = it })
        if (isVisible) {
            LifecycleLoggerScreen()
        }
    }
}

@Composable
fun LifecycleLoggerScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                println("Logcat: Lifecycle ON_START")
            } else if (event == Lifecycle.Event.ON_STOP) {
                println("Logcat: Lifecycle ON_STOP")
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        println("Logcat: Observer Added")

        // onDispose MUST be the final statement of the DisposableEffect block
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            println("Logcat: Observer Removed")
        }
    }
}

/*
TASK COMMENT:
If you temporarily remove the onDispose block, the project will fail to compile with the error:
"DisposableEffect Composable must have an onDispose block as its last statement".
This is Compose's built-in protection mechanism against memory leaks.
*/

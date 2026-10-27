package uz.ttpu.sideeffectslab

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*

// Fake external analytics object
object Analytics {
    var userType: String = "Guest"
}

@Composable
fun AnalyticsScreen() {
    var userTypeState by remember { mutableStateOf("Guest") }
    var recomposeTrigger by remember { mutableIntStateOf(0) }
    val sideEffectCounter = remember { intArrayOf(0) }

    // Read recomposeTrigger during composition to subscribe to updates
    @Suppress("UNUSED_VARIABLE")
    val currentTrigger = recomposeTrigger

    // SideEffect runs AFTER every successful recomposition
    SideEffect {
        Analytics.userType = userTypeState
        sideEffectCounter[0]++
        println("Logcat: SideEffect ran. Counter = ${sideEffectCounter[0]}, UserType = ${Analytics.userType}")
    }

    Column {
        Text("Current state: $userTypeState")
        Button(onClick = { userTypeState = "Premium" }) {
            Text("Make Premium")
        }
        Button(onClick = { recomposeTrigger++ }) {
            Text("Just Recompose")
        }
    }
}

/*
TASK COMMENT:
SideEffect runs after EVERY successful recomposition, even if the target state (userType)
has not changed. This guarantees that an external library is always synchronized with the latest UI.

Attempt Model: During recomposition, Compose may abort execution of a function
and restart it (for example, if a new state arrives midway). Code inside SideEffect
will execute only when the attempt is fully completed and successfully committed to the UI tree.
*/

package uz.ttpu.sideeffectslab

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

@Composable
fun LandingScreenParent() {
    var version by remember { mutableIntStateOf(0) }

    // Every 500 ms, increment the version and create a new lambda
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            version++
        }
    }

    val lambdaWithLatestVersion = { println("Callback executed with version: v$version") }

    // For lab demonstration, you can switch between variant calls:
    // Variant A: Buggy (captures v0)
    // LandingScreenBad(onTimeout = lambdaWithLatestVersion)

    // Variant B: Fixed (calls the latest version)
    LandingScreenGood(onTimeout = lambdaWithLatestVersion)
}

@Composable
fun LandingScreenGood(onTimeout: () -> Unit) {
    // rememberUpdatedState guarantees that we always call the latest version of the lambda
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(true) {
        delay(2000)
        currentOnTimeout() // Will call the latest version (e.g., v3 or v4)
    }
}

@Composable
fun LandingScreenBad(onTimeout: () -> Unit) {
    LaunchedEffect(true) {
        delay(2000)
        onTimeout() // OLD VERSION v0 WILL BE CALLED (since the coroutine captured it at launch)
    }
}

/*
TASK COMMENT:
If you change the key to LaunchedEffect(onTimeout), then every time the 'version' counter changes,
the parent component will pass a new lambda reference. Due to the key change, LaunchedEffect
will be canceled and restarted. As a result, delay(2000) will restart counting from the beginning,
and the timer will never finish — the Splash screen will hang indefinitely.
*/

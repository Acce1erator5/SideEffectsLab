package uz.ttpu.sideeffectslab

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

sealed interface ImageState {
    data object Loading : ImageState
    data object Success : ImageState
    data object Error : ImageState
}

// Fake repository
class ImageRepository {
    suspend fun loadNetworkImage(url: String): ImageState {
        delay(500) // Simulating network latency
        return if (url.endsWith(".png")) ImageState.Success else ImageState.Error
    }
}

@Composable
fun NetworkImageScreen(url: String) {
    val repository = remember { ImageRepository() }

    // Converting suspend call into Compose State
    val imageUiState by produceState<ImageState>(initialValue = ImageState.Loading, key1 = url) {
        // Whenever url changes, this block restarts, resetting to Loading state
        value = ImageState.Loading
        try {
            value = repository.loadNetworkImage(url)
        } finally {
            println("Logcat: Producer for URL $url was cancelled or completed.")
        }
    }

    // Displaying UI based on state using a when expression
    when (imageUiState) {
        is ImageState.Loading -> Text("Loading image...")
        is ImageState.Success -> Text("Image successfully loaded (.png)")
        is ImageState.Error -> Text("Loading error: invalid format")
    }
}

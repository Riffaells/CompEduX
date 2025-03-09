package utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.w3c.dom.Window

@Composable
actual fun getScreenWidth(): Dp {
    // Для WASM/JS можно использовать window.innerWidth
    return (js("window.innerWidth") as Int).dp
}

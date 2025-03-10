package utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
expect fun getScreenWidth(): Dp

@Composable
fun isLargeScreen(): Boolean {
    return getScreenWidth() > 840.dp
}

package utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIScreen

@Composable
actual fun getScreenWidth(): Dp {
    val screenWidth = UIScreen.mainScreen.bounds.size.width
    return (screenWidth / UIScreen.mainScreen.scale).dp
}

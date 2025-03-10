package utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Реализация WindowSizeHelper для Android
 */
class AndroidWindowSizeHelper : WindowSizeHelper {
    @Composable
    override fun getScreenWidth(): Dp {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp.dp
    }

    @Composable
    override fun isLargeScreen(): Boolean {
        return super.isLargeScreen()
    }
}

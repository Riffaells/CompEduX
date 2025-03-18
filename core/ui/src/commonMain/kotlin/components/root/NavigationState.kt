package components.root

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp

data class NavigationState(
    val sideNavAlpha: Float,
    val bottomNavAlpha: Float,
    val sideNavOffsetX: Dp,
    val bottomNavOffsetY: Dp,
    val sideNavPadding: PaddingValues
)

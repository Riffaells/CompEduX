package component.settings.base

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Wrapper component that limits the maximum width of content
 * on large screens to prevent excessive UI stretching.
 *
 * @param maxWidth Maximum content width
 * @param modifier Modifier for the container
 * @param alignment Content alignment within the container
 * @param content Content to be placed inside
 */
@Composable
fun ConstrainedWidthLayout(
    maxWidth: Int = 640,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    content: @Composable (contentWidth: Int) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        val width = maxWidth.toFloat().coerceAtMost(constraints.maxWidth.toFloat())
        val contentWidth = if (constraints.maxWidth > maxWidth) maxWidth else constraints.maxWidth

        content(contentWidth)
    }
}

/**
 * Wrapper component that limits the maximum width of content
 * on large screens using width modifier.
 *
 * @param maxWidth Maximum content width in dp
 * @param modifier Modifier for the container
 * @param alignment Content alignment within the container
 * @param content Content to be placed inside with width modifier
 */
@Composable
fun ConstrainedWidthContainer(
    maxWidth: Int = 640,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    content: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        val contentModifier = if (constraints.maxWidth > maxWidth.dp.value.toInt()) {
            Modifier.width(maxWidth.dp)
        } else {
            Modifier.fillMaxSize()
        }

        content(contentModifier)
    }
}

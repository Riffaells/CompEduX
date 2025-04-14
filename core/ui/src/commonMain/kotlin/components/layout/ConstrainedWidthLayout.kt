package components.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Wrapper component that limits the maximum width of UI content on large screens.
 *
 * This component should be used around settings screens and other content that
 * shouldn't stretch excessively on large displays.
 *
 * @param content Composable function receiving a width-constrained modifier
 * @param maxWidth Maximum content width in dp
 * @param modifier Modifier for the container
 * @param alignment Content alignment within the container
 */
@Composable
fun ConstrainedWidthLayout(
    content: @Composable (contentModifier: Modifier) -> Unit,
    maxWidth: Int = 640,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        val contentModifier = if (maxWidth.dp < this.maxWidth) {
            Modifier.width(maxWidth.dp)
        } else {
            Modifier.fillMaxSize()
        }

        content(contentModifier)
    }
}

package ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.WindowScope

/**
 * Композабл для отображения контента на десктопе с кастомной шапкой окна
 */

@Composable
fun WindowScope.DesktopContent(
    modifier: Modifier = Modifier,
    isSpace: Boolean = false,
    title: String,
    onCloseRequest: () -> Unit,
    onMinimizeRequest: () -> Unit,
    onMaximizeRequest: () -> Unit,
) {

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {

        Column(
            modifier = Modifier.background(Color.Transparent),
        ) {
            TopBar(
                modifier = Modifier,
                onCloseRequest = onCloseRequest,
                onMinimizeRequest = onMinimizeRequest,
                onMaximizeRequest = onMaximizeRequest,
                text = title
            )

        }

    }
}


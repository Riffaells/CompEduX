package ui.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import ui.icon.RIcons


@Composable
fun WindowScope.TopBar(
    text: String,
    modifier: Modifier = Modifier,
    onCloseRequest: () -> Unit,
    onMinimizeRequest: () -> Unit,
    onMaximizeRequest: () -> Unit

) = WindowDraggableArea(
    modifier = modifier.height(36.dp),
) {


    Row(

        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp),
                fontWeight = FontWeight.Bold
            )

        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,

            ) {
            // Кнопка свернуть
            ButtonToolBar(
                onClick = onMinimizeRequest,
                imageVector = RIcons.Minimize,
                contentDescription = "Minimize",
                tint = MaterialTheme.colorScheme.primary
            )

            // Кнопка развернуть
            ButtonToolBar(
                onClick = onMaximizeRequest,
                imageVector = RIcons.Maximize,
                contentDescription = "Maximize",
                tint = MaterialTheme.colorScheme.primary
            )

            // Кнопка закрыть
            ButtonToolBar(
                onClick = onCloseRequest,
                imageVector = RIcons.Close,
                contentDescription = "Close",
                tint = Color.Red
            )
        }
    }

}
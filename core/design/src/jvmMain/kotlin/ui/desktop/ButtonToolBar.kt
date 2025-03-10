package ui.desktop

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ButtonToolBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = ripple(
        bounded = true,
        radius = 48.dp,
        color = LocalContentColor.current.copy(alpha = 0.2f)
    )

    Icon(
        modifier = Modifier
            .padding(5.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple,
                onClickLabel = contentDescription,
                role = Role.Button,
                onClick = onClick,
            ),
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint
    )
}
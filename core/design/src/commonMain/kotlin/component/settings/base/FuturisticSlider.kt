package component.settings.base

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A futuristic slider with glow effects and animations
 *
 * @param value Current value of the slider
 * @param onValueChange Callback for value changes
 * @param valueRange Range of values for the slider
 * @param steps Number of discrete steps
 * @param modifier Modifier for additional styling
 * @param showLabels Whether to show min/mid/max labels
 * @param valueText Text to display for current value (if null, no value is displayed)
 * @param labels List of labels for min, mid, and max positions
 */
@Composable
fun FuturisticSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false,
    valueText: String? = null,
    labels: List<String>? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "sliderGlow")
    val glowAnimation by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Column(modifier = modifier) {
        // Show current value text
        if (valueText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor
                )
            }
        }

        // Show min/mid/max labels
        if (showLabels && labels != null && labels.size >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = labels[0],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = labels[1],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = labels[2],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (showLabels) 2.dp else 0.dp)
        ) {
            // Draw background track
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(4.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            // Calculate progress as a ratio
            val progress = if (valueRange.endInclusive > valueRange.start) {
                (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            } else 0f

            // Draw active track with glow
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(4.dp)
                    .fillMaxWidth(progress)
                    .drawBehind {
                        // Glowing effect
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = glowAnimation),
                                    secondaryColor.copy(alpha = glowAnimation * 0.6f)
                                )
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                            blendMode = BlendMode.Screen
                        )
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor,
                                secondaryColor.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            // The actual slider (transparent track to keep just the thumb)
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )
        }
    }
}

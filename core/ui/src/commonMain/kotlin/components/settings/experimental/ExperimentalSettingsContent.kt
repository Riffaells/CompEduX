package components.settings.experimental

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.*
import component.app.settings.store.SettingsStore
import component.settings.animation.glowingPulse
import component.settings.badge.ExperimentalBadge
import component.settings.base.FuturisticSettingItem
import component.settings.base.FuturisticSlider
import component.settings.headers.SectionHeader
import components.settings.base.SettingsScaffold
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    SettingsScaffold(
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Заголовок
                SectionHeader(
                    title = stringResource(Res.string.experimental_title),
                    icon = Icons.Outlined.Science
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Карточка с описанием
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.experimental_info),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(Res.string.experimental_description),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Секция: Кнопки
                DemoSection(title = stringResource(Res.string.experimental_section_buttons)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.experimental_button_standard))
                        }

                        ElevatedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.experimental_button_icon))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.experimental_button_outlined))
                        }

                        FilledTonalButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.experimental_button_tonal))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.experimental_button_text))
                        }

                        val isSelected = remember { mutableStateOf(false) }
                        Button(
                            onClick = { isSelected.value = !isSelected.value },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected.value)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected.value)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.experimental_button_toggleable))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Секция: Карточки
                DemoSection(title = stringResource(Res.string.experimental_section_cards)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.experimental_card_standard),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.experimental_card_standard_desc),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.experimental_card_elevated),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.experimental_card_elevated_desc),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.experimental_card_outlined),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.experimental_card_outlined_desc),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Секция: Поля ввода
                DemoSection(title = stringResource(Res.string.experimental_section_inputs)) {
                    var text1 by remember { mutableStateOf("") }
                    var text2 by remember { mutableStateOf("") }
                    var text3 by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = text1,
                        onValueChange = { text1 = it },
                        label = { Text(stringResource(Res.string.experimental_input_standard)) },
                        placeholder = { Text(stringResource(Res.string.experimental_input_placeholder)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    TextField(
                        value = text2,
                        onValueChange = { text2 = it },
                        label = { Text(stringResource(Res.string.experimental_input_filled)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    OutlinedTextField(
                        value = text3,
                        onValueChange = { text3 = it },
                        label = { Text(stringResource(Res.string.experimental_input_icons)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { text3 = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(Res.string.experimental_input_clear)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Секция: Переключатели
                DemoSection(title = stringResource(Res.string.experimental_section_controls)) {
                    var switchState by remember { mutableStateOf(true) }
                    var checkboxState by remember { mutableStateOf(true) }
                    var radioState by remember { mutableStateOf(1) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.experimental_control_switch))
                        Switch(
                            checked = switchState,
                            onCheckedChange = { switchState = it }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(Res.string.experimental_control_checkbox))
                        Checkbox(
                            checked = checkboxState,
                            onCheckedChange = { checkboxState = it }
                        )
                    }

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(stringResource(Res.string.experimental_control_radio))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = radioState == 1,
                                onClick = { radioState = 1 }
                            )
                            Text(stringResource(Res.string.experimental_control_radio_option1))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = radioState == 2,
                                onClick = { radioState = 2 }
                            )
                            Text(stringResource(Res.string.experimental_control_radio_option2))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Секция: Слайдеры и индикаторы
                DemoSection(title = stringResource(Res.string.experimental_section_sliders)) {
                    var sliderState by remember { mutableStateOf(0.5f) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.experimental_slider_standard),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Slider(
                            value = sliderState,
                            onValueChange = { sliderState = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = stringResource(Res.string.experimental_slider_value, (sliderState * 100).toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    FuturisticSlider(
                        value = sliderState,
                        onValueChange = { sliderState = it },
                        valueText = "${(sliderState * 100).toInt()}%",
                        showLabels = true,
                        labels = listOf(
                            stringResource(Res.string.experimental_slider_min),
                            stringResource(Res.string.experimental_slider_middle),
                            stringResource(Res.string.experimental_slider_max)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(50.dp),
                                progress = sliderState
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.experimental_progress),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.experimental_progress_infinite),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Секция: Анимированные компоненты
                DemoSection(title = stringResource(Res.string.experimental_section_animations)) {
                    // Пульсирующая кнопка
                    val infiniteTransition = rememberInfiniteTransition()
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Пульсирующая кнопка
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            Text(stringResource(Res.string.experimental_animation_pulse))
                        }

                        // Градиентная кнопка
                        val colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                        val brush = remember { Brush.horizontalGradient(colors) }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(25.dp))
                                .background(brush)
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.experimental_animation_gradient),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Свечение
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .glowingPulse(
                                    baseColor = MaterialTheme.colorScheme.primary,
                                    alpha = 0.2f
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Секция: Собственные компоненты
                DemoSection(title = stringResource(Res.string.experimental_section_custom)) {
                    FuturisticSettingItem(
                        title = stringResource(Res.string.experimental_custom_futuristic),
                        description = stringResource(Res.string.experimental_custom_futuristic_desc),
                        trailingContent = {
                            ExperimentalBadge(tooltipText = stringResource(Res.string.experimental_custom_badge))
                        },
                        modifier = Modifier.padding(8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = stringResource(Res.string.experimental_custom_premium),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(Res.string.experimental_custom_premium_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { },
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                ) {
                                    Text(stringResource(Res.string.experimental_custom_more))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    )
}

@Composable
private fun DemoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Заголовок секции
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // Содержимое секции
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(vertical = 8.dp)
        ) {
            content()
        }
    }
}

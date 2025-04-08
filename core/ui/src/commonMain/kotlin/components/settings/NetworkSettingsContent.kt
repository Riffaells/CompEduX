package components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import component.app.settings.store.SettingsStore
import component.settings.CategoryBlock
import component.settings.ExperimentalBadge
import component.settings.SecondaryCategoryBlock
import component.settings.SettingTextField
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import kotlinx.coroutines.launch
import ui.icon.RIcons

// New feature badge composable
@Composable
fun NewFeatureBadge(
    tooltipText: String,
    titleText: String = "Новая функция",
    icon: ImageVector = RIcons.ExperimentNew
) {
    var tooltipVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(start = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .size(16.dp)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = titleText,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .size(12.dp)
                    .padding(2.dp)
            )
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = tooltipText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            state = rememberTooltipState(),
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable { tooltipVisible = !tooltipVisible }
            )
        }
    }
}

// Reusable futuristic chip component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuturisticFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(16.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.shadow(
            elevation = if (selected) 4.dp else 0.dp,
            shape = RoundedCornerShape(16.dp)
        )
    )
}

// Reusable futuristic option chip component with cleaner design
@Composable
fun FuturisticOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 4.dp else 0.dp,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected)
                MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (selected)
                MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// Reusable futuristic feature card
@Composable
fun FuturisticFeatureCard(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        ),
        modifier = modifier.defaultMinSize(minHeight = 60.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// Reusable futuristic progress bar
@Composable
fun FuturisticProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Progress indicator labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "100%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Background of progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )

        // Filled part of progress bar with gradient and glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(8.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(4.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        )

        // Progress marker
        Box(
            modifier = Modifier
                .offset(x = (-12).dp)
                .fillMaxWidth(progress)
                .padding(end = 1.dp)
                .height(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}

// Reusable futuristic section header
@Composable
fun SectionHeader(
    title: String,
    badge: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(bottom = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )

        if (badge != null) {
            badge()
        }
    }
}

// Reusable futuristic expandable button
@Composable
fun ExpandableButton(
    expanded: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                           else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class,
       androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NetworkSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var serverUrlState by remember { mutableStateOf(TextFieldValue(state.serverUrl)) }
    val coroutineScope = rememberCoroutineScope()

    // State for experimental options
    var enableApiOptimization by remember { mutableStateOf(false) }
    var showBandwidthSettings by remember { mutableStateOf(false) }
    var enableBandwidthLimiter by remember { mutableStateOf(false) }

    // Connection preference state
    var selectedConnectionType by remember { mutableStateOf("Автоматически") }

    // List of connection types
    val connectionTypes = listOf("Автоматически", "Wi-Fi", "Мобильные данные", "Ethernet")

    // Animated alpha for appearance effect
    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "titleAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title without underline
        Text(
            text = stringResource(Res.string.network_settings_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(titleAlpha)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Server settings card using the CategoryBlock component
        CategoryBlock(
            title = "Подключение к серверу",
            icon = Icons.Filled.Cloud,
            isExperimental = false
        ) {
            // Improved text field for server URL
            SettingTextField(
                title = stringResource(Res.string.network_server_url),
                description = stringResource(Res.string.network_server_url_desc),
                value = serverUrlState,
                onValueChange = {
                    serverUrlState = it
                    onAction(SettingsStore.Intent.UpdateServerUrl(it.text))
                },
                placeholder = "https://api.example.com",
                trailingIcon = if (serverUrlState.text.isNotEmpty()) RIcons.Close else RIcons.ContentCopy,
                onTrailingIconClick = {
                    if (serverUrlState.text.isNotEmpty()) {
                        serverUrlState = TextFieldValue("")
                        onAction(SettingsStore.Intent.UpdateServerUrl(""))
                    }
                }
            )

            // Connection type selection with enhanced futuristic chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                SectionHeader(
                    title = "Предпочтительное подключение",
                    badge = {
                        NewFeatureBadge(
                            tooltipText = "Эта функция позволяет оптимизировать подключение в зависимости от типа сети",
                            titleText = "Новая функция"
                        )
                    }
                )

                Text(
                    text = "Выберите тип сети для подключения к серверу",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = spacedBy(8.dp),
                    verticalArrangement = spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 4
                ) {
                    connectionTypes.forEach { type ->
                        FuturisticFilterChip(
                            text = type,
                            selected = selectedConnectionType == type,
                            onClick = { selectedConnectionType = type }
                        )
                    }
                }
            }

            // Experimental API optimization
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "API Оптимизация",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )

                        ExperimentalBadge(
                            tooltipText = "Оптимизация API-запросов может улучшить производительность, но может вызывать нестабильность в некоторых сетях",
                            titleText = "Экспериментальная оптимизация",
                            icon = RIcons.ExperimentBeta
                        )
                    }

                    Text(
                        text = "Оптимизирует запросы к API для улучшения производительности",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = enableApiOptimization,
                    onCheckedChange = { enableApiOptimization = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Button to show additional settings - with enhanced styling
            ExpandableButton(
                expanded = showBandwidthSettings,
                onClick = { showBandwidthSettings = !showBandwidthSettings },
                text = if (showBandwidthSettings)
                      "Скрыть настройки пропускной способности"
                      else "Показать настройки пропускной способности"
            )

            // Expandable section with bandwidth settings
            AnimatedVisibility(
                visible = showBandwidthSettings,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bandwidth limiter with glowing effect for futuristic look
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ограничение пропускной способности",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )

                                Text(
                                    text = "Ограничивает использование сетевого трафика приложением",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = enableBandwidthLimiter,
                                onCheckedChange = { enableBandwidthLimiter = it },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Slider for speed limit if limiter is enabled
                    AnimatedVisibility(visible = enableBandwidthLimiter) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            var sliderPosition by remember { mutableFloatStateOf(5f) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = RIcons.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Лимит скорости: ${sliderPosition.toInt()} Мбит/с",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp)
                            ) {
                                // Fancy slider with custom styling
                                Slider(
                                    value = sliderPosition,
                                    onValueChange = { sliderPosition = it },
                                    valueRange = 1f..20f,
                                    steps = 19,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        activeTickColor = MaterialTheme.colorScheme.primaryContainer,
                                        inactiveTickColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.shadow(2.dp)
                                )
                            }

                            // Speed labels
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Низкая",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                Text(
                                    text = "Средняя",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                Text(
                                    text = "Высокая",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Additional network settings with enhanced styling
        CategoryBlock(
            title = stringResource(Res.string.network_additional_settings),
            icon = RIcons.Settings,
            isExperimental = false
        ) {
            // Protocol selection with enhanced futuristic chips
            var selectedProtocol by remember { mutableStateOf("HTTPS") }
            val protocols = listOf("HTTP", "HTTPS", "FTP", "SFTP")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                SectionHeader(
                    title = "Протокол подключения"
                )

                Text(
                    text = "Выберите протокол для подключения к серверу",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    horizontalArrangement = spacedBy(8.dp),
                    verticalArrangement = spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 4
                ) {
                    protocols.forEach { protocol ->
                        FuturisticOptionChip(
                            text = protocol,
                            selected = selectedProtocol == protocol,
                            onClick = {
                                selectedProtocol = protocol
                                if (protocol == "HTTP") {
                                    coroutineScope.launch {
                                        // Show a warning about unsecure protocol
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Progress bar in modern style with improved futuristic design
            SectionHeader(
                title = "Статус разработки функций",
                badge = {
                    ExperimentalBadge(
                        tooltipText = "Эти функции находятся в активной разработке и скоро будут доступны",
                        titleText = "В разработке",
                        icon = RIcons.ExperimentWIP
                    )
                }
            )

            // Enhanced futuristic progress bar
            FuturisticProgressBar(progress = 0.3f)

            // Planned features with improved presentation
            SectionHeader(
                title = stringResource(Res.string.settings_planned_features),
                badge = {
                    NewFeatureBadge(
                        tooltipText = "Эти функции будут добавлены в ближайших обновлениях",
                        titleText = "Ожидаемые функции",
                        icon = RIcons.ExperimentNew
                    )
                }
            )

            // Planned features section with enhanced futuristic chip design
            val plannedFeatures = listOf(
                "Поддержка прокси-серверов" to RIcons.Lock,
                "Настройка таймаутов соединения" to Icons.Default.Timer,
                "Кэширование запросов" to Icons.Default.SavedSearch
            )

            FlowRow(
                horizontalArrangement = spacedBy(12.dp),
                verticalArrangement = spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                maxItemsInEachRow = 2
            ) {
                plannedFeatures.forEach { (feature, icon) ->
                    FuturisticFeatureCard(
                        text = feature,
                        icon = icon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

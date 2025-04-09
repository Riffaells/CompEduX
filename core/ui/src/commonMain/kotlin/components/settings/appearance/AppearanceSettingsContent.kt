package components.settings.appearance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import component.app.settings.store.SettingsStore
import component.settings.animation.pulsatingGradient
import component.settings.animation.glowingPulse
import component.settings.badge.NewFeatureBadge
import component.settings.badge.PlanningBadge
import component.settings.badge.ExperimentalBadge
import component.settings.base.FuturisticSettingItem
import component.settings.base.FuturisticSlider
import component.settings.headers.FuturisticSectionHeader
import component.settings.headers.SectionHeader
import component.settings.headers.SimpleHeader
import component.settings.input.*
import component.settings.section.*
import component.settings.alert.WarningAlert
import component.settings.alert.AlertSeverity
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import settings.AppearanceSettings

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AppearanceSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with section icon
        SectionHeader(
            title = stringResource(Res.string.appearance_settings_title),
            icon = Icons.Outlined.Palette
        )

        SettingsSection {
            // Черный фон настройка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Черный фон",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Использовать полностью черный фон в темной теме (AMOLED)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.blackBackground,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateBlackBackground(it)) },
                    enabled = state.theme != AppearanceSettings.ThemeOption.THEME_LIGHT
                )
            }

            // Звездное небо настройка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Звездное небо",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Включить анимацию звездного неба на главном экране",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.starrySky,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateStarrySky(it)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Тема приложения
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Тема приложения",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Системная
                Surface(
                    onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_SYSTEM)) },
                    color = Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.theme == AppearanceSettings.ThemeOption.THEME_SYSTEM,
                            onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_SYSTEM)) }
                        )
                        Text(
                            text = "Системная",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Светлая
                Surface(
                    onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_LIGHT)) },
                    color = if (state.theme == AppearanceSettings.ThemeOption.THEME_LIGHT)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.theme == AppearanceSettings.ThemeOption.THEME_LIGHT,
                                onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_LIGHT)) }
                            )
                            Text(
                                text = "Светлая",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }

                // Темная
                Surface(
                    onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_DARK)) },
                    color = if (state.theme == AppearanceSettings.ThemeOption.THEME_DARK)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.theme == AppearanceSettings.ThemeOption.THEME_DARK,
                                onClick = { onAction(SettingsStore.Intent.UpdateTheme(AppearanceSettings.ThemeOption.THEME_DARK)) }
                            )
                            Text(
                                text = "Темная",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Язык приложения
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Язык приложения",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Русский язык
                Surface(
                    onClick = { onAction(SettingsStore.Intent.UpdateLanguage("ru")) },
                    color = if (state.language == "ru")
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.language == "ru",
                            onClick = { onAction(SettingsStore.Intent.UpdateLanguage("ru")) }
                        )
                        Text(
                            text = "Русский",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Английский язык
                Surface(
                    onClick = { onAction(SettingsStore.Intent.UpdateLanguage("en")) },
                    color = if (state.language == "en")
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else
                        Color.Transparent,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.language == "en",
                            onClick = { onAction(SettingsStore.Intent.UpdateLanguage("en")) }
                        )
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Используем футуристический заголовок раздела
            Spacer(modifier = Modifier.height(16.dp))

            FuturisticSectionHeader(
                title = "Тестовый просмотр компонентов",
                icon = Icons.Outlined.Star
            )

            // Предупреждение о тестовом режиме
            var showTestAlert by remember { mutableStateOf(true) }
            WarningAlert(
                title = "Тестовый режим",
                message = "Вы просматриваете демонстрационную версию интерфейса с тестовыми компонентами. " +
                        "Некоторые функции могут работать нестабильно или быть недоступны. " +
                        "Эта секция предназначена только для демонстрации возможностей будущих версий приложения.",
                onDismiss = { showTestAlert = false },
                isVisible = showTestAlert,
                severity = AlertSeverity.INFO,
                icon = Icons.Outlined.Info
            )

            // Разделительная линия перед разделом алертов
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Подраздел с предупреждениями
            SimpleHeader(title = "Компоненты предупреждений")

            // Предупреждение о тестовом режиме (Info)
            var showInfoAlert by remember { mutableStateOf(true) }
            WarningAlert(
                title = "Информационное сообщение",
                message = "Это информационное предупреждение для пользователей. " +
                        "Оно использует цвета primary из темы и выглядит менее угрожающе. " +
                        "Нажмите на крестик, чтобы увидеть плавную анимацию закрытия.",
                onDismiss = { showInfoAlert = false },
                isVisible = showInfoAlert,
                severity = AlertSeverity.INFO,
                icon = Icons.Outlined.Info
            )

            // Предупреждение (Warning)
            var showWarningAlert by remember { mutableStateOf(true) }
            WarningAlert(
                title = "Внимание!",
                message = "Данное действие может привести к потере несохраненных данных. " +
                        "Рекомендуется сохранить все изменения перед продолжением. " +
                        "Нажмите на крестик, чтобы увидеть плавную анимацию закрытия.",
                onDismiss = { showWarningAlert = false },
                isVisible = showWarningAlert,
                severity = AlertSeverity.WARNING
            )

            // Ошибка (Error)
            var showErrorAlert by remember { mutableStateOf(true) }
            WarningAlert(
                title = "Ошибка подключения",
                message = "Не удалось установить соединение с сервером. " +
                        "Проверьте подключение к интернету и повторите попытку позже. " +
                        "Нажмите на крестик, чтобы увидеть плавную анимацию закрытия.",
                onDismiss = { showErrorAlert = false },
                isVisible = showErrorAlert,
                severity = AlertSeverity.ERROR
            )

            // Кнопки для возвращения алертов
            if (!showInfoAlert || !showWarningAlert || !showErrorAlert || !showTestAlert) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            showInfoAlert = true
                            showWarningAlert = true
                            showErrorAlert = true
                            showTestAlert = true
                        }
                    ) {
                        Text("Вернуть скрытые предупреждения")
                    }
                }
            }

            // Разделительная линия после раздела алертов
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Список планируемых функций
            FutureFeaturesList(
                title = "Планируемые функции",
                features = listOf(
                    FutureFeature(
                        title = "Темная тема интерфейса",
                        description = "Настройка цветовой схемы для комфортной работы в ночное время",
                        status = FeatureStatus.COMPLETED
                    ),
                    FutureFeature(
                        title = "Синхронизация настроек",
                        description = "Возможность синхронизировать настройки между устройствами",
                        status = FeatureStatus.IN_PROGRESS
                    ),
                    FutureFeature(
                        title = "Кастомизация интерфейса",
                        description = "Возможность настраивать расположение и видимость элементов интерфейса",
                        status = FeatureStatus.PLANNED
                    ),
                    FutureFeature(
                        title = "Интеграция с облачными сервисами",
                        description = "Поддержка Google Drive, Dropbox и других облачных хранилищ",
                        status = FeatureStatus.FEATURED
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Разделительная линия
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            // Подраздел с бейджами
            SimpleHeader(
                title = "Компоненты-бейджи",
                badge = {
                    PlanningBadge(
                        text = "Демо",
                        planned = true,
                        tooltipText = "Демонстрация бейджей для разных типов функций"
                    )
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Бейджи
                ExperimentalBadge(
                    tooltipText = "Демонстрация экспериментального бейджа"
                )

                NewFeatureBadge(
                    tooltipText = "Демонстрация бейджа новой функции",
                    icon = Icons.Outlined.Favorite
                )

                PlanningBadge(
                    text = "Планируется",
                    planned = true,
                    tooltipText = "Эта функция будет добавлена в ближайшем будущем"
                )
            }

            // Разделительная линия
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Подраздел с базовыми компонентами
            SimpleHeader(title = "Базовые компоненты")

            // FuturisticSettingItem
            FuturisticSettingItem(
                title = "Современный элемент настроек",
                description = "Пример компонента с базовым Switch"
            )

            FuturisticSettingItem(
                title = "Элемент с пользовательским контентом",
                description = "Может содержать любые компоненты справа",
                trailingContent = {
                    val switchColors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                        checkedTrackColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        colors = switchColors
                    )
                }
            )

            // FuturisticSlider
            var sliderValue by remember { mutableStateOf(0.7f) }
            FuturisticSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueText = "${(sliderValue * 100).toInt()}%",
                showLabels = true,
                labels = listOf("Мин", "Среднее", "Макс"),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Разделительная линия
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Подраздел с компонентами ввода
            SimpleHeader(title = "Компоненты ввода")

            // SettingToggle с разными цветами
            SettingToggle(
                title = "Стандартный переключатель",
                description = "Использует цвет primary по умолчанию",
                isChecked = true,
                onCheckedChange = {}
            )

            SettingToggle(
                title = "Переключатель с акцентом",
                description = "Использует цвет secondary для акцента",
                isChecked = true,
                onCheckedChange = {},
                accentColor = MaterialTheme.colorScheme.secondary
            )

            SettingToggle(
                title = "Экспериментальный переключатель",
                description = "Пример с бейджем экспериментальной функции",
                isChecked = false,
                onCheckedChange = {},
                accentColor = MaterialTheme.colorScheme.tertiary,
                isExperimental = true
            )

            SettingToggle(
                title = "Неактивный переключатель",
                description = "Пример отключенного переключателя",
                isChecked = true,
                onCheckedChange = {},
                enabled = false
            )

            // ToggleCard
            ToggleCard(
                title = "Карточка с переключателем",
                description = "Пример карточки с переключателем и описанием",
                icon = Icons.Outlined.Settings,
                checked = true,
                onCheckedChange = {}
            )

            // SettingTextField
            var textFieldState by remember { mutableStateOf(TextFieldValue("Пример текста")) }
            SettingTextField(
                title = "Текстовое поле настройки",
                description = "Описание текстового поля настройки",
                value = textFieldState,
                onValueChange = { textFieldState = it }
            )

            // Разделительная линия
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Демонстрация анимаций
            SimpleHeader(title = "Эффекты анимаций")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Пульсирующий градиент
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .size(60.dp)
                        .pulsatingGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            ),
                            pulseIntensity = 0.1f
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Animation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Эффект свечения
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(60.dp)
                        .glowingPulse(
                            baseColor = MaterialTheme.colorScheme.secondary,
                            alpha = 0.3f
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Дополнительный отступ в конце
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AppearanceSettingsContentPreview() {
    MaterialTheme {
        Surface {
            // Создаем пример состояния для предпросмотра
            val state = SettingsStore.State(
                theme = AppearanceSettings.ThemeOption.THEME_SYSTEM,
                blackBackground = true,
                starrySky = true,
                language = "ru"
            )

            AppearanceSettingsContent(
                state = state,
                onAction = { }
            )
        }
    }
}

// Вспомогательные компоненты для радио-опций
@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        color = if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

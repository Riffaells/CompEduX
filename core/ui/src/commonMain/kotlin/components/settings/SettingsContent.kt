package components.settings

import MultiplatformSettings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.settings.SettingsComponent
import component.app.settings.store.SettingsStore
import component.settings.SettingDropdown
import component.settings.SettingSection
import component.settings.SettingTextField
import component.settings.SettingToggle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import ui.theme.LocalThemeIsDark

/**
 * Композабл для отображения экрана настроек
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(component: SettingsComponent) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()

    // Получаем настройки напрямую для отображения дополнительных опций
    val settings: MultiplatformSettings by rememberInstance()
    val starrySky by settings.starrySkyFlow.collectAsState()
    val blackBackground by settings.blackBackgroundFlow.collectAsState()
    val themeOption by settings.themeFlow.collectAsState()
    val serverUrl by settings.serverUrlFlow.collectAsState()

    // Получаем текущую тему из LocalThemeIsDark
    val isDarkTheme by LocalThemeIsDark.current

    // Локальное состояние для выбора темы
    var selectedThemeOption by remember { mutableStateOf(themeOption) }

    // Локальное состояние для URL сервера
    var serverUrlValue by remember { mutableStateOf(serverUrl) }

    // Состояние для управления drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Состояние для анимации элементов
    var showContent by remember { mutableStateOf(false) }

    // Запускаем анимацию появления контента с небольшой задержкой
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // Создаем drawer с дополнительными опциями
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Дополнительные настройки",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Divider()

                // Анимированные элементы drawer
                val drawerItems = listOf(
                    "Профиль пользователя" to { scope.launch { drawerState.close() } },
                    "Уведомления" to { scope.launch { drawerState.close() } },
                    "Конфиденциальность" to { scope.launch { drawerState.close() } }
                )

                drawerItems.forEachIndexed { index, (text, onClick) ->
                    var showItem by remember { mutableStateOf(false) }

                    // Запускаем анимацию с задержкой для каждого элемента
                    LaunchedEffect(drawerState.isOpen) {
                        if (drawerState.isOpen) {
                            delay(100L * index)
                            showItem = true
                        } else {
                            showItem = false
                        }
                    }

                    AnimatedVisibility(
                        visible = showItem,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        NavigationDrawerItem(
                            label = { Text(text) },
                            selected = false,
                            onClick = { onClick() }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Настройки") },
                    navigationIcon = {
                        IconButton(onClick = { component.onAction(SettingsStore.Intent.Back) }) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Анимация заголовка
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(animationSpec = tween(500)) +
                            slideInHorizontally(
                                initialOffsetX = { -it / 2 },
                                animationSpec = tween(500)
                            ),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Настройки приложения",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // Секция "Внешний вид"
                SettingSection(
                    title = "Внешний вид",
                    showContent = showContent,
                    animationDelay = 600,
                    showTopDivider = false, // Первая секция без верхнего разделителя
                    showBottomDivider = true
                ) {
                    // Настройка темы с использованием компонента выбора из списка
                    val themeOptions = listOf(
                        "Системная" to MultiplatformSettings.ThemeOption.THEME_SYSTEM,
                        "Светлая" to MultiplatformSettings.ThemeOption.THEME_LIGHT,
                        "Темная" to MultiplatformSettings.ThemeOption.THEME_DARK
                    )

                    SettingDropdown(
                        title = "Тема",
                        description = "Выберите тему оформления приложения",
                        options = themeOptions.map { it.first },
                        selectedIndex = themeOptions.indexOfFirst { it.second == selectedThemeOption },
                        onOptionSelected = { index ->
                            val option = themeOptions[index].second
                            selectedThemeOption = option
                            component.onAction(SettingsStore.Intent.UpdateTheme(option))
                        }
                    )

                    // Настройка черного фона (только для темной темы)
                    SettingToggle(
                        title = "Черный фон",
                        description = "Использовать полностью черный фон в темной теме (AMOLED)",
                        isChecked = blackBackground,
                        onCheckedChange = {
                            component.onAction(SettingsStore.Intent.UpdateBlackBackground(it))
                        },
                        enabled = selectedThemeOption != MultiplatformSettings.ThemeOption.THEME_LIGHT
                    )

                    // Настройка звездного неба с использованием компонента переключателя
                    SettingToggle(
                        title = "Звездное небо",
                        description = "Включить анимацию звездного неба на главном экране",
                        isChecked = starrySky,
                        onCheckedChange = { settings.saveStarrySkySettings(it) },
                        isExperimental = true
                    )
                }

                // Секция "Язык и локализация"
                SettingSection(
                    title = "Язык и локализация",
                    showContent = showContent,
                    animationDelay = 700,
                    showTopDivider = false, // Используем только нижний разделитель
                    showBottomDivider = true
                ) {
                    // Настройка языка с использованием компонента выбора из списка
                    val languageOptions = listOf("Русский" to "ru", "Английский" to "en")

                    SettingDropdown(
                        title = "Язык интерфейса",
                        description = "Выберите язык интерфейса приложения",
                        options = languageOptions.map { it.first },
                        selectedIndex = languageOptions.indexOfFirst { it.second == state.language },
                        onOptionSelected = { index ->
                            val newLanguage = languageOptions[index].second
                            component.onAction(SettingsStore.Intent.UpdateLanguage(newLanguage))
                        }
                    )
                }

                // Секция "Настройки сервера"
                SettingSection(
                    title = "Настройки сервера",
                    showContent = showContent,
                    animationDelay = 750,
                    showTopDivider = false,
                    showBottomDivider = true
                ) {
                    // Текстовое поле для ввода URL сервера
                    SettingTextField(
                        title = "URL сервера",
                        description = "Введите адрес сервера для подключения",
                        value = serverUrlValue,
                        onValueChange = { newValue ->
                            serverUrlValue = newValue
                            component.onAction(SettingsStore.Intent.UpdateServerUrl(newValue))
                        }
                    )
                }

                // Секция "Экспериментальные функции"
                SettingSection(
                    title = "Экспериментальные функции",
                    showContent = showContent,
                    animationDelay = 800,
                    showTopDivider = false, // Используем только нижний разделитель
                    showBottomDivider = true
                ) {
                    // Текстовое поле для ввода пользовательского значения
                    SettingTextField(
                        title = "API ключ",
                        description = "Введите ключ API для доступа к экспериментальным функциям",
                        value = "",
                        onValueChange = { /* Сохранение значения */ },
                        isExperimental = true
                    )

                    // Дополнительные экспериментальные настройки
                    SettingToggle(
                        title = "Расширенная аналитика",
                        description = "Включить сбор расширенной аналитики для улучшения приложения",
                        isChecked = false,
                        onCheckedChange = { /* Сохранение значения */ },
                        isExperimental = true
                    )

                    SettingToggle(
                        title = "Новый интерфейс",
                        description = "Включить новый экспериментальный интерфейс (может быть нестабильно)",
                        isChecked = false,
                        onCheckedChange = { /* Сохранение значения */ },
                        isExperimental = true
                    )
                }
            }
        }
    }
}

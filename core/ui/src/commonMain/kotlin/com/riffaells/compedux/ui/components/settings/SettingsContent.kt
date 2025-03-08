package com.riffaells.compedux.ui.components.settings

import MultiplatformSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.settings.SettingsComponent
import component.app.settings.store.SettingsStore
import org.kodein.di.compose.rememberInstance

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
    val themeOption by settings.themeFlow.collectAsState()

    // Локальное состояние для выбора темы
    var selectedThemeOption by remember { mutableStateOf(themeOption) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { component.onAction(SettingsStore.Intent.Back) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Настройки приложения",
                style = MaterialTheme.typography.headlineSmall
            )

            HorizontalDivider()

            // Настройка темы
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Тема", style = MaterialTheme.typography.titleMedium)

                // Радио-кнопки для выбора темы
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedThemeOption == MultiplatformSettings.ThemeOption.THEME_SYSTEM,
                        onClick = {
                            selectedThemeOption = MultiplatformSettings.ThemeOption.THEME_SYSTEM
                            settings.saveThemeSettings(MultiplatformSettings.ThemeOption.THEME_SYSTEM)
                        }
                    )
                    Text("Системная", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedThemeOption == MultiplatformSettings.ThemeOption.THEME_LIGHT,
                        onClick = {
                            selectedThemeOption = MultiplatformSettings.ThemeOption.THEME_LIGHT
                            settings.saveThemeSettings(MultiplatformSettings.ThemeOption.THEME_LIGHT)
                            component.onAction(SettingsStore.Intent.UpdateTheme(false))
                        }
                    )
                    Text("Светлая", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedThemeOption == MultiplatformSettings.ThemeOption.THEME_DARK,
                        onClick = {
                            selectedThemeOption = MultiplatformSettings.ThemeOption.THEME_DARK
                            settings.saveThemeSettings(MultiplatformSettings.ThemeOption.THEME_DARK)
                            component.onAction(SettingsStore.Intent.UpdateTheme(true))
                        }
                    )
                    Text("Темная", modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider()

            // Выбор языка
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Язык: ${if (state.language == "en") "Английский" else "Русский"}")
                Button(
                    onClick = {
                        val newLanguage = if (state.language == "en") "ru" else "en"
                        component.onAction(SettingsStore.Intent.UpdateLanguage(newLanguage))
                    }
                ) {
                    Text("Изменить")
                }
            }

            HorizontalDivider()

            // Настройка звездного неба
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Звездное небо")
                Switch(
                    checked = starrySky,
                    onCheckedChange = {
                        settings.saveStarrySkySettings(it)
                    }
                )
            }
        }
    }
}

package components.settings.base

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import component.app.settings.SettingsComponent

/**
 * Модель категории настроек
 *
 * @param title Заголовок категории
 * @param description Краткое описание категории
 * @param icon Иконка категории
 * @param category Тип категории настроек
 * @param accentColor Дополнительный цвет для акцентирования категории (опционально)
 * @param isNew Флаг, указывающий что категория содержит новый функционал
 * @param isExperimental Флаг, указывающий что категория является экспериментальной
 */
data class SettingCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: SettingsComponent.SettingsCategory,
    val accentColor: Color? = null,
    val isNew: Boolean = false,
    val isExperimental: Boolean = false
)

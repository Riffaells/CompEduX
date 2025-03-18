package components.settings

import androidx.compose.ui.graphics.vector.ImageVector
import component.app.settings.SettingsComponent

/**
 * Модель категории настроек
 */
data class SettingCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: SettingsComponent.SettingsCategory
)


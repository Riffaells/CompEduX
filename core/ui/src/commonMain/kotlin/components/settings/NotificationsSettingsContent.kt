package components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import component.app.settings.store.SettingsStore
import component.settings.CategoryBlock
import component.settings.ExperimentalBadge
import component.settings.SecondaryCategoryBlock
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NotificationsSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Состояния переключателей
    var enableNotifications by remember { mutableStateOf(true) }
    var enableSound by remember { mutableStateOf(true) }
    var enableVibration by remember { mutableStateOf(true) }
    var enableBanner by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(Res.string.settings_category_notifications),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Основные настройки уведомлений
        CategoryBlock(
            title = "Основные настройки",
            icon = Icons.Default.Notifications
        ) {
            // Включение/отключение уведомлений
            NotificationSettingItem(
                title = "Включить уведомления",
                description = "Все системные уведомления приложения",
                icon = Icons.Default.Notifications,
                checked = enableNotifications,
                onCheckedChange = { enableNotifications = it }
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Настройки звука
            NotificationSettingItem(
                title = "Звук",
                description = "Воспроизводить звук при уведомлении",
                icon = Icons.Default.VolumeUp,
                checked = enableSound,
                onCheckedChange = { enableSound = it },
                enabled = enableNotifications
            )

            // Настройки вибрации
            NotificationSettingItem(
                title = "Вибрация",
                description = "Вибрировать при уведомлении",
                icon = Icons.Default.Vibration,
                checked = enableVibration,
                onCheckedChange = { enableVibration = it },
                enabled = enableNotifications
            )

            // Баннеры
            NotificationSettingItem(
                title = "Баннеры",
                description = "Показывать всплывающие уведомления",
                icon = Icons.Default.ViewCarousel,
                checked = enableBanner,
                onCheckedChange = { enableBanner = it },
                enabled = enableNotifications
            )
        }

        // Категории уведомлений
        SecondaryCategoryBlock(
            title = "Категории уведомлений",
            icon = Icons.Filled.Category
        ) {
            // Список категорий уведомлений
            val notificationCategories = listOf(
                Triple("Обновления приложения", "Оповещения о новых версиях", Icons.Default.Update),
                Triple("Учебные материалы", "Уведомления о новых материалах", Icons.Default.MenuBook),
                Triple("Сообщения", "Уведомления о новых сообщениях", Icons.Default.Message),
                Triple("Активность", "Уведомления об активности аккаунта", Icons.Default.WatchLater)
            )

            notificationCategories.forEach { (title, desc, icon) ->
                var enabled by remember { mutableStateOf(true) }

                NotificationSettingItem(
                    title = title,
                    description = desc,
                    icon = icon,
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    enabled = enableNotifications
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }

        // Экспериментальные уведомления
        SecondaryCategoryBlock(
            title = "Каналы уведомлений",
            icon = Icons.Filled.Settings,
            isExperimental = true
        ) {
            Text(
                text = "Эта функция находится в разработке и будет доступна в будущих версиях",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* Действие */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Настроить каналы уведомлений")
            }
        }
    }
}

@Composable
private fun NotificationSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

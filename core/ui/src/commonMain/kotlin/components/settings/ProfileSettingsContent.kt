package components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import component.app.settings.store.SettingsStore
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProfileSettingsContent(
    state: SettingsStore.State,
    onAction: (SettingsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var usernameState by remember { mutableStateOf(TextFieldValue(state.username)) }
    var emailState by remember { mutableStateOf(TextFieldValue(state.email)) }
    var statusState by remember { mutableStateOf(TextFieldValue(state.status)) }
    var avatarUrlState by remember { mutableStateOf(TextFieldValue(state.avatarUrl)) }

    // Обновляем локальное состояние при изменении глобального
    LaunchedEffect(state.username) { usernameState = TextFieldValue(state.username) }
    LaunchedEffect(state.email) { emailState = TextFieldValue(state.email) }
    LaunchedEffect(state.status) { statusState = TextFieldValue(state.status) }
    LaunchedEffect(state.avatarUrl) { avatarUrlState = TextFieldValue(state.avatarUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(Res.string.profile_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Аватар
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (state.avatarUrl.isNotEmpty()) {

            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = stringResource(Res.string.profile_avatar_url),
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // URL аватара
        OutlinedTextField(
            value = avatarUrlState,
            onValueChange = {
                avatarUrlState = it
                onAction(SettingsStore.Intent.UpdateAvatarUrl(it.text))
            },
            label = { Text(stringResource(Res.string.profile_avatar_url)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Имя пользователя
        OutlinedTextField(
            value = usernameState,
            onValueChange = {
                usernameState = it
                onAction(SettingsStore.Intent.UpdateUsername(it.text))
            },
            label = { Text(stringResource(Res.string.profile_username)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Email
        OutlinedTextField(
            value = emailState,
            onValueChange = {
                emailState = it
                onAction(SettingsStore.Intent.UpdateEmail(it.text))
            },
            label = { Text(stringResource(Res.string.profile_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = stringResource(Res.string.profile_email)
                )
            }
        )

        // Статус
        OutlinedTextField(
            value = statusState,
            onValueChange = {
                statusState = it
                onAction(SettingsStore.Intent.UpdateStatus(it.text))
            },
            label = { Text(stringResource(Res.string.profile_status)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Настройки приватности
        SettingsSwitchItem(
            title = stringResource(Res.string.profile_public),
            description = stringResource(Res.string.profile_public_desc),
            icon = Icons.Default.Lock,
            checked = state.isProfilePublic,
            onCheckedChange = { onAction(SettingsStore.Intent.UpdateProfilePublic(it)) }
        )

        // Настройки уведомлений
        SettingsSwitchItem(
            title = stringResource(Res.string.profile_notifications),
            description = stringResource(Res.string.profile_notifications_desc),
            icon = Icons.Default.Notifications,
            checked = state.enableProfileNotifications,
            onCheckedChange = { onAction(SettingsStore.Intent.UpdateProfileNotifications(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка сброса данных профиля
        Button(
            onClick = {
                scope.launch {
                    onAction(SettingsStore.Intent.ClearProfileData)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(Res.string.profile_clear_data))
        }

        // Индикатор заполненности профиля
        LinearProgressIndicator(
            progress = when {
                state.isProfileComplete -> 1f
                state.username.isNotEmpty() && state.email.isEmpty() -> 0.5f
                state.username.isEmpty() && state.email.isNotEmpty() -> 0.5f
                state.username.isNotEmpty() || state.email.isNotEmpty() -> 0.3f
                else -> 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Text(
            text = if (state.isProfileComplete)
                stringResource(Res.string.profile_complete)
            else
                stringResource(Res.string.profile_incomplete),
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.isProfileComplete)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

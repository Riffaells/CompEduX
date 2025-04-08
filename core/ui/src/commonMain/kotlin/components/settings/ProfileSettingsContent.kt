package components.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.*
import component.app.settings.store.SettingsStore
import component.settings.SettingTextField
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
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        // Аватар
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(130.dp)
            ) {
                if (state.avatarUrl.isNotEmpty()) {
                    // Image будет здесь
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(Res.string.profile_avatar_url),
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // URL аватара
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.profile_personal_info),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                // URL аватара
                SettingTextField(
                    title = stringResource(Res.string.profile_avatar_url),
                    description = stringResource(Res.string.profile_avatar_url_desc, ""),
                    value = avatarUrlState,
                    onValueChange = {
                        avatarUrlState = it
                        onAction(SettingsStore.Intent.UpdateAvatarUrl(it.text))
                    },
                    placeholder = "https://example.com/avatar.jpg",
                    trailingIcon = Icons.Default.Link
                )

                // Имя пользователя
                SettingTextField(
                    title = stringResource(Res.string.profile_username),
                    description = stringResource(Res.string.profile_username_desc, ""),
                    value = usernameState,
                    onValueChange = {
                        usernameState = it
                        onAction(SettingsStore.Intent.UpdateUsername(it.text))
                    },
                    placeholder = "username",
                    trailingIcon = Icons.Default.Person
                )

                // Email
                SettingTextField(
                    title = stringResource(Res.string.profile_email),
                    description = stringResource(Res.string.profile_email_desc, ""),
                    value = emailState,
                    onValueChange = {
                        emailState = it
                        onAction(SettingsStore.Intent.UpdateEmail(it.text))
                    },
                    placeholder = "example@mail.com",
                    trailingIcon = Icons.Default.Email
                )

                // Статус
                SettingTextField(
                    title = stringResource(Res.string.profile_status),
                    description = stringResource(Res.string.profile_status_desc, ""),
                    value = statusState,
                    onValueChange = {
                        statusState = it
                        onAction(SettingsStore.Intent.UpdateStatus(it.text))
                    },
                    placeholder = stringResource(Res.string.profile_status_placeholder),
                    trailingIcon = Icons.Default.Info
                )
            }
        }

        // Настройки приватности
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.profile_privacy_settings),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                // Настройки приватности
                ProfileSwitchItem(
                    title = stringResource(Res.string.profile_public),
                    description = stringResource(Res.string.profile_public_desc),
                    icon = Icons.Default.Lock,
                    checked = state.isProfilePublic,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateProfilePublic(it)) }
                )

                // Настройки уведомлений
                ProfileSwitchItem(
                    title = stringResource(Res.string.profile_notifications),
                    description = stringResource(Res.string.profile_notifications_desc),
                    icon = Icons.Default.Notifications,
                    checked = state.enableProfileNotifications,
                    onCheckedChange = { onAction(SettingsStore.Intent.UpdateProfileNotifications(it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Индикатор заполненности профиля
        val progressBarColor = if (state.isProfileComplete)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.tertiary

        val animatedProgress by animateFloatAsState(
            targetValue = when {
                state.isProfileComplete -> 1f
                state.username.isNotEmpty() && state.email.isEmpty() -> 0.5f
                state.username.isEmpty() && state.email.isNotEmpty() -> 0.5f
                state.username.isNotEmpty() || state.email.isNotEmpty() -> 0.3f
                else -> 0f
            },
            animationSpec = tween(1000)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (state.isProfileComplete)
                        stringResource(Res.string.profile_complete)
                    else
                        stringResource(Res.string.profile_incomplete),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (state.isProfileComplete)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .height(8.dp),
                    color = progressBarColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Кнопка сброса данных профиля
                ElevatedButton(
                    onClick = {
                        scope.launch {
                            onAction(SettingsStore.Intent.ClearProfileData)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.profile_clear_data))
                }
            }
        }
    }
}

@Composable
private fun ProfileSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (checked)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        Color.Transparent

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

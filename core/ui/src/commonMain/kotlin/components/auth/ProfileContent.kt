package components.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import component.app.auth.ProfileComponent
import component.app.auth.store.ProfileStore
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(component: ProfileComponent) {
    val state by component.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // User data state
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var linkedinUrl by remember { mutableStateOf("") }
    var twitterUrl by remember { mutableStateOf("") }

    // Настройки
    var theme by remember { mutableStateOf("light") }
    var fontSize by remember { mutableStateOf("medium") }
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var beveragePreference by remember { mutableStateOf("none") }
    var breakReminder by remember { mutableStateOf(true) }
    var breakIntervalMinutes by remember { mutableStateOf(60) }

    // Password change state
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Tabs
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Профиль", "Социальные сети", "Настройки", "Безопасность")

    // Initialize data from state
    LaunchedEffect(state) {
        username = state.username
        email = state.email
        firstName = state.firstName ?: ""
        lastName = state.lastName ?: ""
        bio = state.bio ?: ""
        location = state.location ?: ""
        website = state.website ?: ""
        githubUrl = state.githubUrl ?: ""
        linkedinUrl = state.linkedinUrl ?: ""
        twitterUrl = state.twitterUrl ?: ""
        theme = state.theme
        fontSize = state.fontSize
        emailNotifications = state.emailNotifications
        pushNotifications = state.pushNotifications
        beveragePreference = state.beveragePreference
        breakReminder = state.breakReminder
        breakIntervalMinutes = state.breakIntervalMinutes
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with Avatar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.avatarUrl?.let { avatarUrl ->
                        // TODO: Реализовать загрузку аватара из URL
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            // Placeholder for avatar
                            Icon(
                                imageVector = RIcons.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } ?: run {
                        // Default icon if no avatar
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = RIcons.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Text(
                        text = "Твой профиль",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Настрой всё под себя",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> { // Профиль
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    component.accept(ProfileStore.Intent.UpdateUsername(it))
                                },
                                label = { Text("Имя пользователя") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { },
                                label = { Text("Электронная почта") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                readOnly = true,
                                enabled = false
                            )

                            OutlinedTextField(
                                value = firstName,
                                onValueChange = {
                                    firstName = it
                                    component.accept(ProfileStore.Intent.UpdateFirstName(it))
                                },
                                label = { Text("Имя") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = lastName,
                                onValueChange = {
                                    lastName = it
                                    component.accept(ProfileStore.Intent.UpdateLastName(it))
                                },
                                label = { Text("Фамилия") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = bio,
                                onValueChange = {
                                    bio = it
                                    component.accept(ProfileStore.Intent.UpdateBio(it))
                                },
                                label = { Text("О себе") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                minLines = 3,
                                maxLines = 5
                            )

                            OutlinedTextField(
                                value = location,
                                onValueChange = {
                                    location = it
                                    component.accept(ProfileStore.Intent.UpdateLocation(it))
                                },
                                label = { Text("Местоположение") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }
                    }
                    1 -> { // Социальные сети
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = website,
                                onValueChange = {
                                    website = it
                                    component.accept(ProfileStore.Intent.UpdateWebsite(it))
                                },
                                label = { Text("Личный сайт") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = githubUrl,
                                onValueChange = {
                                    githubUrl = it
                                    component.accept(ProfileStore.Intent.UpdateGithubUrl(it))
                                },
                                label = { Text("GitHub") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = linkedinUrl,
                                onValueChange = {
                                    linkedinUrl = it
                                    component.accept(ProfileStore.Intent.UpdateLinkedinUrl(it))
                                },
                                label = { Text("LinkedIn") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Work,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = twitterUrl,
                                onValueChange = {
                                    twitterUrl = it
                                    component.accept(ProfileStore.Intent.UpdateTwitterUrl(it))
                                },
                                label = { Text("Twitter") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = RIcons.Chat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }
                    }
                    2 -> { // Настройки
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Theme
                            ListItem(
                                headlineContent = { Text("Тема оформления") },
                                leadingContent = {
                                    Icon(
                                        imageVector = RIcons.DarkMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Row {
                                        RadioButton(
                                            selected = theme == "light",
                                            onClick = {
                                                theme = "light"
                                                component.accept(ProfileStore.Intent.UpdateTheme("light"))
                                            }
                                        )
                                        Text("Светлая", modifier = Modifier.padding(start = 8.dp, end = 16.dp))

                                        RadioButton(
                                            selected = theme == "dark",
                                            onClick = {
                                                theme = "dark"
                                                component.accept(ProfileStore.Intent.UpdateTheme("dark"))
                                            }
                                        )
                                        Text("Тёмная", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            )

                            Divider()

                            // Font Size
                            ListItem(
                                headlineContent = { Text("Размер шрифта") },
                                leadingContent = {
                                    Icon(
                                        imageVector = RIcons.FormatSize,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    DropdownMenu(
                                        expanded = false, // TODO: Implement dropdown logic
                                        onDismissRequest = { /* TODO */ }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Маленький") },
                                            onClick = {
                                                fontSize = "small"
                                                component.accept(ProfileStore.Intent.UpdateFontSize("small"))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Средний") },
                                            onClick = {
                                                fontSize = "medium"
                                                component.accept(ProfileStore.Intent.UpdateFontSize("medium"))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Большой") },
                                            onClick = {
                                                fontSize = "large"
                                                component.accept(ProfileStore.Intent.UpdateFontSize("large"))
                                            }
                                        )
                                    }

                                    Text(
                                        when(fontSize) {
                                            "small" -> "Маленький"
                                            "medium" -> "Средний"
                                            "large" -> "Большой"
                                            else -> "Средний"
                                        }
                                    )
                                }
                            )

                            Divider()

                            // Email Notifications
                            ListItem(
                                headlineContent = { Text("Уведомления на почту") },
                                leadingContent = {
                                    Icon(
                                        imageVector = RIcons.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = emailNotifications,
                                        onCheckedChange = {
                                            emailNotifications = it
                                            component.accept(ProfileStore.Intent.UpdateEmailNotifications(it))
                                        }
                                    )
                                }
                            )

                            // Push Notifications
                            ListItem(
                                headlineContent = { Text("Push-уведомления") },
                                leadingContent = {
                                    Icon(
                                        imageVector = RIcons.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = pushNotifications,
                                        onCheckedChange = {
                                            pushNotifications = it
                                            component.accept(ProfileStore.Intent.UpdatePushNotifications(it))
                                        }
                                    )
                                }
                            )

                            Divider()

                            // Break Reminder
                            ListItem(
                                headlineContent = { Text("Напоминания о перерывах") },
                                leadingContent = {
                                    Icon(
                                        imageVector = RIcons.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    Switch(
                                        checked = breakReminder,
                                        onCheckedChange = {
                                            breakReminder = it
                                            component.accept(ProfileStore.Intent.UpdateBreakReminder(it))
                                        }
                                    )
                                }
                            )

                            // Break Interval
                            if (breakReminder) {
                                OutlinedTextField(
                                    value = breakIntervalMinutes.toString(),
                                    onValueChange = {
                                        try {
                                            val minutes = it.toInt()
                                            breakIntervalMinutes = minutes
                                            component.accept(ProfileStore.Intent.UpdateBreakInterval(minutes))
                                        } catch (e: NumberFormatException) {
                                            // Ignore invalid input
                                        }
                                    },
                                    label = { Text("Интервал перерывов (минуты)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = RIcons.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )
                            }

                            Divider()

                            // Beverage Preference
                            ListItem(
                                headlineContent = { Text("Любимый напиток") },
                                leadingContent = {
                                    Icon(
                                        imageVector = RIcons.LocalCafe,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingContent = {
                                    DropdownMenu(
                                        expanded = false, // TODO: Implement dropdown logic
                                        onDismissRequest = { /* TODO */ }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Кофе") },
                                            onClick = {
                                                beveragePreference = "coffee"
                                                component.accept(ProfileStore.Intent.UpdateBeveragePreference("coffee"))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Чай") },
                                            onClick = {
                                                beveragePreference = "tea"
                                                component.accept(ProfileStore.Intent.UpdateBeveragePreference("tea"))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Вода") },
                                            onClick = {
                                                beveragePreference = "water"
                                                component.accept(ProfileStore.Intent.UpdateBeveragePreference("water"))
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Не выбрано") },
                                            onClick = {
                                                beveragePreference = "none"
                                                component.accept(ProfileStore.Intent.UpdateBeveragePreference("none"))
                                            }
                                        )
                                    }

                                    Text(
                                        when(beveragePreference) {
                                            "coffee" -> "Кофе"
                                            "tea" -> "Чай"
                                            "water" -> "Вода"
                                            else -> "Не выбрано"
                                        }
                                    )
                                }
                            )
                        }
                    }
                    3 -> { // Безопасность (пароль)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Поменять пароль",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                OutlinedTextField(
                                    value = currentPassword,
                                    onValueChange = { currentPassword = it },
                                    label = { Text("Текущий пароль") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = RIcons.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                            Icon(
                                                if (currentPasswordVisible) RIcons.VisibilityOff else RIcons.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Новый супер-пароль") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = RIcons.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                            Icon(
                                                if (newPasswordVisible) RIcons.VisibilityOff else RIcons.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Повтори пароль ещё разок") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = RIcons.Lock,
                                            contentDescription = null,
                                            tint = if (newPassword != confirmPassword && confirmPassword.isNotEmpty())
                                                MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                if (confirmPasswordVisible) RIcons.VisibilityOff else RIcons.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    isError = newPassword != confirmPassword && confirmPassword.isNotEmpty()
                                )
                            }
                        }
                    }
                }

                // Display error
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + scaleIn() + expandVertically(),
                    exit = fadeOut() + scaleOut() + shrinkVertically()
                ) {
                    state.error?.let { errorMsg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .clickable() {
                                        clipboardManager.setText(AnnotatedString(errorMsg))
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                // Actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Save button
                    Button(
                        onClick = { component.accept(ProfileStore.Intent.SaveProfile) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !state.loading &&
                                username.isNotEmpty() &&
                                (currentPassword.isEmpty() ||
                                (newPassword.isNotEmpty() && newPassword == confirmPassword)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = RIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Сохранить изменения",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Logout button
                    Button(
                        onClick = { component.accept(ProfileStore.Intent.Logout) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        enabled = !state.loading
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = RIcons.Exit,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Выйти из аккаунта",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

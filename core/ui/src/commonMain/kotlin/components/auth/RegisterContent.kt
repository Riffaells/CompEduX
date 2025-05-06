package components.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import component.app.auth.register.RegisterComponent
import component.app.auth.store.RegisterStore
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun RegisterContent(component: RegisterComponent) {
    val state by component.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Только UI-специфичное состояние
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Локальные переменные для полей формы
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Автоматическое заполнение полей для тестирования
    LaunchedEffect(Unit) {
        // Данные для тестирования
        username = "testuser"
        email = "test@example.com"
        password = "Test123456!"
        confirmPassword = "Test123456!"
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
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = RIcons.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Присоединяйся к нам!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Будет весело, обещаем!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Input Fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { value ->
                            if (value.length <= 20) {
                                username = value
                            }
                        },
                        label = { Text("Придумай классное имя") },
                        leadingIcon = {
                            Icon(
                                RIcons.Person,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        supportingText = {
                            // Показываем счетчик только при превышении лимита
                            if (username.length >= 15) {
                                Text(
                                    "${username.length}/20",
                                    color = if (username.length == 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { value ->
                            email = value
                        },
                        label = { Text("Твоя волшебная почта") },
                        leadingIcon = {
                            Icon(
                                RIcons.Email,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { value ->
                            password = value
                        },
                        label = { Text("Секретный пароль (никому не говори!)") },
                        leadingIcon = {
                            Icon(
                                RIcons.Lock,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) RIcons.VisibilityOff else RIcons.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { value ->
                            confirmPassword = value
                        },
                        label = { Text("Повтори пароль ещё разок") },
                        leadingIcon = {
                            Icon(
                                RIcons.Lock,
                                null,
                                tint = if (password != confirmPassword && confirmPassword.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) RIcons.VisibilityOff else RIcons.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = password != confirmPassword && confirmPassword.isNotEmpty()
                    )
                }

                // Error message
                AnimatedVisibility(visible = state.error != null) {
                    state.error?.let { errorMsg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(errorMsg))
                                    }
                                    .wrapContentWidth(),
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
                    Button(
                        onClick = {
                            if (!state.isLoading) {
                                component.accept(
                                    RegisterStore.Intent.RegisterClicked(
                                        username = username,
                                        email = email,
                                        password = password
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !state.isLoading &&
                                username.isNotEmpty() &&
                                email.isNotEmpty() &&
                                password.isNotEmpty() &&
                                password == confirmPassword,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state.isLoading) {
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
                                Icon(RIcons.PersonAdd, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Создать супер-аккаунт!")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { component.onBackClick() },
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(RIcons.ArrowBack, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Назад")
                            }
                        }

                        OutlinedButton(
                            onClick = { component.accept(RegisterStore.Intent.NavigateToLogin) },
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Войти")
                                Spacer(Modifier.width(4.dp))
                                Icon(RIcons.Login, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

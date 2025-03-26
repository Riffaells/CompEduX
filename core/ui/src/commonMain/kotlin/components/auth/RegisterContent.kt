package components.auth

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import component.app.auth.register.RegisterComponent
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(component: RegisterComponent) {
    val state by component.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = RIcons.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Создание аккаунта",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Присоединяйтесь к нам",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Input Fields with animations
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
                        label = { Text("Имя пользователя") },
                        leadingIcon = { Icon(RIcons.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        supportingText = {
                            Text("${username.length}/20")
                        }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { value ->
                            email = value
                        },
                        label = { Text("Email") },
                        leadingIcon = { Icon(RIcons.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { value ->
                            password = value
                        },
                        label = { Text("Пароль") },
                        leadingIcon = { Icon(RIcons.Lock, null) },
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
                        label = { Text("Подтвердите пароль") },
                        leadingIcon = { Icon(RIcons.Lock, null) },
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

                // Отображение ошибки
                AnimatedVisibility(visible = state.error != null) {
                    state.error?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Индикатор загрузки
                AnimatedVisibility(visible = state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Actions with animations
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { component.onRegisterClick(email, password, confirmPassword, username) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !state.isLoading &&
                                username.isNotEmpty() &&
                                email.isNotEmpty() &&
                                password.isNotEmpty() &&
                                password == confirmPassword
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(RIcons.PersonAdd, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Создать аккаунт")
                        }
                    }

                    TextButton(
                        onClick = { component.onLoginClick() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(RIcons.Login, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Уже есть аккаунт? Войти")
                        }
                    }

                    TextButton(
                        onClick = { component.onBackClick() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.outline
                        ),
                        enabled = !state.isLoading
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(RIcons.ArrowBack, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Назад")
                        }
                    }
                }
            }
        }
    }
}

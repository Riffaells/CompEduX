package components.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
    val clipboardManager = LocalClipboardManager.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Автоматическое заполнение полей для тестирования
    LaunchedEffect(Unit) {
        // Данные для тестирования
        username = "testuser"
        email = "test@example.com"
        password = "Test123456!"
        confirmPassword = "Test123456!"
    }

    // Градиент для фона карточки
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Декоративные элементы фона
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f))
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .background(cardGradient)
                    .padding(horizontal = 32.dp, vertical = 40.dp),
            ) {
                Column(
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
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = RIcons.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { value ->
                                if (value.length <= 20) {
                                    username = value
                                }
                            },
                            label = { Text("Имя пользователя") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        RIcons.Person,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            supportingText = {
                                // Показываем счетчик только при превышении лимита
                                if (username.length >= 20) {
                                    Text("${username.length}/20", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { value ->
                                email = value
                            },
                            label = { Text("Email") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        RIcons.Email,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { value ->
                                password = value
                            },
                            label = { Text("Пароль") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        RIcons.Lock,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
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
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { value ->
                                confirmPassword = value
                            },
                            label = { Text("Подтвердите пароль") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        RIcons.Lock,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
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
                            isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }

                    // Отображение ошибки с возможностью копирования
                    AnimatedVisibility(visible = state.error != null) {
                        state.error?.let { errorMsg ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMsg,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(errorMsg))
                                        }
                                        .padding(12.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Actions with animations
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!state.isLoading) {
                                    component.onRegisterClick(email, password, confirmPassword, username)
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
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            if (state.isLoading) {
                                // Индикатор загрузки внутри кнопки
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
                                    Text("Создать аккаунт")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = { component.onBackClick() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.outline
                                )
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

                            TextButton(
                                onClick = { component.onLoginClick() },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Войти")
                                    Spacer(Modifier.width(8.dp))
                                    Icon(RIcons.Login, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

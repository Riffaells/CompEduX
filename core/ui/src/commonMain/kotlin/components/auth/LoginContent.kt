package components.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compedux.core.ui.generated.resources.*
import component.app.auth.login.LoginComponent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun LoginContent(component: LoginComponent) {
    val state by component.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Анимированные цвета для градиента
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Gradient rotation"
    )

    val primaryBackground = MaterialTheme.colorScheme.primaryContainer
    val secondaryBackground = MaterialTheme.colorScheme.secondaryContainer
    val tertiaryBackground = MaterialTheme.colorScheme.tertiaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val radius = size.width * 1.5f
                val angleInRadians = Math.toRadians(gradientAngle.toDouble()).toFloat()
                val x = center.x + radius * kotlin.math.cos(angleInRadians)
                val y = center.y + radius * kotlin.math.sin(angleInRadians)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryBackground.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(x, y),
                        radius = radius
                    ),
                    radius = radius
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryBackground.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(x * 0.8f, y * 0.8f),
                        radius = radius * 0.7f
                    ),
                    radius = radius * 0.7f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tertiaryBackground.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(x * 1.2f, y * 1.2f),
                        radius = radius * 0.5f
                    ),
                    radius = radius * 0.5f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Декоративные элементы
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 20.dp, y = (-20).dp)
                    .alpha(0.2f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .blur(radius = 40.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-20).dp, y = 20.dp)
                    .alpha(0.2f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.tertiary, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .blur(radius = 35.dp)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .graphicsLayer {
                    alpha = 0.97f
                },
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 8.dp,
            shadowElevation = 10.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        )
                    )
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
                        enter = fadeIn() + slideInVertically() + expandVertically()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.login_welcome_back),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(Res.string.login_we_missed_you),
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
                            value = email,
                            onValueChange = { value ->
                                if (value.length <= 50) {
                                    email = value
                                }
                            },
                            label = { Text(stringResource(Res.string.login_email)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = RIcons.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(16.dp)
                                    shadowElevation = 2f
                                },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { value ->
                                if (value.length <= 32) {
                                    password = value
                                }
                            },
                            label = { Text(stringResource(Res.string.login_password)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = RIcons.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) RIcons.VisibilityOff else RIcons.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(16.dp)
                                    shadowElevation = 2f
                                },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    // Отображение ошибки
                    AnimatedVisibility(
                        visible = state.error != null,
                        enter = fadeIn() + scaleIn() + expandVertically(),
                        exit = fadeOut() + scaleOut() + shrinkVertically()
                    ) {
                        state.error?.let { errorMsg ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }
                    }

                    // Actions with animations
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Кнопка входа с загрузкой внутри
                        Button(
                            onClick = { component.onLoginClick(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(16.dp)
                                    shadowElevation = 4f
                                },
                            shape = RoundedCornerShape(16.dp),
                            enabled = !state.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
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
                                        Icon(
                                            imageVector = RIcons.Login,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(Res.string.login_button),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = { component.onRegisterClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(RIcons.PersonAdd, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.login_create_account),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }

                        TextButton(
                            onClick = { component.onBackClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.outline
                            ),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(RIcons.ArrowBack, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.login_back),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

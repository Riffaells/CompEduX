package components.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import component.app.auth.ProfileComponent
import ui.icon.RIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(component: ProfileComponent) {
    val state by component.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Initialize username from state
    LaunchedEffect(state.username) {
        username = state.username
    }

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
                        colors = listOf(tertiaryBackground.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(x * 0.8f, y * 0.8f),
                        radius = radius * 0.7f
                    ),
                    radius = radius * 0.7f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(secondaryBackground.copy(alpha =.1f), Color.Transparent),
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
                            colors = listOf(MaterialTheme.colorScheme.tertiary, Color.Transparent)
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
                            colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent)
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
                            Icon(
                                imageVector = RIcons.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .graphicsLayer {
                                        shadowElevation = 8f
                                    },
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Profile",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Account Management",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Profile Information
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                component.onUsernameChange(it)
                            },
                            label = { Text("Username") },
                            leadingIcon = {
                                Icon(
                                    imageVector = RIcons.Person,
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

                        // Display email (read-only)
                        OutlinedTextField(
                            value = "test@test.com", // Временное значение для демонстрации
                            onValueChange = { },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = RIcons.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
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
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Password Change Section
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
                                text = "Change Password",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = { Text("Current Password") },
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
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password") },
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
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm New Password") },
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
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        clip = true
                                        shape = RoundedCornerShape(16.dp)
                                        shadowElevation = 2f
                                    },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                isError = newPassword != confirmPassword && confirmPassword.isNotEmpty(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLeadingIconColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }

                    // Display error
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

                    // Actions
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Кнопка сохранения с загрузкой внутри
                        Button(
                            onClick = { component.onUpdateProfile() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(16.dp)
                                    shadowElevation = 4f
                                },
                            shape = RoundedCornerShape(16.dp),
                            enabled = !state.loading &&
                                    username.isNotEmpty() &&
                                    (currentPassword.isEmpty() ||
                                    (newPassword.isNotEmpty() && newPassword == confirmPassword)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                            text = "Save Changes",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Кнопка выхода
                        Button(
                            onClick = { component.onLogout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(16.dp)
                                    shadowElevation = 4f
                                },
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
                                    text = "Log Out",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        TextButton(
                            onClick = { component.onBackClicked() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.outline
                            ),
                            enabled = !state.loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(RIcons.ArrowBack, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Back",
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

package components.settings.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.settings.CategoryBlock
import compedux.core.ui.generated.resources.Res
import compedux.core.ui.generated.resources.network_security
import compedux.core.ui.generated.resources.certificate_validation
import compedux.core.ui.generated.resources.advanced_security
import compedux.core.ui.generated.resources.hide
import compedux.core.ui.generated.resources.show
import compedux.core.ui.generated.resources.auth_token
import compedux.core.ui.generated.resources.security_warning
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SecuritySection(
    isCertificateValidationEnabled: Boolean,
    onCertificateValidationChanged: (Boolean) -> Unit,
    authToken: String,
    onAuthTokenChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    CategoryBlock(
        title = stringResource(Res.string.network_security),
        icon = Icons.Default.Security,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Certificate validation toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.certificate_validation),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isCertificateValidationEnabled,
                    onCheckedChange = onCertificateValidationChanged
                )
            }

            Divider()

            // Advanced security options section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.advanced_security),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isExpanded) stringResource(Res.string.hide) else stringResource(Res.string.show)
                    )
                }
            }

            // Advanced security options content
            AnimatedVisibility(visible = isExpanded) {
                AuthorizationTokenSettings(
                    authToken = authToken,
                    onAuthTokenChanged = onAuthTokenChanged
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AuthorizationTokenSettings(
    authToken: String,
    onAuthTokenChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isTokenVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Auth token input field
        OutlinedTextField(
            value = authToken,
            onValueChange = onAuthTokenChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.auth_token)) },
            trailingIcon = {
                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                    Icon(
                        imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isTokenVisible) stringResource(Res.string.hide) else stringResource(Res.string.show)
                    )
                }
            },
            visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true
        )

        Text(
            text = stringResource(Res.string.security_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

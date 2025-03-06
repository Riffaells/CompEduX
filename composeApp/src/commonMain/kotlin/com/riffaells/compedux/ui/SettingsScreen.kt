package com.riffaells.compedux.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riffaells.compedux.common.Logger
import com.riffaells.compedux.settings.AppSettings

@Composable
fun SettingsScreen(appSettings: AppSettings) {
    // Collect settings as state
//    val isDarkMode by appSettings.isDarkModeFlow().collectAsState(initial = false)
//    val username by appSettings.usernameFlow().collectAsState(initial = "")
//
//     Local state for text field
//    var usernameInput by remember { mutableStateOf(username) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dark mode toggle
        Text(
            text = "Dark Mode",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

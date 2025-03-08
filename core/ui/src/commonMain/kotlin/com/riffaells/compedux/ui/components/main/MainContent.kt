package com.riffaells.compedux.ui.components.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import component.app.main.MainComponent
import component.app.main.store.MainStore

/**
 * Композабл для отображения главного экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(component: MainComponent) {
    // Получаем состояние из компонента
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Главный экран",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { component.onAction(MainStore.Intent.OpenSettings) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Открыть настройки")
            }

            Button(
                onClick = { component.onAction(MainStore.Intent.UpdateTitle("Обновленный заголовок")) },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Обновить заголовок")
            }
        }
    }
}

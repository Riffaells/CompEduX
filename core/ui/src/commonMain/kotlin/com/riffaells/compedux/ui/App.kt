package com.riffaells.compedux.ui

import androidx.compose.runtime.Composable
import com.riffaells.compedux.ui.components.root.RootContent
import com.riffaells.compedux.ui.theme.AppTheme
import component.root.RootComponent

/**
 * Главный композабл приложения, который оборачивает RootContent в тему
 */
@Composable
fun App(
    rootComponent: RootComponent,
    darkTheme: Boolean = false
) {
    AppTheme(darkTheme = darkTheme) {
        RootContent(
            component = rootComponent
        )
    }
}

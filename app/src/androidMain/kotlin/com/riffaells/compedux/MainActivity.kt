package com.riffaells.compedux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import com.riffaells.compedux.di.appDI
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import components.root.RootContent
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory

class AppActivity : ComponentActivity() {
    private lateinit var rootComponent: DefaultRootComponent

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Отключаем проверку главного потока для Android
        System.setProperty("mvikotlin.enableThreadAssertions", "false")

        // Получаем фабрику для RootComponent из DI
        val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()

        // Создаем корневой компонент с контекстом Decompose
        val rootComponent = rootComponentFactory(
            RootComponentParams(
                componentContext = defaultComponentContext()
            )
        )

        // Устанавливаем контент
        setContent {
            withDI(appDI) {
                // Предоставляем AndroidWindowSizeHelper через CompositionLocalProvider
                CompositionLocalProvider(
                    utils.LocalWindowSizeHelper provides utils.AndroidWindowSizeHelper()
                ) {
                    RootContent(component = rootComponent)
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    withDI(appDI) {
        // Используем пустой контент для превью
        // Не используем AppTheme напрямую, так как она может быть internal
    }
}

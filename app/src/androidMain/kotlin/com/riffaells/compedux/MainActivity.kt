package com.riffaells.compedux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import components.root.RootContent
import component.root.DefaultRootComponent
import di.RootComponentParams
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import theme.AppTheme

class AppActivity : ComponentActivity() {
    override val lifecycle = LifecycleRegistry()
    private lateinit var rootComponent: DefaultRootComponent

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                RootContent(component = rootComponent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycle.resume()
    }

    override fun onPause() {
        super.onPause()
        lifecycle.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.destroy()
    }
}

@Preview
@Composable
fun AppPreview() {
    withDI(appDI) {
        AppTheme {
            // Превью не может использовать реальный компонент, поэтому здесь можно создать мок
            // или использовать пустой контент
            // RootContent(component = mockRootComponent)
        }
    }
}

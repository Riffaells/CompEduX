package com.riffaells.compedux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import com.riffaells.compedux.theme.AppTheme
import com.riffaells.compedux.ui.components.root.RootContent
import component.root.DefaultRootComponent
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory

class AppActivity : ComponentActivity() {
    private val lifecycle = LifecycleRegistry()
    private lateinit var rootComponent: DefaultRootComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Создаем корневой компонент
        val rootComponentFactory: (com.arkivanov.decompose.ComponentContext, Any?, Any?) -> DefaultRootComponent = appDI.direct.factory()
        rootComponent = rootComponentFactory(DefaultComponentContext(lifecycle), null, null)

        setContent {
            withDI(appDI) {
                AppTheme {
                    RootContent(component = rootComponent)
                }
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

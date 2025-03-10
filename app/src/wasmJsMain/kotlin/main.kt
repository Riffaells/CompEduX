import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import components.root.RootContent
import component.root.DefaultRootComponent
import di.RootComponentParams
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val body = document.body ?: return
    ComposeViewport(body) {
        withDI(appDI) {
            // Получаем фабрику для RootComponent и создаем компонент
            val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()
            val rootComponent = rootComponentFactory(
                RootComponentParams(
                    componentContext = DefaultComponentContext(lifecycle)
                )
            )

            // Отображаем корневой контент
            RootContent(
                component = rootComponent
            )
        }
    }
}

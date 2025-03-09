import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import component.root.DefaultRootComponent
import components.root.RootContent
import di.RootComponentParams
import kotlinx.browser.document
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    // Отключаем проверку главного потока для WASM
    js("globalThis.mvikotlin_enableThreadAssertions = false")

    // Создаем lifecycle для Decompose
    val lifecycle = LifecycleRegistry()

    // Получаем фабрику для RootComponent из DI
    val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()

    // Создаем корневой компонент
    val rootComponent = rootComponentFactory(
        RootComponentParams(
            componentContext = DefaultComponentContext(lifecycle)
        )
    )

    // Получаем body элемент и устанавливаем ComposeViewport
    val body = document.body ?: return
    ComposeViewport(body) {
        withDI(appDI) {
            RootContent(component = rootComponent)
        }
    }
}

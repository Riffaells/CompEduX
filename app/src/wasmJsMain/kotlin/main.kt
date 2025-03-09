import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import component.root.DefaultRootComponent
import components.root.RootContent
import di.RootComponentParams
import kotlinx.browser.document
import kotlinx.browser.window
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import utils.LocalWindowSizeHelper
import utils.WasmWindowSizeHelper
import kotlin.js.json

// Отключаем проверку главного потока для WASM
// Это должно быть в отдельном файле, но для простоты оставим здесь
@JsName("mvikotlin_enableThreadAssertions")
val mvikotlinEnableThreadAssertions: Boolean = false

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    // Создаем lifecycle для Decompose
    val lifecycle = LifecycleRegistry()

    // Создаем WebHistoryController для работы с историей браузера
    val webHistoryController = object : WebHistoryController {
        override fun navigateToPath(path: String, query: String, hash: String) {
            window.history.pushState(json(), "", buildString {
                append(path)
                if (query.isNotEmpty()) append(query)
                if (hash.isNotEmpty()) append(hash)
            })
        }

        override fun getPath(): String = window.location.pathname
        override fun getQuery(): String = window.location.search
        override fun getHash(): String = window.location.hash
    }

    // Получаем фабрику для RootComponent из DI
    val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()

    // Создаем корневой компонент
    val rootComponent = rootComponentFactory(
        RootComponentParams(
            componentContext = DefaultComponentContext(lifecycle),
            webHistoryController = webHistoryController
        )
    )

    // Получаем body элемент и устанавливаем ComposeViewport
    val body = document.body ?: return
    ComposeViewport(body) {
        withDI(appDI) {
            // Предоставляем WasmWindowSizeHelper через CompositionLocalProvider
            CompositionLocalProvider(
                LocalWindowSizeHelper provides WasmWindowSizeHelper()
            ) {
                RootContent(component = rootComponent)
            }
        }
    }
}

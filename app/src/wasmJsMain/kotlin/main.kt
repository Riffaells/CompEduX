import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import components.root.RootContent
import kotlinx.browser.document
import logging.Logger
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import org.kodein.di.instance

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    // Получаем логгер из DI


    val logger = appDI.direct.instance<Logger>()
    logger.d("Запуск WASM-приложения")

    val lifecycle = LifecycleRegistry()

    val body = document.body ?: return
    ComposeViewport(body) {
        withDI(appDI) {
            logger.d("Инициализация DI")

            // Получаем фабрику для RootComponent и создаем компонент
            val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()
            val rootComponent = rootComponentFactory(
                RootComponentParams(
                    componentContext = DefaultComponentContext(lifecycle)
                )
            )

            logger.d("Рендеринг RootContent")
            // Отображаем корневой контент
            RootContent(
                component = rootComponent
            )
        }
    }
}

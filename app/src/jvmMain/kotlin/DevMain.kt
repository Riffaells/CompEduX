import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import components.root.RootContent
import logging.Logger
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import org.kodein.di.instance

/**
 * Точка входа для разработки с горячей перезагрузкой Compose.
 * Запускать через задачу 'gradle runHot'
 */
fun main() {
    // Отключаем проверки потоков для MVI и Decompose
    System.setProperty("mvikotlin.enableThreadAssertions", "false")
    System.setProperty("decompose.mainThreadChecker.enabled", "false")

    // Получаем логгер из DI
    val logger = appDI.direct.instance<Logger>()
    logger.d("Запуск Hot Reload режима разработки")

    val lifecycle = LifecycleRegistry()

    // Запускаем приложение
    application {
        withDI(appDI) {
            // Настраиваем окно
            val windowState = rememberWindowState(
                width = 1280.dp,
                height = 800.dp,
                position = WindowPosition(Alignment.Center)
            )

            Window(
                onCloseRequest = ::exitApplication,
                title = "CompEduX [Hot Reload]",
                state = windowState,
                alwaysOnTop = true // Удобно для разработки
            ) {
                logger.d("Hot Reload активирован, изменения будут применяться автоматически")

                // Получаем фабрику для RootComponent и создаем компонент
                val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()
                val rootComponent = rootComponentFactory(
                    RootComponentParams(
                        componentContext = DefaultComponentContext(lifecycle)
                    )
                )

                // Отображаем корневой контент приложения
                RootContent(component = rootComponent)
            }
        }
    }
}

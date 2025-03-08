import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.utils.setMainThreadId
import com.riffaells.compedux.App
import com.riffaells.compedux.di.appDI
import com.riffaells.compedux.ui.components.root.RootContent
import com.riffaells.compedux.ui.desktop.DesktopContent
import component.root.DefaultRootComponent
import di.RootComponentParams
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import javax.swing.SwingUtilities

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    // Отключаем проверку главного потока для JVM-платформы
    System.setProperty("mvikotlin.enableThreadAssertions", "false")

    // Устанавливаем обработчик необработанных исключений, чтобы приложение не падало
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        println("Uncaught exception in thread $thread: ${throwable.message}")
        // Не делаем ничего, просто логируем ошибку
    }

    val lifecycle = LifecycleRegistry()


    application {
        withDI(appDI) {

            // Получаем фабрику для RootComponent и создаем компонент
            val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()
            val rootComponent = runOnUiThread {
                // Устанавливаем ID главного потока как AWT-EventQueue
                // Ошибка в главном потоке будет вызывать исключение в Settings как минимум
//                setMainThreadId(Thread.currentThread().id)

                rootComponentFactory(
                    RootComponentParams(
                        componentContext = DefaultComponentContext(lifecycle)
                    )
                )
            }

            val windowState = rememberWindowState()
            LifecycleController(lifecycle, windowState)


            Window(
                onCloseRequest = ::exitApplication,
                title = "CompEduX",
                decoration = WindowDecoration.Undecorated(),
            ) {

                RootContent(
                    component = rootComponent,
                ) { component, isSpace ->

                    DesktopContent(
                        modifier = Modifier,
                        isSpace = isSpace,
                        onCloseRequest = ::exitApplication,
                        onMinimizeRequest = { windowState.isMinimized = true },
                        onMaximizeRequest = {
                            if (windowState.placement == WindowPlacement.Floating)
                                windowState.placement = WindowPlacement.Maximized
                            else
                                windowState.placement = WindowPlacement.Floating
                        },
                    )
                }
            }
        }
    }
}

// Вспомогательная функция для выполнения кода в UI потоке
internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }

    error?.also { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}

@Preview
@Composable
fun AppPreview() {
    App()
}

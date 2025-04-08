import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.riffaells.compedux.di.appDI
import compedux.app.generated.resources.Res
import compedux.app.generated.resources.app_name
import component.root.DefaultRootComponent
import component.root.RootComponentParams
import components.root.RootContent
import di.Logger
import org.jetbrains.compose.resources.stringResource
import org.kodein.di.compose.withDI
import org.kodein.di.direct
import org.kodein.di.factory
import org.kodein.di.instance
import ui.desktop.DesktopContent
import java.awt.geom.RoundRectangle2D
import javax.swing.SwingUtilities

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    // Получаем логгер из DI
    val logger = appDI.direct.instance<Logger>()
    logger.d("Запуск JVM-приложения")

    // Отключаем проверку главного потока для JVM-платформы
    System.setProperty("mvikotlin.enableThreadAssertions", "false")

    // Отключаем проверку главного потока для Decompose
    System.setProperty("decompose.mainThreadChecker.enabled", "false")


    // Устанавливаем обработчик необработанных исключений, чтобы приложение не падало
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.e("Uncaught exception in thread $thread: ${throwable.message}", throwable)
        // Не делаем ничего, просто логируем ошибку
    }

    val lifecycle = LifecycleRegistry()

    logger.d("Создание жизненного цикла приложения")

    application {
        withDI(appDI) {
            logger.d("Инициализация DI")

            // Получаем фабрику для RootComponent и создаем компонент
            val rootComponentFactory = appDI.direct.factory<RootComponentParams, DefaultRootComponent>()
            val rootComponent = runOnUiThread {
                logger.d("Создание RootComponent в UI потоке")

                rootComponentFactory(
                    RootComponentParams(
                        componentContext = DefaultComponentContext(lifecycle)
                    )
                )
            }

            val windowState = rememberWindowState(
                width = 1280.dp,
                height = 800.dp,
                position = WindowPosition(Alignment.Center)
            )
            LifecycleController(lifecycle, windowState)

            // Константы для скругления углов
            val cornerRadius = 24f

            // Функция для обновления формы окна
            fun updateWindowShape(window: java.awt.Window, radius: Float, isMaximized: Boolean = false) {
                // Если окно максимизировано, убираем скругление углов
                val effectiveRadius = if (isMaximized) 0f else radius
                window.shape = RoundRectangle2D.Float(
                    0f, 0f,
                    window.width.toFloat(), window.height.toFloat(),
                    effectiveRadius, effectiveRadius
                )
            }

            logger.d("Создание главного окна приложения")
            Window(
                onCloseRequest = ::exitApplication,
                title = "CompEduX",
                decoration = WindowDecoration.Undecorated(),
                transparent = true,
                state = windowState,
                onPreviewKeyEvent = {
                    false
                }
            ) {
                // Отслеживаем состояние максимизации окна
                val isMaximized = windowState.placement == WindowPlacement.Maximized

                // Устанавливаем форму окна со скругленными углами
                updateWindowShape(window, cornerRadius, isMaximized)

                // Добавляем слушатель изменения размера окна
                window.addComponentListener(object : java.awt.event.ComponentAdapter() {
                    override fun componentResized(e: java.awt.event.ComponentEvent) {
                        // Обновляем форму при изменении размера
                        updateWindowShape(window, cornerRadius, isMaximized)
                    }
                })

                // Отслеживаем изменения состояния окна
                LaunchedEffect(windowState.placement) {
                    updateWindowShape(window, cornerRadius, isMaximized)
                }

                logger.d("Рендеринг RootContent")
                RootContent(
                    component = rootComponent,
                ) { component, isSpace ->

                    DesktopContent(
                        modifier = Modifier,
                        title = stringResource(Res.string.app_name),
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

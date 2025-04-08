package com.riffaells.compedux

import android.app.Application
import android.content.Context
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.riffaells.compedux.di.appDI
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class App : Application(), DIAware {

    override val di: DI by DI.lazy {
        // Импортируем основной DI контейнер
        extend(appDI)

        // Привязываем контекст приложения
        bindSingleton<Context> { this@App.applicationContext }

        // Привязываем Android-специфичные зависимости
        bindSingleton<StoreFactory> { DefaultStoreFactory() }
    }

    override fun onCreate() {
        super.onCreate()

        // Инициализируем Napier для логирования
        Napier.base(DebugAntilog())
        Napier.d("Initializing application")

        // Отключаем проверку главного потока для Android
        System.setProperty("mvikotlin.enableThreadAssertions", "false")

        // Отключаем проверку главного потока для Decompose
        System.setProperty("decompose.mainThreadChecker.enabled", "false")

        // Устанавливаем обработчик необработанных исключений
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Napier.e("Uncaught exception in thread $thread", throwable)
        }

        // Инициализируем необходимые компоненты
        val storeFactory by instance<StoreFactory>()
        // Здесь можно добавить инициализацию других компонентов, если необходимо
    }
}


//fun Context.isDebug() = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE

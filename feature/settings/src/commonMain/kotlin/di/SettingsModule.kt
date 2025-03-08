package di

import MultiplatformSettings
import MultiplatformSettingsImpl
import com.russhwolf.settings.Settings
import org.kodein.di.DI
import org.kodein.di.bindSingleton


val settingsModule = DI.Module("settingsModule") {
    bindSingleton<MultiplatformSettings> { MultiplatformSettingsImpl(Settings()) }
}
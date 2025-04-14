package components.settings.model

import component.app.settings.SettingsComponent

/**
 * Sealed класс для представления состояний различных категорий настроек
 */
sealed class CategoryState {
    /**
     * Базовое свойство для всех состояний категорий - заголовок
     */
    abstract val title: String

    /**
     * Состояние для категории внешнего вида
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Appearance(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории языка
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Language(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории сети
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Network(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории безопасности
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Security(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории уведомлений
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Notifications(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории хранилища
     *
     * @param title Заголовок категории
     */
    data class Storage(
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории системных настроек
     *
     * @param title Заголовок категории
     */
    data class System(
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории экспериментальных настроек
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Experimental(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()

    /**
     * Состояние для категории профиля
     *
     * @param component Компонент настроек
     * @param title Заголовок категории
     */
    data class Profile(
        val component: SettingsComponent,
        override val title: String
    ) : CategoryState()
}

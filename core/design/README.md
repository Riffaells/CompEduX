# Design Module

Модуль `design` содержит общие компоненты пользовательского интерфейса, которые используются во всем приложении.

## Компоненты навигации

### FloatingNavigationBar

Кастомный компонент нижней навигации с эффектом "парения" и размытием фона.

```kotlin
FloatingNavigationBar(
    modifier = Modifier,
    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    contentColor = MaterialTheme.colorScheme.onSurface,
    elevation = 8f,
    blurRadius = 15f,
    cornerRadius = 24f
) {
    // Элементы навигации
    FloatingNavigationBarItem(
        selected = true,
        onClick = { /* действие */ },
        icon = { Icon(Icons.Default.Home, contentDescription = null) },
        label = { Text("Главная") }
    )
    // Другие элементы...
}
```

### FloatingNavigationRail

Кастомный компонент боковой навигации с эффектом "парения" и размытием фона.

```kotlin
FloatingNavigationRail(
    modifier = Modifier,
    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    contentColor = MaterialTheme.colorScheme.onSurface,
    elevation = 8f,
    blurRadius = 15f,
    cornerRadius = 24f
) {
    // Элементы навигации
    FloatingNavigationRailItem(
        selected = true,
        onClick = { /* действие */ },
        icon = { Icon(Icons.Default.Home, contentDescription = null) },
        label = { Text("Главная") }
    )
    // Другие элементы...
}
```

### NavigationHost

Компонент для управления навигацией, который автоматически выбирает подходящий тип навигации в зависимости от размера
экрана.

```kotlin
// Создаем конфигурацию навигации
val navigationConfig = NavigationConfig().apply {
    addItem(
        id = "main",
        icon = Icons.Default.Home,
        label = "Главная",
        contentDescription = "Перейти на главную страницу",
        onClick = { /* действие */ }
    )
    // Другие элементы...
}

// Используем NavigationHost
NavigationHost(
    config = navigationConfig,
    selectedItemId = "main",
    isLargeScreen = isLargeScreen(),
    onItemSelected = { id -> /* обработка выбора */ }
)
```

## Эффекты

### BlurEffect

Компонент для создания эффекта размытия фона.

```kotlin
BlurEffect(
    modifier = Modifier,
    radius = 10f,
    backgroundColor = Color.White.copy(alpha = 0.1f)
)
```

## Использование

Для использования компонентов из модуля `design` добавьте зависимость в ваш модуль:

```kotlin
// build.gradle.kts
dependencies {
    implementation(projects.core.design)
}
```

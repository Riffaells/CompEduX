# CompEduX - Стратегия Развития

## Общая архитектура

CompEduX представляет собой образовательную платформу, построенную на многослойной архитектуре:

1. **Клиентский уровень**: Многоплатформенное приложение на Kotlin Multiplatform с использованием Compose.
2. **Серверный уровень**: Микросервисная архитектура с отдельными сервисами для аутентификации, управления курсами и
   т.д.
3. **Уровень данных**: Реляционная база данных для хранения информации о пользователях, курсах и прогрессе.

## Текущая структура проекта

Проект использует модульную архитектуру с разделением ответственности:

### Модули ядра (core)

- **app** - точка входа в приложение
- **common** - общие компоненты, навигация UI и бизнес-логика UI
- **domain** - бизнес-логика и модели данных
- **data** - репозитории и источники данных
- **ui** - основные UI компоненты
- **design** - базовые UI компоненты, стили и темы
- **utils** - утилитарные функции и классы
- **network** - работа с API и сетевые взаимодействия

### Модули фич (feature)

- **settings** - настройки приложения
- **tree** - визуализация и взаимодействие с деревом курса (в разработке)

### Особенности сборки и конфигурации

- Используется build logic в виде convention plugins
- Настроены правильные зависимости между модулями
- Подготовлена инфраструктура для кросс-платформенной разработки

### Текущий статус разработки

- Реализован серверный API для управления курсами
- В процессе интеграции API курсов в клиентское приложение
- Планируется реализация рендеринга дерева курса с использованием Skiko
- Локализация будет реализована через стандартные механизмы Compose resources

## Ключевые сущности

### Пользователь (User)

- Аутентификация и авторизация (уже реализовано)
- Профиль пользователя
- Роли (студент, преподаватель, администратор)

### Курс (Course)

- Метаданные (ключи для названия, описания)
- Структура курса в виде дерева развития (JSON)
- Стили и конфигурация для визуализации
- Мультиязычные материалы через систему ключей
- Достижения

### Комната (Room)

- Экземпляр курса для группы пользователей
- Отслеживание прогресса участников
- Управление соревнованиями

### Соревнование (Competition)

- Различные типы соревнований
- Правила и метрики
- Система наград

## Этапы реализации

### Этап 1: Серверная часть - Основа (Текущий фокус)

1. **Определение API для управления курсами**
    - Создание, чтение, обновление и удаление курсов
    - Загрузка и управление структурой курса (JSON-дерево)
    - Управление мультиязычным контентом

2. **Хранение данных**
    - Проектирование схемы базы данных для курсов
    - Реализация репозиториев и сервисов для работы с данными

### Этап 2: Серверная часть - Комнаты и Прогресс

1. **API для управления комнатами**
    - Создание и управление комнатами
    - Добавление/удаление участников

2. **Система отслеживания прогресса**
    - Фиксация прохождения элементов курса
    - Получение и обновление достижений

### Этап 3: Серверная часть - Соревнования

1. **API для соревнований**
    - Определение типов соревнований
    - Логика проведения и подсчета результатов

### Этап 4: Клиентская часть - Основной интерфейс

1. **Визуализация дерева курса**
    - Реализация рендеринга JSON-структуры через Skiko
    - Интерактивная навигация по дереву

2. **Отображение мультиязычного контента**
    - Переключение между языками
    - Адаптивное отображение контента

### Этап 5: Клиентская часть - Комнаты и Прогресс

1. **Интерфейс комнаты**
    - Отображение прогресса участников
    - Социальные элементы взаимодействия

2. **Система достижений**
    - Визуализация полученных достижений
    - Уведомления о новых достижениях

### Этап 6: Клиентская часть - Соревнования

1. **Интерфейс соревнований**
    - Участие и отслеживание результатов
    - Рейтинги и лидерборды

## Техническая реализация

### Технологический стек

1. **Серверная часть**:
    - FastAPI для микросервисов
    - API Gateway для маршрутизации запросов
    - PostgreSQL для хранения данных (JSONB для структур)

2. **Клиентская часть**:
    - Kotlin Multiplatform с Compose
    - Skiko для рендеринга графических элементов

### Расширенный формат JSON для дерева курса

Обновленная и расширенная структура JSON для полной поддержки требований проекта:

```json
{
  "metadata": {
    "title": "Python Programming",
    "description": "Basic course for beginners",
    "version": "1.0",
    "defaultLanguage": "en",
    "author": "CompEduX Team",
    "tags": ["programming", "python", "beginner"]
  },
  "displaySettings": {
    "theme": "dark",
    "defaultScale": 1.0,
    "gridSize": 50,
    "background": "grid"
  },
  "nodes": [
    {
      "id": "node1",
      "titleKey": "course.intro",
      "descriptionKey": "course.intro.desc",
      "position": {"x": 100, "y": 150},
      "style": "circular",
      "styleClass": "beginner",
      "state": "available",
      "difficulty": 1,
      "estimatedTime": 20,
      "children": ["node2", "node3"],
      "contentId": "content123",
      "requirements": [],
      "achievements": ["ach1"]
    },
    {
      "id": "node2",
      "titleKey": "course.basics",
      "descriptionKey": "course.basics.desc",
      "position": {"x": 200, "y": 100},
      "style": "hexagon",
      "styleClass": "intermediate",
      "state": "locked",
      "difficulty": 2,
      "estimatedTime": 45,
      "children": ["node4"],
      "contentId": "content456",
      "requirements": ["node1"],
      "achievements": ["ach2", "ach3"]
    }
  ],
  "connections": [
    {
      "id": "conn1",
      "from": "node1",
      "to": "node2",
      "style": "solid_arrow",
      "styleClass": "required",
      "label": "Move to basics"
    },
    {
      "id": "conn2",
      "from": "node1",
      "to": "node3",
      "style": "dashed_line",
      "styleClass": "optional",
      "label": "Additional"
    }
  ],
  "achievements": [
    {
      "id": "ach1",
      "titleKey": "achievement.first_step",
      "descriptionKey": "achievement.first_step.desc",
      "iconUrl": "icons/first_step.svg",
      "points": 10
    },
    {
      "id": "ach2",
      "titleKey": "achievement.code_master",
      "descriptionKey": "achievement.code_master.desc",
      "iconUrl": "icons/code_master.svg",
      "points": 20
    }
  ],
  "styles": {
    "nodeStyles": {
      "beginner": {
        "color": "#4CAF50",
        "borderColor": "#2E7D32",
        "shape": "circular",
        "icon": "icons/beginner.svg"
      },
      "intermediate": {
        "color": "#2196F3",
        "borderColor": "#0D47A1",
        "shape": "hexagon",
        "icon": "icons/intermediate.svg"
      },
      "advanced": {
        "color": "#FF5722",
        "borderColor": "#BF360C",
        "shape": "square",
        "icon": "icons/advanced.svg"
      }
    },
    "connectionStyles": {
      "required": {
        "color": "#FFFFFF",
        "thickness": 2,
        "style": "solid_arrow"
      },
      "optional": {
        "color": "#AAAAAA",
        "thickness": 1,
        "style": "dashed_line"
      }
    }
  }
}
```

#### Описание компонентов расширенного формата JSON:

1. **Метаданные (metadata)**
    - Информация о курсе (название, описание, версия)
    - Базовые настройки (язык по умолчанию, автор)
    - Теги для категоризации и поиска

2. **Настройки отображения (displaySettings)**
    - Тема оформления (светлая/темная)
    - Масштаб по умолчанию
    - Размер сетки для выравнивания
    - Настройки фона

3. **Узлы (nodes)**
    - Базовая информация (id, ключи локализации)
    - Позиционирование (координаты x, y)
    - Стиль и оформление (форма, класс стиля)
    - Состояние доступа (доступно, заблокировано, завершено)
    - Метрики (сложность, расчетное время)
    - Связи (дочерние узлы, требования)
    - Достижения, связанные с узлом

4. **Соединения (connections)**
    - Уникальный идентификатор
    - Начальная и конечная точки
    - Стиль отображения (тип линии, класс стиля)
    - Опциональная метка для соединения

5. **Достижения (achievements)**
    - Идентификатор и ключи локализации
    - Ссылка на иконку
    - Количество очков

6. **Стили (styles)**
    - Коллекции стилей для узлов разных типов
    - Коллекции стилей для соединений
    - Позволяет переиспользовать стили через styleClass

## Модели данных (расширенный вид)

```kotlin
// Модель курса
data class Course(
    val id: UUID,
    val titleKey: String,
    val descriptionKey: String,
    val treeStructure: TreeStructure,
    val styleConfig: StyleConfig,
    val defaultLanguage: String,
    val version: String,
    val author: String,
    val tags: List<String>
)

// Структура дерева
data class TreeStructure(
    val metadata: TreeMetadata,
    val displaySettings: DisplaySettings,
    val nodes: List<TreeNode>,
    val connections: List<TreeConnection>,
    val achievements: List<Achievement>,
    val styles: StyleDefinitions
)

// Узел дерева
data class TreeNode(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val position: Position,
    val style: String,
    val styleClass: String,
    val state: NodeState,
    val difficulty: Int,
    val estimatedTime: Int,
    val children: List<String>,
    val contentId: String,
    val requirements: List<String>,
    val achievements: List<String>
)

// Соединение между узлами
data class TreeConnection(
    val id: String,
    val from: String,
    val to: String,
    val style: String,
    val styleClass: String,
    val label: String?
)

// Достижение
data class Achievement(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val iconUrl: String,
    val points: Int
)

// Состояние узла
enum class NodeState {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    IN_PROGRESS
}
```

## Визуализация дерева развития

Визуализация дерева будет реализована с использованием Skiko для рендеринга графических элементов, аналогично
древовидным структурам в играх.

### Ключевые аспекты визуализации:

1. **Узлы дерева**
    - Различные визуальные стили (круги, шестиугольники, квадраты)
    - Цветовое кодирование (по типу, сложности, состоянию)
    - Отображение иконок и индикаторов прогресса

2. **Соединения между узлами**
    - Типы линий (сплошные, пунктирные, с градиентом)
    - Стрелки, показывающие направление прогресса
    - Обозначение обязательных и опциональных связей

3. **Позиционирование**
    - Координаты x, y для точного размещения узлов
    - Автоматическое размещение по сетке
    - Масштабирование и панорамирование области просмотра

4. **Состояние узлов**
    - Визуальные индикаторы прогресса (доступно, заблокировано, завершено)
    - Анимации при изменении состояния
    - Подсветка активного и доступных узлов

5. **Интерактивность**
    - Выбор узла по клику/касанию
    - Навигация по дереву (перемещение, масштабирование)
    - Отображение подробной информации по выбранному узлу

### Кастомизация визуализации:

- Определение стилей через конфигурацию курса
- Поддержка различных тем оформления (светлая/темная)
- Динамическое позиционирование узлов для адаптации к разным экранам

### Рендеринг и производительность:

- Оптимизация отрисовки больших деревьев
- Подход "виртуального списка" для отображения только видимых узлов
- Уменьшение деталей при отдалении и увеличение при приближении

## Мультиязычная поддержка

1. **Подход к локализации**:
    - Использование ключей вместо прямых текстов
    - Отдельные файлы локализации для каждого языка
    - Fallback на ключ или язык по умолчанию при отсутствии перевода

2. **Структура файлов локализации**:
   ```json
   {
     "en": {
       "course.intro": "Introduction",
       "course.intro.desc": "Getting started with the course",
       "course.basics": "Basics",
       "course.basics.desc": "Basic concepts",
       "achievement.first_step": "First Step",
       "achievement.first_step.desc": "You've made your first step in learning"
     },
     "ru": {
       "course.intro": "Введение",
       "course.intro.desc": "Начало работы с курсом",
       "course.basics": "Основы",
       "course.basics.desc": "Базовые концепции",
       "achievement.first_step": "Первый шаг",
       "achievement.first_step.desc": "Вы сделали первый шаг в обучении"
     }
   }
   ```

3. **Логика работы с переводами**:
   ```kotlin
   fun getLocalizedText(key: String, language: String, fallbackLanguage: String = "en"): String {
       return when {
           key in translations[language] -> translations[language][key]
           key in translations[fallbackLanguage] -> translations[fallbackLanguage][key]
           else -> key
       }
   }
   ```

4. **Динамическое переключение языков**:
    - Мгновенное обновление интерфейса при смене языка
    - Сохранение предпочтений пользователя
    - Информация о доступных переводах для контента

## Интеграция с сервером

1. **API для работы с деревом**:
   ```
   GET /api/courses/{courseId}/tree            # Get tree structure
   GET /api/courses/{courseId}/tree/node/{id}  # Get specific node
   POST /api/courses/{courseId}/tree           # Create/update tree
   GET /api/courses/{courseId}/localizations   # Get translations
   ```

2. **Отслеживание прогресса**:
   ```
   GET /api/rooms/{roomId}/progress            # User progress in room
   POST /api/rooms/{roomId}/progress/{nodeId}  # Update progress for node
   ```

3. **Реализация оффлайн-режима**:
    - Локальное кэширование дерева и контента
    - Синхронизация прогресса при подключении
    - Статус синхронизации с сервером

## Следующие шаги

1. Спроектировать расширенные модели данных
2. Реализовать серверное API для управления деревьями курсов
3. Создать прототип рендеринга дерева с поддержкой всех типов узлов и связей
4. Реализовать систему отслеживания прогресса с визуальной индикацией состояний

# CompEduX - Development Strategy

## Key Entities

### User

- Authentication and authorization (already implemented)
- User profile
- Roles (student, teacher, administrator)

### Course

- Metadata (keys for title, description)
- Course structure as a development tree (JSON)
- Styles and configuration for visualization
- Multilingual materials via key system
- Achievements

### Room

- Course instance for a group of users
- Tracking participants' progress
- Competition management

## Technology Stack

1. **Server Side**:
    - FastAPI for microservices
    - API Gateway for request routing
    - PostgreSQL for data storage (JSONB for structures)

2. **Client Side**:
    - Kotlin Multiplatform with Compose
    - Skiko for rendering graphical elements

## Enhanced JSON Format for Course Tree

Updated and expanded structure to fully support project requirements:

```json
{
  "metadata": {
    "title": "Python Programming",
    "description": "Basic course for beginners",
    "version": "1.0",
    "defaultLanguage": "en",
    "author": "CompEduX Team",
    "tags": ["programming", "python", "beginner"]
  },
  "displaySettings": {
    "theme": "dark",
    "defaultScale": 1.0,
    "gridSize": 50,
    "background": "grid"
  },
  "nodes": [
    {
      "id": "node1",
      "titleKey": "course.intro",
      "descriptionKey": "course.intro.desc",
      "position": {"x": 100, "y": 150},
      "style": "circular",
      "styleClass": "beginner",
      "state": "available",
      "difficulty": 1,
      "estimatedTime": 20,
      "children": ["node2", "node3"],
      "contentId": "content123",
      "requirements": [],
      "achievements": ["ach1"]
    },
    {
      "id": "node2",
      "titleKey": "course.basics",
      "descriptionKey": "course.basics.desc",
      "position": {"x": 200, "y": 100},
      "style": "hexagon",
      "styleClass": "intermediate",
      "state": "locked",
      "difficulty": 2,
      "estimatedTime": 45,
      "children": ["node4"],
      "contentId": "content456",
      "requirements": ["node1"],
      "achievements": ["ach2", "ach3"]
    }
  ],
  "connections": [
    {
      "id": "conn1",
      "from": "node1",
      "to": "node2",
      "style": "solid_arrow",
      "styleClass": "required",
      "label": "Move to basics"
    },
    {
      "id": "conn2",
      "from": "node1",
      "to": "node3",
      "style": "dashed_line",
      "styleClass": "optional",
      "label": "Additional"
    }
  ],
  "achievements": [
    {
      "id": "ach1",
      "titleKey": "achievement.first_step",
      "descriptionKey": "achievement.first_step.desc",
      "iconUrl": "icons/first_step.svg",
      "points": 10
    },
    {
      "id": "ach2",
      "titleKey": "achievement.code_master",
      "descriptionKey": "achievement.code_master.desc",
      "iconUrl": "icons/code_master.svg",
      "points": 20
    }
  ],
  "styles": {
    "nodeStyles": {
      "beginner": {
        "color": "#4CAF50",
        "borderColor": "#2E7D32",
        "shape": "circular",
        "icon": "icons/beginner.svg"
      },
      "intermediate": {
        "color": "#2196F3",
        "borderColor": "#0D47A1",
        "shape": "hexagon",
        "icon": "icons/intermediate.svg"
      },
      "advanced": {
        "color": "#FF5722",
        "borderColor": "#BF360C",
        "shape": "square",
        "icon": "icons/advanced.svg"
      }
    },
    "connectionStyles": {
      "required": {
        "color": "#FFFFFF",
        "thickness": 2,
        "style": "solid_arrow"
      },
      "optional": {
        "color": "#AAAAAA",
        "thickness": 1,
        "style": "dashed_line"
      }
    }
  }
}
```

## Data Models (Enhanced View)

```kotlin
// Course model
data class Course(
    val id: UUID,
    val titleKey: String,
    val descriptionKey: String,
    val treeStructure: TreeStructure,
    val styleConfig: StyleConfig,
    val defaultLanguage: String,
    val version: String,
    val author: String,
    val tags: List<String>
)

// Tree structure
data class TreeStructure(
    val metadata: TreeMetadata,
    val displaySettings: DisplaySettings,
    val nodes: List<TreeNode>,
    val connections: List<TreeConnection>,
    val achievements: List<Achievement>,
    val styles: StyleDefinitions
)

// Tree node
data class TreeNode(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val position: Position,
    val style: String,
    val styleClass: String,
    val state: NodeState,
    val difficulty: Int,
    val estimatedTime: Int,
    val children: List<String>,
    val contentId: String,
    val requirements: List<String>,
    val achievements: List<String>
)

// Connection between nodes
data class TreeConnection(
    val id: String,
    val from: String,
    val to: String,
    val style: String,
    val styleClass: String,
    val label: String?
)

// Achievement
data class Achievement(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val iconUrl: String,
    val points: Int
)

// Node state
enum class NodeState {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    IN_PROGRESS
}
```

## Development Tree Visualization

The tree visualization will be implemented using Skiko for rendering graphical elements, similar to tree structures in
games.

### Key Visualization Aspects:

1. **Tree Nodes**
    - Various visual styles (circles, hexagons, squares)
    - Color coding (by type, difficulty, state)
    - Display of icons and progress indicators

2. **Connections Between Nodes**
    - Line types (solid, dashed, gradient)
    - Arrows showing progress direction
    - Designation of required and optional connections

3. **Positioning**
    - X, y coordinates for precise node placement
    - Automatic grid placement
    - Scaling and panning of the viewport

4. **Node States**
    - Visual progress indicators (available, locked, completed)
    - Animations when changing states
    - Highlighting of active and available nodes

5. **Interactivity**
    - Node selection by click/touch
    - Tree navigation (moving, scaling)
    - Displaying detailed information on the selected node

### Visualization Customization:

- Style definitions through course configuration
- Support for different themes (light/dark)
- Dynamic node positioning for adaptation to different screens

### Rendering and Performance:

- Optimization of large tree rendering
- "Virtual list" approach to display only visible nodes
- Reducing details when zooming out and increasing when zooming in

## Multilingual Support

1. **Localization Approach**:
    - Using keys instead of direct texts
    - Separate localization files for each language
    - Fallback to key or default language when translation is missing

2. **Localization File Structure**:
   ```json
   {
     "en": {
       "course.intro": "Introduction",
       "course.intro.desc": "Getting started with the course",
       "course.basics": "Basics",
       "course.basics.desc": "Basic concepts",
       "achievement.first_step": "First Step",
       "achievement.first_step.desc": "You've made your first step in learning"
     },
     "ru": {
       "course.intro": "Введение",
       "course.intro.desc": "Начало работы с курсом",
       "course.basics": "Основы",
       "course.basics.desc": "Базовые концепции",
       "achievement.first_step": "Первый шаг",
       "achievement.first_step.desc": "Вы сделали первый шаг в обучении"
     }
   }
   ```

3. **Translation Logic**:
   ```kotlin
   fun getLocalizedText(key: String, language: String, fallbackLanguage: String = "en"): String {
       return when {
           key in translations[language] -> translations[language][key]
           key in translations[fallbackLanguage] -> translations[fallbackLanguage][key]
           else -> key
       }
   }
   ```

4. **Dynamic Language Switching**:
    - Instant interface update when changing language
    - Saving user preferences
    - Information about available translations for content

## Server Integration

1. **API for Working with Tree**:
   ```
   GET /api/courses/{courseId}/tree            # Get tree structure
   GET /api/courses/{courseId}/tree/node/{id}  # Get specific node
   POST /api/courses/{courseId}/tree           # Create/update tree
   GET /api/courses/{courseId}/localizations   # Get translations
   ```

2. **Progress Tracking**:
   ```
   GET /api/rooms/{roomId}/progress            # User progress in room
   POST /api/rooms/{roomId}/progress/{nodeId}  # Update progress for node
   ```

3. **Offline Mode Implementation**:
    - Local caching of tree and content
    - Progress synchronization upon connection
    - Server synchronization status

## Next Steps

1. Design enhanced data models
2. Implement server API for course tree management
3. Create a tree rendering prototype with support for all node and connection types
4. Implement a progress tracking system with visual state indication

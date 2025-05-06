# Структура технологического дерева

Этот документ описывает обновленную структуру технологического дерева, используемого для представления схемы развития в курсах.

## Обзор

Технологическое дерево представляет собой структуру, отображающую логические связи между различными элементами учебного контента. Это позволяет визуализировать путь обучения и зависимости между темами.

## Основная структура JSON

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "version": 2,
  "createdAt": "2023-06-10T10:30:00Z",
  "updatedAt": "2023-06-25T14:30:00Z",
  "nodes": [...],
  "connections": [...],
  "groups": [...],
  "metadata": {...}
}
```

## Компоненты

### 1. Общие атрибуты дерева

| Атрибут    | Тип      | Описание                                      |
|------------|----------|-----------------------------------------------|
| id         | UUID     | Уникальный идентификатор дерева               |
| version    | Integer  | Версия структуры дерева                       |
| createdAt  | DateTime | Дата и время создания                         |
| updatedAt  | DateTime | Дата и время последнего обновления            |

### 2. Узлы (Nodes)

Узлы представляют собой отдельные элементы контента (уроки, модули, темы).

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "titleKey": "course.intro",
  "descriptionKey": "course.intro.description",
  "position": {"x": 100, "y": 150},
  "style": "circular",
  "contentId": "550e8400-e29b-41d4-a716-446655440010",
  "requirements": [],
  "status": "published",
  "type": "lesson",
  "metadata": {
    "estimatedTimeMinutes": 45,
    "difficultyLevel": "beginner",
    "order": 1,
    "tags": ["introduction", "basics"]
  },
  "visualAttributes": {
    "color": "#4A90E2",
    "icon": "book",
    "size": "medium"
  }
}
```

| Атрибут        | Тип    | Описание                                      |
|----------------|--------|-----------------------------------------------|
| id             | UUID   | Уникальный идентификатор узла                 |
| titleKey       | String | Ключ для локализации заголовка                |
| descriptionKey | String | Ключ для локализации описания (опционально)   |
| position       | Object | Координаты узла на канвасе (x, y)             |
| style          | String | Стиль отображения (circular, hexagon, square) |
| contentId      | UUID   | Ссылка на связанный контент                   |
| requirements   | Array  | Список ID узлов, требуемых для этого узла     |
| status         | String | Статус узла (published, draft, hidden)        |
| type           | String | Тип контента (lesson, quiz, assignment, etc.) |
| metadata       | Object | Дополнительные метаданные                     |
| visualAttributes | Object | Атрибуты для визуализации                   |

#### Типы узлов (type)

* `lesson` - стандартный урок
* `quiz` - тест или опрос
* `assignment` - задание для выполнения
* `project` - проектная работа
* `discussion` - обсуждение или форум
* `resource` - дополнительный ресурс
* `checkpoint` - контрольная точка

### 3. Соединения (Connections)

Соединения определяют связи между узлами.

```json
{
  "id": "conn-550e8400-e29b-41d4-a716-446655440001",
  "from": "550e8400-e29b-41d4-a716-446655440001",
  "to": "550e8400-e29b-41d4-a716-446655440002",
  "style": "solid_arrow",
  "type": "required",
  "visualAttributes": {
    "color": "#4A90E2",
    "thickness": 2,
    "dashPattern": [0]
  }
}
```

| Атрибут         | Тип    | Описание                                     |
|-----------------|--------|--------------------------------------------|
| id              | UUID   | Уникальный идентификатор соединения         |
| from            | UUID   | ID исходного узла                           |
| to              | UUID   | ID целевого узла                            |
| style           | String | Стиль линии (solid_arrow, dashed_line, etc.) |
| type            | String | Тип связи (required, recommended, optional) |
| visualAttributes | Object | Атрибуты для визуализации                  |

#### Типы соединений (type)

* `required` - обязательное прохождение предыдущего узла
* `recommended` - рекомендуемое прохождение предыдущего узла
* `optional` - опциональная связь без требований

### 4. Группы (Groups)

Группы позволяют объединять узлы в логические блоки (модули, темы).

```json
{
  "id": "group-550e8400-e29b-41d4-a716-446655440001",
  "nameKey": "course.module1",
  "nodes": ["550e8400-e29b-41d4-a716-446655440001", "550e8400-e29b-41d4-a716-446655440002"],
  "visualAttributes": {
    "color": "#E6F7FF",
    "position": {"x": 150, "y": 150},
    "collapsed": false,
    "border": {
      "color": "#4A90E2",
      "style": "dashed",
      "thickness": 1
    }
  }
}
```

| Атрибут         | Тип    | Описание                                     |
|-----------------|--------|--------------------------------------------|
| id              | UUID   | Уникальный идентификатор группы             |
| nameKey         | String | Ключ для локализации названия группы        |
| nodes           | Array  | Список ID узлов, входящих в группу          |
| visualAttributes | Object | Атрибуты для визуализации                  |

### 5. Метаданные (Metadata)

Общие метаданные о структуре дерева.

```json
{
  "defaultLanguage": "en",
  "availableLanguages": ["en", "ru", "fr"],
  "totalNodes": 12,
  "layoutType": "tree",
  "layoutDirection": "horizontal",
  "canvasSize": {
    "width": 1200,
    "height": 800
  }
}
```

| Атрибут           | Тип    | Описание                                     |
|-------------------|--------|--------------------------------------------|
| defaultLanguage   | String | Код языка по умолчанию                      |
| availableLanguages | Array | Список доступных языков                     |
| totalNodes        | Integer | Общее количество узлов в дереве            |
| layoutType        | String | Тип макета (tree, mesh, radial)            |
| layoutDirection   | String | Направление макета (horizontal, vertical)  |
| canvasSize        | Object | Размеры канваса для отображения            |

## Пример полной структуры

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "version": 2,
  "createdAt": "2023-06-10T10:30:00Z",
  "updatedAt": "2023-06-25T14:30:00Z",
  "nodes": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "titleKey": "course.intro",
      "descriptionKey": "course.intro.description",
      "position": {"x": 100, "y": 150},
      "style": "circular",
      "contentId": "550e8400-e29b-41d4-a716-446655440010",
      "requirements": [],
      "status": "published",
      "type": "lesson",
      "metadata": {
        "estimatedTimeMinutes": 45,
        "difficultyLevel": "beginner",
        "order": 1,
        "tags": ["introduction", "basics"]
      },
      "visualAttributes": {
        "color": "#4A90E2",
        "icon": "book",
        "size": "medium"
      }
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "titleKey": "course.basics",
      "descriptionKey": "course.basics.description",
      "position": {"x": 200, "y": 100},
      "style": "hexagon",
      "contentId": "550e8400-e29b-41d4-a716-446655440011",
      "requirements": ["550e8400-e29b-41d4-a716-446655440001"],
      "status": "published",
      "type": "lesson",
      "metadata": {
        "estimatedTimeMinutes": 60,
        "difficultyLevel": "beginner",
        "order": 2,
        "tags": ["basics", "fundamentals"]
      },
      "visualAttributes": {
        "color": "#50E3C2",
        "icon": "code",
        "size": "medium"
      }
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440003",
      "titleKey": "course.advanced",
      "descriptionKey": "course.advanced.description",
      "position": {"x": 200, "y": 200},
      "style": "square",
      "contentId": "550e8400-e29b-41d4-a716-446655440012",
      "requirements": ["550e8400-e29b-41d4-a716-446655440001"],
      "status": "published",
      "type": "lesson",
      "metadata": {
        "estimatedTimeMinutes": 90,
        "difficultyLevel": "intermediate",
        "order": 3,
        "tags": ["advanced", "concepts"]
      },
      "visualAttributes": {
        "color": "#F5A623",
        "icon": "star",
        "size": "medium"
      }
    }
  ],
  "connections": [
    {
      "id": "conn-550e8400-e29b-41d4-a716-446655440001",
      "from": "550e8400-e29b-41d4-a716-446655440001",
      "to": "550e8400-e29b-41d4-a716-446655440002",
      "style": "solid_arrow",
      "type": "required",
      "visualAttributes": {
        "color": "#4A90E2",
        "thickness": 2,
        "dashPattern": [0]
      }
    },
    {
      "id": "conn-550e8400-e29b-41d4-a716-446655440002",
      "from": "550e8400-e29b-41d4-a716-446655440001",
      "to": "550e8400-e29b-41d4-a716-446655440003",
      "style": "dashed_line",
      "type": "recommended",
      "visualAttributes": {
        "color": "#9B9B9B",
        "thickness": 1,
        "dashPattern": [5, 5]
      }
    }
  ],
  "groups": [
    {
      "id": "group-550e8400-e29b-41d4-a716-446655440001",
      "nameKey": "course.module1",
      "nodes": ["550e8400-e29b-41d4-a716-446655440001", "550e8400-e29b-41d4-a716-446655440002"],
      "visualAttributes": {
        "color": "#E6F7FF",
        "position": {"x": 150, "y": 150},
        "collapsed": false,
        "border": {
          "color": "#4A90E2",
          "style": "dashed",
          "thickness": 1
        }
      }
    }
  ],
  "metadata": {
    "defaultLanguage": "en",
    "availableLanguages": ["en", "ru", "fr"],
    "totalNodes": 3,
    "layoutType": "tree",
    "layoutDirection": "horizontal",
    "canvasSize": {
      "width": 1200,
      "height": 800
    }
  }
}
```

## Примечания по реализации

1. **Локализация**: Все текстовые элементы используют ключи локализации (`titleKey`, `descriptionKey`, `nameKey`), которые соответствуют записям в таблице локализаций.

2. **Визуализация**: Атрибуты визуализации (`visualAttributes`) обеспечивают гибкую настройку отображения элементов на схеме.

3. **Расширяемость**: Структура поддерживает добавление новых типов узлов, соединений и групп в будущем.

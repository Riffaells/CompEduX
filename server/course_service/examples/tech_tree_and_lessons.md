# Примеры работы с технологическим деревом и уроками

## Структура и взаимодействие компонентов

В системе курсов существует три основных компонента:

1. **Курсы (Courses)** - контейнеры для учебного материала
2. **Уроки (Lessons)** - учебные единицы с контентом
3. **Технологическое дерево (Technology Tree)** - визуальная структура для навигации по курсу

Взаимосвязь между ними следующая:
- Курс содержит множество уроков
- Курс имеет одно технологическое дерево
- Уроки могут быть связаны с узлами технологического дерева через поле `tree_node_id`
- Уроки могут содержать статьи (Articles) через связующую таблицу `lesson_article`

## Пример 1: Создание курса с технологическим деревом и уроками

### 1. Создание курса

```python
# POST /api/v1/courses/
course_data = {
    "title": "Введение в программирование на Python",
    "description": "Базовый курс по Python для начинающих",
    "slug": "intro-to-python",
    "language": "ru",
    "is_published": False
}

response = requests.post("http://localhost:8002/api/v1/courses/", json=course_data)
course = response.json()
course_id = course["id"]
```

### 2. Создание технологического дерева для курса

```python
# POST /api/v1/courses/{course_id}/technology-tree/
tree_data = {
    "course_id": course_id,
    "metadata": {
        "defaultLanguage": "ru",
        "availableLanguages": ["ru", "en"],
        "layoutType": "tree",
        "layoutDirection": "horizontal"
    },
    "nodes": [],
    "connections": [],
    "groups": []
}

response = requests.post(f"http://localhost:8002/api/v1/courses/{course_id}/technology-tree/", json=tree_data)
tree = response.json()
```

### 3. Добавление узлов в технологическое дерево

```python
# POST /api/v1/courses/{course_id}/technology-tree/nodes
node1_data = {
    "node_data": {
        "id": "node1",
        "titleKey": "python-basics",
        "title": {"ru": "Основы Python", "en": "Python Basics"},
        "description": {"ru": "Введение в язык Python", "en": "Introduction to Python language"},
        "position": {"x": 100, "y": 100},
        "style": "circular",
        "type": "module",
        "status": "published",
        "requirements": []
    }
}

response = requests.post(f"http://localhost:8002/api/v1/courses/{course_id}/technology-tree/nodes", json=node1_data)
node1 = response.json()

# Добавляем второй узел
node2_data = {
    "node_data": {
        "id": "node2",
        "titleKey": "variables",
        "title": {"ru": "Переменные", "en": "Variables"},
        "description": {"ru": "Работа с переменными в Python", "en": "Working with variables in Python"},
        "position": {"x": 250, "y": 100},
        "style": "circular",
        "type": "lesson",
        "status": "published",
        "requirements": ["node1"]
    }
}

response = requests.post(f"http://localhost:8002/api/v1/courses/{course_id}/technology-tree/nodes", json=node2_data)
node2 = response.json()
```

### 4. Создание уроков и связь с узлами дерева

```python
# POST /api/v1/lessons/
lesson1_data = {
    "course_id": course_id,
    "slug": "python-basics",
    "language": "ru",
    "title": "Основы Python",
    "description": "Введение в язык программирования Python",
    "content": "# Основы Python\n\nPython - это интерпретируемый язык программирования...",
    "order": 1,
    "duration": 30,
    "is_published": True,
    "tree_node_id": "node1"  # Связь с узлом дерева
}

response = requests.post("http://localhost:8002/api/v1/lessons/", json=lesson1_data)
lesson1 = response.json()

# Создаем второй урок
lesson2_data = {
    "course_id": course_id,
    "slug": "variables",
    "language": "ru",
    "title": "Переменные в Python",
    "description": "Изучаем работу с переменными",
    "content": "# Переменные в Python\n\nПеременные используются для хранения данных...",
    "order": 2,
    "duration": 25,
    "is_published": True,
    "tree_node_id": "node2"  # Связь с узлом дерева
}

response = requests.post("http://localhost:8002/api/v1/lessons/", json=lesson2_data)
lesson2 = response.json()
```

### 5. Создание статей и связь с уроками

```python
# POST /api/v1/articles/
article_data = {
    "course_id": course_id,
    "slug": "python-history",
    "language": "ru",
    "title": "История языка Python",
    "description": "Краткая история создания и развития Python",
    "content": "# История Python\n\nPython был создан Гвидо ван Россумом в конце 1980-х годов...",
    "order": 1,
    "is_published": True
}

response = requests.post("http://localhost:8002/api/v1/articles/", json=article_data)
article = response.json()

# Связываем статью с уроком
response = requests.post(f"http://localhost:8002/api/v1/lessons/{lesson1['id']}/articles/{article['id']}")
```

## Пример 2: Получение данных о технологическом дереве и уроках

### 1. Получение технологического дерева курса

```python
# GET /api/v1/courses/{course_id}/technology-tree/
response = requests.get(f"http://localhost:8002/api/v1/courses/{course_id}/technology-tree/")
tree = response.json()

# Получение дерева с локализацией на конкретном языке
response = requests.get(f"http://localhost:8002/api/v1/courses/{course_id}/technology-tree/?language=ru")
localized_tree = response.json()
```

### 2. Получение списка уроков курса

```python
# GET /api/v1/lessons/?course_id={course_id}
response = requests.get(f"http://localhost:8002/api/v1/lessons/?course_id={course_id}")
lessons = response.json()

# Получение уроков, связанных с конкретным узлом дерева
response = requests.get(f"http://localhost:8002/api/v1/lessons/?course_id={course_id}&tree_node_id=node1")
node_lessons = response.json()
```

### 3. Получение урока с полным контентом

```python
# GET /api/v1/lessons/{lesson_id}/content
response = requests.get(f"http://localhost:8002/api/v1/lessons/{lesson1['id']}/content")
lesson_with_content = response.json()

# В ответе будут:
# - Основные данные урока
# - Связанные статьи
# - Информация о связанном узле технологического дерева
```

## Пример 3: Отслеживание прогресса по урокам и технологическому дереву

Для отслеживания прогресса учащихся по курсу необходимо:

1. Создать таблицу `user_lesson_progress` для хранения прогресса по урокам
2. Создать таблицу `user_tree_node_progress` для хранения прогресса по узлам дерева

### Схема таблицы прогресса по урокам

```sql
CREATE TABLE user_lesson_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'not_started',  -- not_started, in_progress, completed
    progress_percent INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(user_id, lesson_id)
);
```

### Схема таблицы прогресса по узлам дерева

```sql
CREATE TABLE user_tree_node_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    node_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'locked',  -- locked, available, in_progress, completed
    unlocked_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(user_id, course_id, node_id)
);
```

### API для работы с прогрессом

```python
# Отметить урок как начатый
requests.post(f"http://localhost:8002/api/v1/lessons/{lesson_id}/progress/start", json={"user_id": user_id})

# Обновить прогресс по уроку
requests.put(f"http://localhost:8002/api/v1/lessons/{lesson_id}/progress", 
             json={"user_id": user_id, "progress_percent": 75})

# Отметить урок как завершенный
requests.post(f"http://localhost:8002/api/v1/lessons/{lesson_id}/progress/complete", 
              json={"user_id": user_id})

# Получить прогресс пользователя по всему курсу
requests.get(f"http://localhost:8002/api/v1/courses/{course_id}/progress?user_id={user_id}")
```

## Пример 4: Визуализация технологического дерева на клиенте

Для визуализации технологического дерева на клиенте можно использовать библиотеки:

1. **React Flow** - для React приложений
2. **D3.js** - для более сложных визуализаций
3. **Cytoscape.js** - для графовых структур

### Пример кода для React Flow

```jsx
import React, { useEffect, useState } from 'react';
import ReactFlow, { 
  Background, 
  Controls, 
  MiniMap,
  addEdge,
  useNodesState,
  useEdgesState
} from 'react-flow-renderer';

const TechnologyTreeViewer = ({ courseId }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  
  useEffect(() => {
    // Загрузка данных технологического дерева
    const fetchTechTree = async () => {
      const response = await fetch(`/api/v1/courses/${courseId}/technology-tree/`);
      const treeData = await response.json();
      
      // Преобразование узлов дерева в формат React Flow
      const flowNodes = Object.values(treeData.nodes).map(node => ({
        id: node.id,
        type: 'default',
        data: { 
          label: node.title_localized || node.title.en || Object.values(node.title)[0],
          nodeData: node
        },
        position: node.position,
        style: {
          background: node.visualAttributes?.color || '#4A90E2',
          width: 150,
          height: 50,
        }
      }));
      
      // Преобразование связей в формат React Flow
      const flowEdges = treeData.connections.map(conn => ({
        id: conn.id,
        source: conn.from,
        target: conn.to,
        animated: conn.type === 'recommended',
        style: { stroke: conn.visualAttributes?.color || '#888' }
      }));
      
      setNodes(flowNodes);
      setEdges(flowEdges);
    };
    
    fetchTechTree();
  }, [courseId]);
  
  return (
    <div style={{ height: 600 }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        fitView
      >
        <Background />
        <Controls />
        <MiniMap />
      </ReactFlow>
    </div>
  );
};

export default TechnologyTreeViewer;
```

## Заключение

Технологическое дерево и уроки в системе курсов тесно связаны между собой. Дерево предоставляет визуальную структуру для навигации по курсу, а уроки содержат учебный контент. Связь между ними осуществляется через поле `tree_node_id` в модели урока.

Для полноценной работы системы необходимо:

1. Создать курс
2. Создать технологическое дерево для курса
3. Добавить узлы в дерево
4. Создать уроки и связать их с узлами дерева
5. Реализовать систему отслеживания прогресса
6. Добавить визуализацию дерева на клиенте

Такой подход позволяет создавать гибкие и интерактивные учебные материалы с четкой структурой и логикой прохождения. 
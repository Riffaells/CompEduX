"""
Тест добавления статьи к существующему курсу
"""
import asyncio
import json
import sys
import os
import aiohttp

# Добавляем путь к корню проекта, чтобы найти модули
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

async def get_courses():
    """Получение списка курсов (первый курс будет использован для добавления статьи)"""
    url = "http://localhost:8000/api/v1/courses"
    params = {
        "page": 0,
        "size": 1,
        "sort_by": "created_at",
        "sort_order": "desc"
    }

    async with aiohttp.ClientSession() as session:
        async with session.get(url, params=params) as response:
            if response.status == 200:
                result = await response.json()
                if result['items']:
                    return result['items'][0]
    return None

async def add_article_to_course(course_id):
    """Добавление новой статьи к курсу"""
    # URL для добавления статьи по API эндпоинту articles
    url = f"http://localhost:8000/api/v1/courses/{course_id}/articles"

    # Данные для создания статьи - course_id не нужно отправлять, он уже в URL
    article_data = {
        "slug": "getting-started-python",
        "language": "ru",
        "title": "Начало работы с Python",
        "order": 1,
        "content": """
# Начало работы с Python

Python - это мощный язык программирования, который отличается простым синтаксисом и читаемостью кода.

## Установка Python

Чтобы начать работу с Python, вам нужно:

1. Скачать Python с официального сайта [python.org](https://python.org)
2. Установить Python, следуя инструкциям установщика
3. Проверить установку, выполнив в терминале:
   ```
   python --version
   ```

## Ваша первая программа

Создайте файл `hello.py` и добавьте следующий код:

```python
print("Привет, мир!")
```

Запустите программу:

```
python hello.py
```

## Основы синтаксиса Python

Python использует отступы для определения блоков кода:

```python
if 5 > 2:
    print("Пять больше двух!")
```

### Переменные

```python
x = 5
y = "Привет"
print(x)
print(y)
```
        """,
        "metadata": {
            "reading_time": 5,
            "difficulty": "beginner",
            "keywords": ["python", "installation", "hello world"]
        }
    }

    # Заголовки запроса
    headers = {
        "Authorization": "Bearer test_token",  # В реальной системе используйте настоящий токен
        "Content-Type": "application/json"
    }

    # Печатаем полный URL и данные для отладки
    print(f"URL запроса: {url}")
    print(f"Отправляемые данные: {json.dumps(article_data, ensure_ascii=False)[:100]}...")

    # Отправляем запрос на создание статьи
    async with aiohttp.ClientSession() as session:
        try:
            async with session.post(url, json=article_data, headers=headers) as response:
                status = response.status
                print(f"Статус ответа: {status}")

                if status == 201:
                    result = await response.json()
                    print("Статья успешно создана:")
                    print(json.dumps(result, indent=2, ensure_ascii=False))
                    return result
                else:
                    error = await response.text()
                    print(f"Ошибка при создании статьи: {error}")
                    print(f"Заголовки ответа: {response.headers}")
                    return None
        except Exception as e:
            print(f"Исключение при отправке запроса: {str(e)}")
            return None

async def main():
    # Получаем первый доступный курс
    print("Получение курса для добавления статьи...")
    course = await get_courses()

    if not course:
        print("Не удалось найти курсы для добавления статьи.")
        return

    print(f"Найден курс: {course['title'].get('ru', course['title'].get('en', 'Без названия'))}")
    print(f"ID курса: {course['id']}")
    print(f"Slug курса: {course['slug']}")

    # Статьи можно добавлять только по ID курса
    course_id = course['id']
    print(f"\nИспользуем ID курса: {course_id}")

    # Добавляем статью к курсу
    print("\nДобавление статьи к курсу...")
    article = await add_article_to_course(course_id)

    if article:
        print("\nСтатья успешно добавлена:")
        print(f"ID статьи: {article['id']}")
        print(f"Заголовок: {article['title']}")
        print(f"Slug: {article['slug']}")
        print(f"Язык: {article['language']}")
    else:
        print("Не удалось добавить статью к курсу.")

if __name__ == "__main__":
    asyncio.run(main())

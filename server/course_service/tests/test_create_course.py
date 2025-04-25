"""
Тест создания курса через API
"""
import asyncio
import json
import uuid
import sys
import os
import aiohttp

# Добавляем путь к корню проекта, чтобы найти модули
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

async def create_course():
    """Тестирование создания нового курса"""
    # URL сервиса курсов
    url = "http://localhost:8000/api/v1/courses"

    # Генерируем UUID для автора курса
    author_id = str(uuid.uuid4())

    # Подготавливаем данные для курса
    course_data = {
        "title": {
            "ru": "Введение в программирование Python",
            "en": "Introduction to Python Programming"
        },
        "description": {
            "ru": "Базовый курс по основам программирования на Python для начинающих",
            "en": "Basic Python programming course for beginners"
        },
        "author_id": author_id,
        "visibility": "PUBLIC",
        "is_published": True,
        "tags": ["python", "programming", "beginner"]
    }

    # Заголовки запроса
    headers = {
        "Authorization": "Bearer test_token",  # В реальной системе используйте настоящий токен
        "Content-Type": "application/json"
    }

    # Отправляем запрос на создание курса
    async with aiohttp.ClientSession() as session:
        async with session.post(url, json=course_data, headers=headers) as response:
            status = response.status
            print(f"Статус ответа: {status}")

            if status == 201:
                result = await response.json()
                print("Курс успешно создан:")
                print(json.dumps(result, indent=2, ensure_ascii=False))
                return result
            else:
                error = await response.text()
                print(f"Ошибка при создании курса: {error}")
                return None

async def main():
    print("Тестирование API создания курса")
    course = await create_course()

    if course:
        print(f"ID курса: {course['id']}")
        print(f"Slug курса: {course['slug']}")
        print(f"Доступные языки: {course['title'].keys()}")

if __name__ == "__main__":
    asyncio.run(main())

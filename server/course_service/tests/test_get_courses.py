"""
Тест получения списка курсов и информации об отдельном курсе
"""
import asyncio
import json
import os
import sys

import aiohttp

# Добавляем путь к корню проекта, чтобы найти модули
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))


async def get_courses():
    """Получение списка курсов"""
    # URL сервиса курсов
    url = "http://localhost:8000/api/v1/courses"

    # Параметры запроса
    params = {
        "page": 0,
        "size": 10,
        "sort_by": "created_at",
        "sort_order": "desc",
        "language": "ru"
    }

    # Отправляем запрос на получение списка курсов
    async with aiohttp.ClientSession() as session:
        async with session.get(url, params=params) as response:
            status = response.status
            print(f"Статус ответа: {status}")

            if status == 200:
                result = await response.json()
                print(f"Получено курсов: {len(result['items'])}")
                print(f"Всего курсов: {result['total']}")
                return result
            else:
                error = await response.text()
                print(f"Ошибка при получении курсов: {error}")
                return None


async def get_course_by_id(course_id):
    """Получение информации о конкретном курсе по ID"""
    # URL сервиса курсов
    url = f"http://localhost:8000/api/v1/courses/{course_id}"

    # Отправляем запрос на получение информации о курсе
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            status = response.status
            print(f"Статус ответа: {status}")

            if status == 200:
                result = await response.json()
                print("Информация о курсе:")
                print(json.dumps(result, indent=2, ensure_ascii=False))
                return result
            else:
                error = await response.text()
                print(f"Ошибка при получении курса: {error}")
                return None


async def get_course_by_slug(slug):
    """Получение информации о конкретном курсе по slug"""
    # URL сервиса курсов
    url = f"http://localhost:8000/api/v1/courses/{slug}"

    # Отправляем запрос на получение информации о курсе
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            status = response.status
            print(f"Статус ответа: {status}")

            if status == 200:
                result = await response.json()
                print("Информация о курсе:")
                print(json.dumps(result, indent=2, ensure_ascii=False))
                return result
            else:
                error = await response.text()
                print(f"Ошибка при получении курса: {error}")
                return None


async def main():
    print("Получение списка курсов")
    courses = await get_courses()

    if courses and courses['items']:
        # Получаем первый курс из списка
        first_course = courses['items'][0]

        # Выводим базовую информацию о первом курсе
        print(f"\nПервый курс в списке:")
        print(f"ID: {first_course['id']}")
        print(f"Название: {first_course['title'].get('ru', first_course['title'].get('en', 'Без названия'))}")
        print(f"Slug: {first_course['slug']}")

        # Получаем детальную информацию о курсе по ID
        print("\nПолучение информации о курсе по ID:")
        await get_course_by_id(first_course['id'])

        # Получаем детальную информацию о курсе по slug
        print("\nПолучение информации о курсе по slug:")
        await get_course_by_slug(first_course['slug'])
    else:
        print("Курсы не найдены или произошла ошибка при получении списка")


if __name__ == "__main__":
    asyncio.run(main())

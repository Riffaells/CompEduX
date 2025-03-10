# CompEduX Platform

Микросервисная платформа для проведения соревнований по программированию.

## Архитектура

Проект построен на микросервисной архитектуре с использованием FastAPI и Docker:

- **API Gateway** - центральный шлюз для маршрутизации запросов к микросервисам
- **Auth Service** - сервис аутентификации и авторизации
- **Room Service** (планируется) - сервис для управления комнатами соревнований
- **Competition Service** (планируется) - сервис для управления соревнованиями
- **Achievement Service** (планируется) - сервис для управления достижениями

## Требования

- Docker и Docker Compose
- Python 3.11+ (для локальной разработки)

## Запуск проекта

### 1. Настройка переменных окружения

Скопируйте файл `.env.example` в `.env` и настройте переменные окружения:

```bash
cp .env.example .env
```

Отредактируйте файл `.env` в соответствии с вашими потребностями.

### 2. Запуск с помощью скриптов

#### Режим разработки

В Linux/macOS:
```bash
chmod +x dev.sh
./dev.sh
```

В Windows:
```cmd
dev.bat
```

#### Продакшен режим

В Linux/macOS:
```bash
chmod +x prod.sh
./prod.sh
```

В Windows:
```cmd
prod.bat
```

### 3. Запуск вручную с Docker Compose

#### Режим разработки
```bash
docker-compose -f docker-compose.dev.yml up -d
```

#### Продакшен режим
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 4. Проверка работоспособности

API Gateway будет доступен по адресу: http://localhost:8000

Документация API: http://localhost:8000/docs

## Разработка

### Локальный запуск сервисов

Для локальной разработки можно запустить каждый сервис отдельно:

#### Auth Service

```bash
cd auth_service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

#### API Gateway

```bash
cd api_gateway
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### Миграции базы данных

Для создания и применения миграций используется Alembic:

```bash
cd auth_service
alembic revision --autogenerate -m "Initial migration"
alembic upgrade head
```

## API Endpoints

### Auth Service

- `POST /api/v1/auth/register` - Регистрация нового пользователя
- `POST /api/v1/auth/login` - Аутентификация пользователя
- `POST /api/v1/auth/refresh` - Обновление токена доступа
- `POST /api/v1/auth/logout` - Выход из системы
- `GET /api/v1/auth/me` - Получение информации о текущем пользователе

### Users API

- `GET /api/v1/users` - Получение списка пользователей (только для администраторов)
- `GET /api/v1/users/{user_id}` - Получение информации о пользователе
- `PATCH /api/v1/users/{user_id}` - Обновление информации о пользователе
- `DELETE /api/v1/users/{user_id}` - Удаление пользователя (только для администраторов)

## Лицензия

MIT

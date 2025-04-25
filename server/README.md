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

# CompEduX Database Initialization Tool

Скрипт для инициализации пользователей и баз данных PostgreSQL для микросервисной архитектуры CompEduX.

## Назначение

Этот скрипт решает следующие задачи:

1. **Централизованное создание пользователей БД**
    - Создание пользователей PostgreSQL для каждого микросервиса
    - Настройка прав доступа и привилегий

2. **Инициализация баз данных**
    - Создание баз данных с правильными настройками кодировки UTF-8
    - Назначение владельцев и привилегий

3. **Устранение проблем с кодировкой**
    - Установка `lc_messages='C'` для предотвращения ошибок с кириллическими сообщениями об ошибках
    - Выбор правильных параметров локали для баз данных

## Использование

### Требования

Для работы скрипта необходимы:

- Python 3.7 или выше
- Библиотека psycopg2 (`pip install psycopg2-binary`)

### Запуск

Простой запуск с параметрами по умолчанию:

```bash
python init_db.py
```

С указанием параметров подключения:

```bash
python init_db.py --host localhost --port 5432 --user postgres --password secure_password
```

### Параметры командной строки

| Параметр     | Описание                         | По умолчанию |
|--------------|----------------------------------|--------------|
| `--host`     | Хост PostgreSQL                  | localhost    |
| `--port`     | Порт PostgreSQL                  | 5432         |
| `--user`     | Имя администратора PostgreSQL    | postgres     |
| `--password` | Пароль администратора PostgreSQL | postgres     |
| `--retry`    | Количество попыток подключения   | 5            |
| `--timeout`  | Таймаут подключения в секундах   | 10           |

## Настройка сервисов

По умолчанию скрипт создает следующие сервисы:

1. **auth_service**
    - Пользователь: `auth_user`
    - База данных: `auth_db`

2. **course_service**
    - Пользователь: `course_user`
    - База данных: `course_db`

Для добавления новых сервисов отредактируйте массив `DEFAULT_SERVICES` в файле скрипта.

## Решение проблемы с кодировкой

Скрипт автоматически решает проблему с кириллическими сообщениями об ошибках PostgreSQL, которая вызывает ошибку:

```
UnicodeDecodeError: 'utf-8' codec can't decode byte 0xc2 in position 61: invalid continuation byte
```

Эта ошибка возникает, когда PostgreSQL с русской локалью возвращает сообщения об ошибках на русском языке,
закодированные в Windows-1251 (CP1251), а psycopg2 пытается интерпретировать их как UTF-8.

Скрипт устанавливает параметр `lc_messages='C'` в PostgreSQL, что переключает все сообщения на английский язык с ASCII
кодировкой.

## Примеры использования

### Локальный запуск

```bash
# С параметрами по умолчанию
python init_db.py

# С указанием хоста и пароля
python init_db.py --host localhost --password my_secure_password

# С указанием всех параметров
python init_db.py --host db.example.com --port 5432 --user admin --password secret --retry 3 --timeout 5
```

### Запуск в Docker-окружении

```bash
# Если PostgreSQL запущен в Docker
python init_db.py --host postgres --password postgres
```

### Использование в CI/CD пайплайне

```yaml
# Пример для GitLab CI
init_database:
  stage: deploy
  script:
    - pip install psycopg2-binary
    - python init_db.py --host $DB_HOST --user $DB_USER --password $DB_PASSWORD
  only:
    - master
```

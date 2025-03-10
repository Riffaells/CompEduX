FROM python:3.11-slim

WORKDIR /app

# Установка системных зависимостей для psycopg2
RUN apt-get update && apt-get install -y \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Установка зависимостей Python
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копирование кода приложения
COPY . .

# Запуск миграций и приложения
CMD alembic upgrade head && uvicorn app.main:app --host 0.0.0.0 --port 8000

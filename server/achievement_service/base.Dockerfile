FROM python:3.13

WORKDIR /app

# Установка зависимостей
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Переменная окружения для указания режима работы
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

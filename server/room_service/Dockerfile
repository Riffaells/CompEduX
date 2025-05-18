FROM python:3.10-slim

WORKDIR /app

# Установка зависимостей
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копирование кода приложения
COPY . .

# Создание директории для логов
RUN mkdir -p logs

# Установка переменных окружения
ENV PYTHONPATH=/app
ENV PYTHONUNBUFFERED=1
ENV HOST=0.0.0.0
ENV PORT=8003
ENV DOCKER_DELAY=true
ENV DOCKER_STARTUP_DELAY_SEC=2

# Открываем порт
EXPOSE 8003

# Запуск приложения
CMD ["python", "run.py"] 
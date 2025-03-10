FROM python:3.11-slim

WORKDIR /app

# Установка общих зависимостей
RUN pip install --no-cache-dir fastapi uvicorn pydantic pydantic-settings httpx

# Копирование кода проекта
COPY . .

# Установка зависимостей для каждого сервиса
RUN pip install --no-cache-dir -r auth_service/requirements.txt
RUN pip install --no-cache-dir -r api_gateway/requirements.txt

# Переменные окружения
ENV PYTHONPATH=/app

# Порт для API Gateway
EXPOSE 8000

# Запуск API Gateway по умолчанию
CMD ["uvicorn", "api_gateway.app.main:app", "--host", "0.0.0.0", "--port", "8000"]

FROM python:3.13-slim

WORKDIR /app

# Копируем только requirements.txt сначала для использования кеша Docker
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копируем остальные файлы
COPY . .

# Создаем непривилегированного пользователя
RUN adduser --disabled-password --gecos "" appuser
USER appuser

# Запускаем приложение
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]

FROM python:3.13 AS builder

WORKDIR /app

# Копируем только файл requirements.txt для установки зависимостей
COPY requirements.txt .

# Устанавливаем зависимости в отдельный слой
RUN pip install --no-cache-dir -r requirements.txt

# Второй этап сборки
FROM python:3.13-slim

WORKDIR /app

# Копируем установленные зависимости из первого этапа
COPY --from=builder /usr/local/lib/python3.13/site-packages /usr/local/lib/python3.13/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

# Переменная окружения для указания режима работы
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

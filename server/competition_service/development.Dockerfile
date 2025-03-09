FROM competition_service-base

WORKDIR /app

# Установка зависимостей
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Установка дополнительных инструментов для разработки
RUN pip install --no-cache-dir watchdog[watchmedo] pytest pytest-cov

# Не копируем код, так как он будет монтироваться как volume

# Запуск с автоматическим перезапуском при изменении файлов
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload", "--reload-dir", "/app", "--reload-dir", "/app/common"]

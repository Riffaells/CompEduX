FROM room_service-base

# Копируем код приложения
COPY . .

# Запуск без режима автоматической перезагрузки
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]

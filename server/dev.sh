#!/bin/bash

# Загрузка переменных окружения
echo "Загрузка переменных окружения из .env..."
export $(grep -v '^#' .env | xargs)

# Запуск в режиме разработки
echo "Запуск в режиме разработки..."
docker-compose -f docker-compose.dev.yml up -d

echo "Сервисы запущены:"
echo "API Gateway: http://localhost:${API_GATEWAY_PORT:-8000}"
echo "Auth Service: http://localhost:${AUTH_SERVICE_PORT:-8001}"
echo "Документация API: http://localhost:${API_GATEWAY_PORT:-8000}/docs"

#!/bin/bash

# Запуск в продакшен режиме
echo "Запуск в продакшен режиме..."
docker-compose -f docker-compose.prod.yml up -d

echo "Сервисы запущены:"
echo "API Gateway: http://localhost:${API_GATEWAY_PORT:-8000}"
echo "Auth Service: http://localhost:${AUTH_SERVICE_PORT:-8001}"
echo "Документация API: http://localhost:${API_GATEWAY_PORT:-8000}/docs"

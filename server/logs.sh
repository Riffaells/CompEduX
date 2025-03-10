#!/bin/bash

# Скрипт для просмотра логов контейнеров

if [ -z "$1" ]; then
    echo "Использование: ./logs.sh [service_name]"
    echo "Доступные сервисы:"
    echo "  postgres - База данных PostgreSQL"
    echo "  auth_service - Сервис аутентификации"
    echo "  api_gateway - API Gateway"
    echo "  all - Все сервисы"
    exit 1
fi

if [ "$1" == "all" ]; then
    docker-compose logs --tail=100 -f
else
    docker-compose logs --tail=100 -f "$1"
fi

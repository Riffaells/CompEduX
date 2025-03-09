#!/bin/bash

# Скрипт для просмотра логов сервисов

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Проверка наличия аргумента
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}Вывод логов всех сервисов. Нажмите Ctrl+C для выхода.${NC}"
    docker-compose logs -f
    exit 0
fi

SERVICE=$1

# Проверка существования сервиса
if ! docker-compose ps | grep -q $SERVICE; then
    echo -e "${RED}Ошибка: Сервис '$SERVICE' не найден или не запущен.${NC}"
    echo -e "Запущенные сервисы:"
    docker-compose ps --services
    exit 1
fi

echo -e "${YELLOW}Вывод логов сервиса '$SERVICE'. Нажмите Ctrl+C для выхода.${NC}"
docker-compose logs -f $SERVICE

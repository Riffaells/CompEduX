#!/bin/bash

# Скрипт для запуска тестов

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Проверка наличия аргумента
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}Запуск тестов для всех сервисов...${NC}"

    echo -e "${YELLOW}Запуск тестов для auth_service...${NC}"
    docker-compose exec auth_service pytest -v

    echo -e "${YELLOW}Запуск тестов для room_service...${NC}"
    docker-compose exec room_service pytest -v

    echo -e "${YELLOW}Запуск тестов для achievement_service...${NC}"
    docker-compose exec achievement_service pytest -v

    echo -e "${YELLOW}Запуск тестов для competition_service...${NC}"
    docker-compose exec competition_service pytest -v

    echo -e "${YELLOW}Запуск тестов для api_gateway...${NC}"
    docker-compose exec api_gateway pytest -v

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

echo -e "${YELLOW}Запуск тестов для сервиса '$SERVICE'...${NC}"

# Проверка наличия дополнительных аргументов для pytest
if [ $# -gt 1 ]; then
    ARGS="${@:2}"
    echo -e "${YELLOW}Дополнительные аргументы: $ARGS${NC}"
    docker-compose exec $SERVICE pytest $ARGS
else
    docker-compose exec $SERVICE pytest -v
fi

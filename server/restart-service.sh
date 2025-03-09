#!/bin/bash

# Скрипт для перезапуска отдельного сервиса

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Проверка наличия аргумента
if [ $# -eq 0 ]; then
    echo -e "${RED}Ошибка: Не указан сервис для перезапуска.${NC}"
    echo -e "Использование: $0 <service_name>"
    echo -e "Доступные сервисы:"
    echo -e "  - ${YELLOW}api_gateway${NC}"
    echo -e "  - ${YELLOW}auth_service${NC}"
    echo -e "  - ${YELLOW}room_service${NC}"
    echo -e "  - ${YELLOW}achievement_service${NC}"
    echo -e "  - ${YELLOW}competition_service${NC}"
    exit 1
fi

SERVICE=$1

# Проверка существования сервиса
if ! docker-compose ps | grep -q $SERVICE; then
    echo -e "${RED}Ошибка: Сервис '$SERVICE' не найден или не запущен.${NC}"
    echo -e "Запущенные сервисы:"
    docker-compose ps --services
    exit 1
fi

echo -e "${YELLOW}Перезапуск сервиса '$SERVICE'...${NC}"
docker-compose restart $SERVICE

echo -e "${GREEN}Сервис '$SERVICE' перезапущен.${NC}"
echo -e "${YELLOW}Вывод логов сервиса '$SERVICE'. Нажмите Ctrl+C для выхода.${NC}"
docker-compose logs -f $SERVICE

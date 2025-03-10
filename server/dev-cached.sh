#!/bin/bash

# Скрипт для запуска проекта в режиме разработки с использованием кэша Docker

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Запуск CompEduX в режиме разработки с использованием кэша...${NC}"

# Проверка наличия Docker и Docker Compose
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker не установлен. Пожалуйста, установите Docker.${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose не установлен. Пожалуйста, установите Docker Compose.${NC}"
    exit 1
fi

# Установка переменной окружения для режима разработки
export ENV=development

# Проверка наличия .env файла
if [ ! -f .env ]; then
    echo -e "${YELLOW}Файл .env не найден. Копирование из .env.example...${NC}"
    cp .env.example .env
    echo -e "${GREEN}Файл .env создан. Пожалуйста, проверьте настройки.${NC}"
fi

# Сборка базовых образов с использованием кэша
echo -e "${YELLOW}Сборка базовых образов с использованием кэша...${NC}"
docker-compose build auth_service-base room_service-base achievement_service-base competition_service-base api_gateway-base

# Запуск сервисов
echo -e "${GREEN}Запуск сервисов...${NC}"
docker-compose up -d

# Вывод информации о запущенных сервисах
echo -e "${GREEN}Сервисы запущены:${NC}"
echo -e "API Gateway: ${YELLOW}http://localhost:8000${NC}"
echo -e "Auth Service: ${YELLOW}http://localhost:8001${NC}"
echo -e "Room Service: ${YELLOW}http://localhost:8002${NC}"
echo -e "Achievement Service: ${YELLOW}http://localhost:8003${NC}"
echo -e "Competition Service: ${YELLOW}http://localhost:8004${NC}"
echo -e "PostgreSQL: ${YELLOW}localhost:5432${NC}"

echo -e "${GREEN}Для просмотра логов используйте:${NC} docker-compose logs -f [service_name]"
echo -e "${GREEN}Для остановки сервисов используйте:${NC} docker-compose down"
echo -e "${GREEN}Для перезапуска сервиса используйте:${NC} docker-compose restart [service_name]"

# Вывод логов всех сервисов
echo -e "${YELLOW}Вывод логов всех сервисов. Нажмите Ctrl+C для выхода.${NC}"
docker-compose logs -f

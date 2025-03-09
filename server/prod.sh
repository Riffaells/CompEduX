#!/bin/bash

# Скрипт для запуска проекта в production режиме

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Запуск CompEduX в production режиме...${NC}"

# Проверка наличия Docker и Docker Compose
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker не установлен. Пожалуйста, установите Docker.${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}Docker Compose не установлен. Пожалуйста, установите Docker Compose.${NC}"
    exit 1
fi

# Установка переменной окружения для production режима
export ENV=production

# Проверка наличия .env файла
if [ ! -f .env ]; then
    echo -e "${YELLOW}Файл .env не найден. Копирование из .env.example...${NC}"
    cp .env.example .env
    echo -e "${GREEN}Файл .env создан. Пожалуйста, проверьте настройки.${NC}"
    echo -e "${RED}ВНИМАНИЕ: Обязательно обновите секретные ключи и пароли в .env файле перед запуском в production!${NC}"
    exit 1
fi

# Сборка базовых образов
echo -e "${YELLOW}Сборка базовых образов...${NC}"
docker-compose build auth_service-base room_service-base achievement_service-base competition_service-base api_gateway-base

# Запуск сервисов
echo -e "${GREEN}Запуск сервисов в production режиме...${NC}"
docker-compose up -d

# Вывод информации о запущенных сервисах
echo -e "${GREEN}Сервисы запущены:${NC}"
echo -e "API Gateway: ${YELLOW}http://localhost:8000${NC}"
echo -e "${GREEN}Для просмотра логов используйте:${NC} docker-compose logs -f [service_name]"
echo -e "${GREEN}Для остановки сервисов используйте:${NC} docker-compose down"

# Проверка статуса сервисов
echo -e "${YELLOW}Проверка статуса сервисов...${NC}"
docker-compose ps

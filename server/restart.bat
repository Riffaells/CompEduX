@echo off
chcp 65001 > nul

echo Остановка и удаление контейнеров...
docker-compose down

echo Удаление томов PostgreSQL...
docker volume rm server_postgres_data || echo Том не найден, продолжаем...

echo Очистка Docker кэша...
docker system prune -f

echo Запуск в режиме разработки...
docker-compose -f docker-compose.dev.yml up -d

echo.
echo Просмотр логов auth_service...
docker-compose logs -f auth_service

echo.
echo Сервисы запущены:
echo API Gateway: http://localhost:8000
echo Auth Service: http://localhost:8001
echo Документация API: http://localhost:8000/docs
echo.

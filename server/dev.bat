@echo off
chcp 65001 > nul

echo Запуск в режиме разработки...
docker-compose -f docker-compose.dev.yml up -d

echo.
echo Сервисы запущены:
echo API Gateway: http://localhost:8000
echo Auth Service: http://localhost:8001
echo Документация API: http://localhost:8000/docs
echo.

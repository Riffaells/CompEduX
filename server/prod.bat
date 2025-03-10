@echo off
chcp 65001 > nul

echo Загрузка переменных окружения из .env...
for /f "tokens=*" %%a in (.env) do (
    set "%%a"
)

echo Запуск в продакшен режиме...
docker-compose -f docker-compose.prod.yml up -d

echo.
echo Сервисы запущены:
echo API Gateway: http://localhost:%API_GATEWAY_PORT%
echo Auth Service: http://localhost:%AUTH_SERVICE_PORT%
echo Документация API: http://localhost:%API_GATEWAY_PORT%/docs
echo.

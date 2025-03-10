@echo off
chcp 65001 > nul

echo Проверка статуса сервисов...
docker-compose ps

echo.
echo Для просмотра логов используйте:
echo logs.bat postgres - для логов PostgreSQL
echo logs.bat auth_service - для логов Auth Service
echo logs.bat api_gateway - для логов API Gateway
echo.

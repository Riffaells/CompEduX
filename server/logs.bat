@echo off
chcp 65001 > nul

if "%1"=="" (
    echo Использование: logs.bat [service_name]
    echo Доступные сервисы:
    echo   postgres - База данных PostgreSQL
    echo   auth_service - Сервис аутентификации
    echo   api_gateway - API Gateway
    echo   all - Все сервисы
    exit /b
)

if "%1"=="all" (
    docker-compose logs -f
) else (
    docker-compose logs -f %1
)

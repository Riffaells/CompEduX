# Скрипт для запуска проекта в режиме разработки с использованием кэша Docker (Windows)

Write-Host "Запуск CompEduX в режиме разработки с использованием кэша..." -ForegroundColor Green

# Проверка наличия Docker
try {
    docker --version | Out-Null
} catch {
    Write-Host "Docker не установлен. Пожалуйста, установите Docker Desktop для Windows." -ForegroundColor Red
    exit 1
}

# Проверка наличия Docker Compose
try {
    docker-compose --version | Out-Null
} catch {
    Write-Host "Docker Compose не установлен. Пожалуйста, установите Docker Desktop для Windows." -ForegroundColor Red
    exit 1
}

# Установка переменной окружения для режима разработки
$env:ENV = "development"

# Проверка наличия .env файла
if (-not (Test-Path .env)) {
    Write-Host "Файл .env не найден. Копирование из .env.example..." -ForegroundColor Yellow
    Copy-Item .env.example .env
    Write-Host "Файл .env создан. Пожалуйста, проверьте настройки." -ForegroundColor Green
}

# Сборка базовых образов с использованием кэша
Write-Host "Сборка базовых образов с использованием кэша..." -ForegroundColor Yellow
docker-compose build auth_service-base room_service-base achievement_service-base competition_service-base api_gateway-base

# Запуск сервисов
Write-Host "Запуск сервисов..." -ForegroundColor Green
docker-compose up -d

# Вывод информации о запущенных сервисах
Write-Host "Сервисы запущены:" -ForegroundColor Green
Write-Host "API Gateway: http://localhost:8000" -ForegroundColor Yellow
Write-Host "Auth Service: http://localhost:8001" -ForegroundColor Yellow
Write-Host "Room Service: http://localhost:8002" -ForegroundColor Yellow
Write-Host "Achievement Service: http://localhost:8003" -ForegroundColor Yellow
Write-Host "Competition Service: http://localhost:8004" -ForegroundColor Yellow
Write-Host "PostgreSQL: localhost:5432" -ForegroundColor Yellow

Write-Host "Для просмотра логов используйте: docker-compose logs -f [service_name]" -ForegroundColor Green
Write-Host "Для остановки сервисов используйте: docker-compose down" -ForegroundColor Green
Write-Host "Для перезапуска сервиса используйте: docker-compose restart [service_name]" -ForegroundColor Green

# Вывод логов всех сервисов
Write-Host "Вывод логов всех сервисов. Нажмите Ctrl+C для выхода." -ForegroundColor Yellow
docker-compose logs -f

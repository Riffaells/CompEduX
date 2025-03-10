@echo off
chcp 65001 > nul

echo Остановка и удаление контейнеров...

if "%1"=="--volumes" (
    echo Удаление контейнеров вместе с томами...
    docker-compose down -v
) else if "%1"=="-v" (
    echo Удаление контейнеров вместе с томами...
    docker-compose down -v
) else (
    echo Удаление контейнеров (тома сохраняются)...
    docker-compose down
)

echo Готово!

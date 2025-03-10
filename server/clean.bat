@echo off
chcp 65001 > nul

echo Остановка и удаление контейнеров...
docker-compose down

echo Удаление всех томов...
docker volume prune -f

echo Удаление всех образов...
docker image prune -a -f

echo Очистка Docker кэша...
docker system prune -f

echo Готово! Все контейнеры, образы и тома удалены.
echo Теперь вы можете запустить проект заново с помощью команды dev.bat

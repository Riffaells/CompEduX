#!/bin/bash
set -e

# Функция для создания пользователей и баз данных
create_user_and_database() {
    local database=$1
    local user=$2
    local password=$3

    echo "Creating user '$user' and database '$database'"

    # Создаем пользователя, если он не существует
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        DO
        \$\$
        BEGIN
            IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = $user) THEN
                CREATE USER $user WITH PASSWORD '$password';
            END IF;
        END
        \$\$;
EOSQL

    # Создаем базу данных, если она не существует
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        SELECT 'CREATE DATABASE $database' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
        GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
EOSQL
}

# Создаем базы данных и пользователей для каждого сервиса
if [ -n "$AUTH_DB_NAME" ] && [ -n "$AUTH_DB_USER" ] && [ -n "$AUTH_DB_PASSWORD" ]; then
    create_user_and_database "$AUTH_DB_NAME" "$AUTH_DB_USER" "$AUTH_DB_PASSWORD"
fi

if [ -n "$ROOM_DB_NAME" ] && [ -n "$ROOM_DB_USER" ] && [ -n "$ROOM_DB_PASSWORD" ]; then
    create_user_and_database "$ROOM_DB_NAME" "$ROOM_DB_USER" "$ROOM_DB_PASSWORD"
fi

if [ -n "$COMPETITION_DB_NAME" ] && [ -n "$COMPETITION_DB_USER" ] && [ -n "$COMPETITION_DB_PASSWORD" ]; then
    create_user_and_database "$COMPETITION_DB_NAME" "$COMPETITION_DB_USER" "$COMPETITION_DB_PASSWORD"
fi

if [ -n "$ACHIEVEMENT_DB_NAME" ] && [ -n "$ACHIEVEMENT_DB_USER" ] && [ -n "$ACHIEVEMENT_DB_PASSWORD" ]; then
    create_user_and_database "$ACHIEVEMENT_DB_NAME" "$ACHIEVEMENT_DB_USER" "$ACHIEVEMENT_DB_PASSWORD"
fi

echo "Database initialization completed"

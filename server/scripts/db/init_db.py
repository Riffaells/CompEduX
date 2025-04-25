#!/usr/bin/env python
"""
CompEduX Database Initialization Script

Скрипт для инициализации пользователей и баз данных PostgreSQL
для микросервисов CompEduX.
"""
import argparse
import os
import platform
import subprocess
import sys
import time
from typing import Dict, List, Optional, Tuple, Any

from dotenv import load_dotenv

# Добавляем путь к корневой директории проекта
ROOT_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, ROOT_DIR)

import psycopg2
from psycopg2 import sql
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

# Инициализируем логгер из общего модуля
from common.logger import initialize_logging

logger = initialize_logging("db-init", log_file="logs/db_init.log")

# Константы в зависимости от ОС
WINDOWS_LOCALE = "C"  # На Windows используем просто "C" локаль
UNIX_LOCALE = "C.UTF-8"  # На UNIX используем "C.UTF-8"


class DatabaseInitializer:
    """Класс для инициализации баз данных и пользователей PostgreSQL"""

    def __init__(self, connection_params: Dict[str, Any], services: List[Dict[str, str]], retry_count: int = 5):
        """
        Инициализация объекта DatabaseInitializer

        Args:
            connection_params: Параметры подключения к PostgreSQL
            services: Список сервисов для инициализации
            retry_count: Количество попыток подключения
        """
        self.connection_params = connection_params
        self.services = services
        self.logger = logger
        self.retry_count = retry_count
        self.is_windows = platform.system() == "Windows"
        self.locale = WINDOWS_LOCALE if self.is_windows else UNIX_LOCALE

    @classmethod
    def from_env(cls, env_path: str = None) -> 'DatabaseInitializer':
        """
        Создает экземпляр класса на основе переменных окружения

        Args:
            env_path: Путь к файлу .env

        Returns:
            Экземпляр DatabaseInitializer
        """
        # Определение пути к .env файлу
        if env_path is None:
            env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '.env')

        # Загрузка переменных окружения
        if os.path.exists(env_path):
            load_dotenv(env_path)
            logger.info(f"Загружены настройки из {env_path}")
        else:
            logger.warning(f"Файл .env не найден в {env_path}. Используются значения по умолчанию.")

        # Чтение параметров командной строки
        parser = argparse.ArgumentParser(description="Инициализация БД для микросервисов CompEduX")
        parser.add_argument("--host", default=os.getenv("POSTGRES_HOST", "localhost"),
                            help="Хост PostgreSQL")
        parser.add_argument("--port", default=os.getenv("POSTGRES_PORT", "5432"),
                            help="Порт PostgreSQL")
        parser.add_argument("--user", default=os.getenv("POSTGRES_ADMIN_USER", "postgres"),
                            help="Имя администратора PostgreSQL")
        parser.add_argument("--password", default=os.getenv("POSTGRES_ADMIN_PASSWORD", "postgres"),
                            help="Пароль администратора PostgreSQL")
        parser.add_argument("--retry", type=int, default=int(os.getenv("RETRY_COUNT", "5")),
                            help="Количество попыток подключения")
        parser.add_argument("--timeout", type=int, default=int(os.getenv("CONNECT_TIMEOUT", "10")),
                            help="Таймаут подключения в секундах")
        args = parser.parse_args()

        # Формирование параметров подключения
        connection_params = {
            "host": args.host,
            "port": args.port,
            "user": args.user,
            "password": args.password,
            "dbname": "postgres",
            "connect_timeout": args.timeout,
            "client_encoding": "UTF8"  # Используем UTF-8 для поддержки русских сообщений
        }

        # Чтение конфигурации сервисов из переменных окружения
        services = [
            {
                "name": "auth_service",
                "user": os.getenv("AUTH_SERVICE_USER", "auth_user"),
                "password": os.getenv("AUTH_SERVICE_PASSWORD", "authpassword123"),
                "db": os.getenv("AUTH_SERVICE_DB", "auth_db"),
            },
            {
                "name": "course_service",
                "user": os.getenv("COURSE_SERVICE_USER", "course_user"),
                "password": os.getenv("COURSE_SERVICE_PASSWORD", "coursepassword123"),
                "db": os.getenv("COURSE_SERVICE_DB", "course_db"),
            },
            # При необходимости добавьте здесь другие сервисы
        ]

        return cls(connection_params, services, args.retry)

    def _get_connection(self, dbname: Optional[str] = None) -> Tuple[Dict[str, Any], psycopg2.extensions.connection]:
        """
        Создает подключение к базе данных

        Args:
            dbname: Имя базы данных (если None, используется значение из connection_params)

        Returns:
            Tuple[dict, connection]: Параметры подключения и объект соединения
        """
        conn_params = self.connection_params.copy()

        if dbname:
            conn_params["dbname"] = dbname

        return conn_params, psycopg2.connect(**conn_params)

    def check_connection(self) -> bool:
        """
        Проверка соединения с PostgreSQL

        Returns:
            bool: True, если подключение успешно, иначе False
        """
        try:
            conn_params, conn = self._get_connection()
            with conn:
                with conn.cursor() as cursor:
                    cursor.execute("SELECT version();")
                    version = cursor.fetchone()[0]
                    self.logger.info(f"Подключение к PostgreSQL успешно: {version}")

                    # Проверка кодировки
                    cursor.execute("SHOW server_encoding;")
                    encoding = cursor.fetchone()[0]
                    self.logger.info(f"Кодировка сервера: {encoding}")

                    # Проверка доступных локалей
                    if not self.is_windows:
                        try:
                            cursor.execute("SHOW lc_collate;")
                            collate = cursor.fetchone()[0]
                            self.logger.info(f"Текущая локаль LC_COLLATE: {collate}")
                        except:
                            pass
            return True
        except Exception as e:
            self.logger.error(f"Ошибка подключения к PostgreSQL: {e}")
            # Если ошибка связана с аутентификацией, выводим полезное сообщение
            error_str = str(e)
            if "password authentication failed" in error_str:
                self.logger.error("Ошибка аутентификации. Проверьте правильность имени пользователя и пароля.")
            elif "does not exist" in error_str and "role" in error_str:
                self.logger.error("Указанная роль не существует. Проверьте имя пользователя.")
            elif "could not connect to server" in error_str:
                self.logger.error("Не удалось подключиться к серверу. Проверьте, что PostgreSQL запущен и доступен.")
            return False

    def user_exists(self, username: str) -> bool:
        """
        Проверка существования пользователя

        Args:
            username: Имя пользователя

        Returns:
            bool: True, если пользователь существует, иначе False
        """
        try:
            conn_params, conn = self._get_connection()
            with conn:
                with conn.cursor() as cursor:
                    cursor.execute(
                        "SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = %s",
                        (username,)
                    )
                    return cursor.fetchone() is not None
        except Exception as e:
            self.logger.error(f"Ошибка проверки пользователя {username}: {e}")
            return False

    def database_exists(self, db_name: str) -> bool:
        """
        Проверка существования базы данных

        Args:
            db_name: Имя базы данных

        Returns:
            bool: True, если база данных существует, иначе False
        """
        try:
            conn_params, conn = self._get_connection()
            with conn:
                with conn.cursor() as cursor:
                    cursor.execute(
                        "SELECT 1 FROM pg_database WHERE datname = %s",
                        (db_name,)
                    )
                    return cursor.fetchone() is not None
        except Exception as e:
            self.logger.error(f"Ошибка проверки БД {db_name}: {e}")
            return False

    def create_or_update_user(self, username: str, password: str) -> bool:
        """
        Создание или обновление пользователя PostgreSQL

        Args:
            username: Имя пользователя
            password: Пароль пользователя

        Returns:
            bool: True, если пользователь создан/обновлен успешно, иначе False
        """
        try:
            conn_params, conn = self._get_connection()
            with conn:
                conn.autocommit = True
                with conn.cursor() as cursor:
                    if self.user_exists(username):
                        # Обновление существующего пользователя
                        cursor.execute(
                            sql.SQL("ALTER USER {} WITH PASSWORD %s").format(
                                sql.Identifier(username)
                            ),
                            (password,)
                        )
                        self.logger.info(f"Обновлен пароль для пользователя: {username}")
                    else:
                        # Создание нового пользователя
                        cursor.execute(
                            sql.SQL("CREATE USER {} WITH PASSWORD %s").format(
                                sql.Identifier(username)
                            ),
                            (password,)
                        )
                        self.logger.info(f"Создан пользователь: {username}")
                    return True
        except Exception as e:
            self.logger.error(f"Ошибка создания/обновления пользователя {username}: {e}")
            return False

    def create_database_using_psql(self, db_name: str, owner: str) -> bool:
        """
        Создание базы данных с использованием внешней утилиты psql

        Args:
            db_name: Имя базы данных
            owner: Владелец базы данных

        Returns:
            bool: True, если база данных создана успешно, иначе False
        """
        if self.is_windows:
            self.logger.warning("Метод psql не используется на Windows")
            return False

        try:
            # Формируем SQL-запрос
            sql_command = f"""
            CREATE DATABASE {db_name}
            OWNER {owner}
            ENCODING 'UTF8'
            LC_COLLATE '{self.locale}'
            LC_CTYPE '{self.locale}'
            TEMPLATE template0;
            """

            # Формируем команду psql
            cmd = [
                "psql",
                f"--host={self.connection_params['host']}",
                f"--port={self.connection_params['port']}",
                f"--username={self.connection_params['user']}",
                "--dbname=postgres",
                "-c", sql_command
            ]

            # Устанавливаем переменную окружения для пароля
            env = os.environ.copy()
            env["PGPASSWORD"] = self.connection_params["password"]

            # Запускаем команду
            process = subprocess.run(
                cmd,
                env=env,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )

            if process.returncode == 0:
                self.logger.info(f"Создана база данных: {db_name} с владельцем {owner} (через psql)")
                return True
            else:
                error = process.stderr.strip()
                if "already exists" in error:
                    self.logger.info(f"База данных {db_name} уже существует")
                    return True
                else:
                    self.logger.error(f"Ошибка создания базы данных через psql: {error}")
                    return False

        except Exception as e:
            self.logger.error(f"Ошибка выполнения psql: {e}")
            return False

    def create_database(self, db_name: str, owner: str) -> bool:
        """
        Создание базы данных PostgreSQL

        Args:
            db_name: Имя базы данных
            owner: Владелец базы данных

        Returns:
            bool: True, если база данных создана успешно, иначе False
        """
        # Сначала проверяем, существует ли база данных
        if self.database_exists(db_name):
            self.logger.info(f"База данных {db_name} уже существует")

            # Назначаем привилегии на существующую базу данных
            try:
                conn_params, conn = self._get_connection()
                with conn:
                    conn.autocommit = True
                    with conn.cursor() as cursor:
                        cursor.execute(
                            sql.SQL("GRANT ALL PRIVILEGES ON DATABASE {} TO {}").format(
                                sql.Identifier(db_name),
                                sql.Identifier(owner)
                            )
                        )

                # Пробуем подключиться к этой базе данных для установки привилегий на схему
                self.setup_schema_privileges(db_name, owner)

            except Exception as e:
                self.logger.warning(f"Ошибка при назначении привилегий для существующей БД {db_name}: {e}")

            return True

        # Если мы на Windows, сразу пробуем SQL метод
        # На Linux можно попробовать psql (в Windows утилит обычно нет)
        if not self.is_windows and self.create_database_using_psql(db_name, owner):
            # Устанавливаем привилегии на схему
            return self.setup_schema_privileges(db_name, owner)
        else:
            # Пробуем SQL метод напрямую
            return self.create_database_using_sql(db_name, owner)

    def create_database_using_sql(self, db_name: str, owner: str) -> bool:
        """
        Создание базы данных с использованием SQL-запросов

        Args:
            db_name: Имя базы данных
            owner: Владелец базы данных

        Returns:
            bool: True, если база данных создана успешно, иначе False
        """
        try:
            # Важно: CREATE DATABASE не может выполняться внутри транзакции
            # Поэтому устанавливаем autocommit=True на уровне соединения
            conn_params, conn = self._get_connection()

            # Устанавливаем autocommit напрямую на уровне соединения
            conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)

            # Определяем локаль для использования в зависимости от ОС
            locale_collate = self.locale
            locale_ctype = self.locale

            self.logger.info(f"Используем локаль для создания БД: {locale_collate}")

            with conn.cursor() as cursor:
                # Создание базы данных с заданными параметрами
                cursor.execute(
                    sql.SQL("""
                    CREATE DATABASE {}
                        OWNER {}
                        ENCODING 'UTF8'
                        LC_COLLATE '{}'
                        LC_CTYPE '{}'
                        TEMPLATE template0
                    """).format(
                        sql.Identifier(db_name),
                        sql.Identifier(owner),
                        sql.SQL(locale_collate),
                        sql.SQL(locale_ctype)
                    )
                )
                self.logger.info(f"Создана база данных: {db_name} с владельцем {owner}")

                # Назначение привилегий
                cursor.execute(
                    sql.SQL("GRANT ALL PRIVILEGES ON DATABASE {} TO {}").format(
                        sql.Identifier(db_name),
                        sql.Identifier(owner)
                    )
                )

            # Закрываем соединение
            conn.close()

            # Устанавливаем привилегии на схему
            return self.setup_schema_privileges(db_name, owner)

        except Exception as e:
            error_message = str(e)
            self.logger.error(f"Ошибка создания базы данных SQL методом: {error_message}")

            # Если ошибка связана с локалью, пробуем другие локали
            if "неверное имя локали" in error_message or "invalid locale name" in error_message:
                return self.try_alternative_locales(db_name, owner)

            # Проверяем, существует ли база данных, несмотря на ошибку
            if self.database_exists(db_name):
                self.logger.info(f"База данных {db_name} была создана, несмотря на ошибку SQL метода")
                return self.setup_schema_privileges(db_name, owner)

            return False

    def try_alternative_locales(self, db_name: str, owner: str) -> bool:
        """
        Попытка создать базу данных с альтернативными локалями

        Args:
            db_name: Имя базы данных
            owner: Владелец базы данных

        Returns:
            bool: True, если база данных создана успешно, иначе False
        """
        # Список локалей для попытки создания базы данных
        alternative_locales = ["C", "en_US.UTF-8", "en_US.utf8", "English_United States.1252"]

        for locale in alternative_locales:
            if locale == self.locale:
                continue  # Пропускаем локаль, которую уже пробовали

            self.logger.info(f"Пробуем создать базу данных с альтернативной локалью: {locale}")

            try:
                conn_params, conn = self._get_connection()
                conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)

                with conn.cursor() as cursor:
                    cursor.execute(
                        sql.SQL("""
                        CREATE DATABASE {}
                            OWNER {}
                            ENCODING 'UTF8'
                            LC_COLLATE '{}'
                            LC_CTYPE '{}'
                            TEMPLATE template0
                        """).format(
                            sql.Identifier(db_name),
                            sql.Identifier(owner),
                            sql.SQL(locale),
                            sql.SQL(locale)
                        )
                    )
                    self.logger.info(f"Создана база данных: {db_name} с владельцем {owner} и локалью {locale}")

                    cursor.execute(
                        sql.SQL("GRANT ALL PRIVILEGES ON DATABASE {} TO {}").format(
                            sql.Identifier(db_name),
                            sql.Identifier(owner)
                        )
                    )

                conn.close()
                return self.setup_schema_privileges(db_name, owner)

            except Exception as e:
                self.logger.warning(f"Не удалось создать базу данных с локалью {locale}: {e}")

        self.logger.error("Все попытки создания базы данных с различными локалями завершились неудачно")
        return False

    def setup_schema_privileges(self, db_name: str, owner: str) -> bool:
        """
        Настройка привилегий на схему базы данных

        Args:
            db_name: Имя базы данных
            owner: Владелец базы данных

        Returns:
            bool: True, если привилегии установлены успешно, иначе False
        """
        try:
            # Подключение к базе данных для установки привилегий на схему
            conn_params, conn = self._get_connection(db_name)
            with conn:
                conn.autocommit = True
                with conn.cursor() as cursor:
                    cursor.execute(
                        sql.SQL("GRANT ALL PRIVILEGES ON SCHEMA public TO {}").format(
                            sql.Identifier(owner)
                        )
                    )
                    cursor.execute(
                        sql.SQL("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {}").format(
                            sql.Identifier(owner)
                        )
                    )
                    cursor.execute(
                        sql.SQL("GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO {}").format(
                            sql.Identifier(owner)
                        )
                    )

            self.logger.info(f"Назначены привилегии для {owner} на базу данных {db_name}")
            return True

        except Exception as e:
            self.logger.warning(f"Ошибка настройки привилегий схемы для {db_name}: {e}")
            # Возвращаем True, так как база данных уже создана
            return True

    def setup_service(self, service: Dict[str, str]) -> bool:
        """
        Настройка БД для сервиса

        Args:
            service: Словарь с настройками сервиса

        Returns:
            bool: True, если настройка выполнена успешно, иначе False
        """
        self.logger.info(f"Настройка {service['name']}...")

        # Создание пользователя
        user_success = self.create_or_update_user(
            service["user"],
            service["password"]
        )
        if not user_success:
            self.logger.error(f"Не удалось создать пользователя для {service['name']}")
            return False

        # Создание базы данных
        db_success = self.create_database(
            service["db"],
            service["user"]
        )
        if not db_success:
            self.logger.error(f"Не удалось создать базу данных для {service['name']}")
            return False

        self.logger.info(f"Настройка {service['name']} завершена успешно")
        return True

    def setup_all_services(self) -> bool:
        """
        Настройка всех сервисов

        Returns:
            bool: True, если все сервисы настроены успешно, иначе False
        """
        all_success = True

        # Настройка каждого сервиса
        for service in self.services:
            success = self.setup_service(service)
            if not success:
                all_success = False
                self.logger.error(f"Не удалось настроить {service['name']}")
            else:
                self.logger.info(f"{service['name']} настроен успешно")

        return all_success

    def initialize(self) -> bool:
        """
        Основной метод инициализации БД

        Returns:
            bool: True, если инициализация выполнена успешно, иначе False
        """
        connection_info = self.connection_params.copy()
        if 'password' in connection_info:
            connection_info['password'] = '******'  # Маскируем пароль в логах

        self.logger.info("Инициализация баз данных для CompEduX...")
        self.logger.info(f"Параметры подключения: {connection_info}")
        self.logger.info(f"Операционная система: {platform.system()}")
        self.logger.info(f"Используемая локаль: {self.locale}")

        # Вывод информации о сервисах
        self.logger.info("Список сервисов для инициализации:")
        for service in self.services:
            service_info = service.copy()
            if 'password' in service_info:
                service_info['password'] = '******'  # Маскируем пароль в логах
            self.logger.info(f"  - {service_info['name']}: {service_info}")

        # Проверка подключения с повторными попытками
        connected = False
        retry_count = 0

        while not connected and retry_count < self.retry_count:
            connected = self.check_connection()
            if not connected:
                retry_count += 1
                wait_time = retry_count * 2
                self.logger.warning(
                    f"Не удалось подключиться к PostgreSQL. Повторная попытка через {wait_time} секунд... ({retry_count}/{self.retry_count})")
                time.sleep(wait_time)

        if not connected:
            self.logger.error("Не удалось подключиться к PostgreSQL после нескольких попыток. Выход.")
            return False

        # Настройка всех сервисов
        success = self.setup_all_services()

        if success:
            self.logger.info("Инициализация баз данных CompEduX завершена успешно!")
        else:
            self.logger.error("Инициализация баз данных завершена с ошибками")

        return success


def main():
    # Создаем директорию для логов, если её нет
    os.makedirs("logs", exist_ok=True)

    # Инициализируем базы данных
    initializer = DatabaseInitializer.from_env()
    success = initializer.initialize()

    # Возвращаем код выхода
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())

"""
Инициализация пакета тестов
Добавляет родительскую директорию в sys.path для корректной работы импортов
"""
import os
import sys

# Добавляем корень проекта в sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

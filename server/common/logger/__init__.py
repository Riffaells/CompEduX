"""
Common logging package for CompEduX microservices.

This module provides unified logging functionality with Rich formatting
and optional file logging for all microservices in the CompEduX platform.
"""

from common.logger.config import LoggerConfig, get_default_theme
from common.logger.setup import setup_rich_logger, get_logger
from common.logger.startup import initialize_logging

__all__ = ["setup_rich_logger", "get_logger", "LoggerConfig", "get_default_theme", "initialize_logging"]

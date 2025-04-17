"""
Module for unified logging initialization when starting services.
Prevents double initialization of loggers.
"""
import os
import sys
import logging

# Flag for tracking logging initialization
_logging_initialized = {}
_pythonpath_warned = False  # Флаг для отслеживания сообщения о PYTHONPATH

def initialize_logging(service_name, log_file=None):
    """
    Initialize logging for the entire application.

    This function sets up a unified logging configuration using Rich.
    It prevents duplicate initialization and handles uvicorn reload processes.

    Args:
        service_name: Name of the service
        log_file: Optional path to log file

    Returns:
        Configured logger
    """
    global _logging_initialized, _pythonpath_warned

    # Prevent re-initialization for the same service
    if service_name in _logging_initialized:
        from common.logger import get_logger
        return get_logger(service_name)

    # Check if this is a uvicorn reload process
    is_reload_process = "UVICORN_RELOAD" in os.environ

    # Disable logging for uvicorn and other libraries
    loggers_to_silence = [
        "uvicorn", "uvicorn.access", "uvicorn.error",
        "uvicorn.watchgram", "uvicorn.watchgram.watcher",
        "uvicorn.reload", "uvicorn.statreload",  # For reload mode
        "watchfiles", "watchfiles.main",         # File watching library
        "fastapi", "httpx", "asyncio"
    ]

    for name in loggers_to_silence:
        logger = logging.getLogger(name)
        logger.handlers = []
        logger.propagate = False
        logger.setLevel(logging.CRITICAL)  # Only critical errors
        logger.addHandler(logging.NullHandler())

    # Complete disabling of logs from watchfiles
    logging.getLogger("watchfiles.main").disabled = True

    # Clear existing logging settings
    for handler in list(logging.root.handlers):
        logging.root.removeHandler(handler)

    # Temporary basic logging for messages during initialization
    logging.basicConfig(level=logging.INFO, format='%(message)s', force=True)
    temp_logger = logging.getLogger("startup")

    # Add path to project root directory to PYTHONPATH
    root_dir = os.path.abspath(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))
    if root_dir not in sys.path:
        sys.path.insert(0, root_dir)

    # Выводим сообщение о PYTHONPATH только один раз за всё время работы приложения
    if not _pythonpath_warned and not is_reload_process:
        temp_logger.info(f"Adding root directory to PYTHONPATH: {root_dir}")
        _pythonpath_warned = True

    # Import logging modules - if there's an error here, the application will terminate immediately
    from common.logger import setup_rich_logger

    # Initialize common logger for the entire process
    logger = setup_rich_logger(
        service_name=service_name,
        log_level=logging.INFO,
        log_file=log_file
    )

    # Log only in the main process
    if not is_reload_process:
        logger.info(f"{service_name} logger initialized")

    # Set the flag that logging has been initialized
    _logging_initialized[service_name] = True
    return logger

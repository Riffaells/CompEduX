# CompEduX Common Utilities

Shared code for use across all CompEduX microservices.

## Logger

Common logging module using Rich for terminal formatting with optional file output.

### Basic Usage

```python
from common.logger import setup_rich_logger, get_logger

# Initialize at the beginning of the application
logger = setup_rich_logger(
    service_name="api_gateway",  # service name
    log_level=logging.INFO,      # logging level
    log_file="logs/api_gateway.log",  # optional - log file
    use_colors=True              # enable Rich colors
)

# Usage in code
logger.info("Message")
logger.error("Error")
logger.debug("Debug information")

# With Rich formatting
logger.info("[bold green]Service started[/bold green]")
logger.error(f"[bold red]Critical error: {error}[/bold red]")
```

### Simplified Initialization

```python
from common.logger import initialize_logging

# Initialize logging with one function call
logger = initialize_logging(
    service_name="auth_service",
    log_file="logs/auth_service.log"
)
```

### In FastAPI Applications

```python
from fastapi import FastAPI
from contextlib import asynccontextmanager

from common.logger import setup_rich_logger, get_logger
from common.logger.middleware import setup_request_logging

# Initialize logger
logger = setup_rich_logger(service_name="auth_service")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Log application lifecycle
    logger.info("[bold green]Application starting...[/bold green]")
    yield
    logger.info("[bold yellow]Application shutting down...[/bold yellow]")

app = FastAPI(lifespan=lifespan)

# Configure middleware for request logging
setup_request_logging(app, logger)
```

### In Settings Module

```python
from common.logger import get_logger

# Get an already configured logger
logger = get_logger("auth_service.settings")
```

## Import Resolution

To ensure proper imports, add the top-level project directory to `PYTHONPATH`. This can be done in several ways:

### 1. In run.py

```python
import sys
import os

# Add root directory to PYTHONPATH
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Start services
```

### 2. Through Environment Variable

Windows (cmd):

```
set PYTHONPATH=%PYTHONPATH%;C:\path\to\CompEduX\server
```

Linux/Mac:

```
export PYTHONPATH=$PYTHONPATH:/path/to/CompEduX/server
```

### 3. In PyCharm

Settings -> Project Structure -> Add Content Root

## Additional Features

### Request Logging Middleware

```python
from fastapi import FastAPI
from common.logger import setup_rich_logger
from common.logger.middleware import setup_request_logging

app = FastAPI()
logger = setup_rich_logger("api_service")

# Set up request logging middleware
setup_request_logging(
    app=app,
    logger=logger,
    exclude_paths=["/docs", "/redoc", "/openapi.json", "/healthz"],
    log_request_headers=False
)
```

### Disable Uvicorn Logs

When using Uvicorn in reload mode, you may want to disable the change detection logs:

```python
from common.logger import get_logger

# Disable Uvicorn reload logs
loggers_to_silence = [
    "uvicorn.reload",
    "uvicorn.statreload",
    "watchfiles",
    "watchfiles.main"
]

for name in loggers_to_silence:
    logger = logging.getLogger(name)
    logger.disabled = True
```

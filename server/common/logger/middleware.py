"""
Middleware for request/response logging in FastAPI applications.
"""

import time
import logging
import asyncio
from typing import Callable, List, Dict, Any, Optional
import uuid

from fastapi import FastAPI, Request, Response
from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint
from starlette.types import ASGIApp

from common.logger.config import format_log_time


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    """Middleware for logging HTTP requests and responses."""

    def __init__(
        self,
        app: ASGIApp,
        logger: logging.Logger,
        exclude_paths: List[str] = None,
        log_request_headers: bool = False
    ):
        """
        Initialize the middleware.

        Args:
            app: The ASGI application
            logger: Logger instance to use
            exclude_paths: List of path prefixes to exclude from logging
            log_request_headers: Whether to log request headers
        """
        super().__init__(app)
        self.logger = logger
        self.exclude_paths = exclude_paths or ['/docs', '/redoc', '/openapi.json', '/healthz']
        self.log_request_headers = log_request_headers

    async def dispatch(
        self, request: Request, call_next: RequestResponseEndpoint
    ) -> Response:
        """Process the request and log information."""
        # Get client IP and request ID
        client_ip = request.client.host if request.client else "unknown"
        request_id = request.headers.get("X-Request-ID", "")

        # Get formatted timestamp
        current_time = format_log_time()

        # Extract URL path and method
        path = request.url.path
        method = request.method

        # Don't log requests to excluded paths
        should_log = not any(path.startswith(p) for p in self.exclude_paths)

        # Start timer for request duration
        start_time = time.time()

        if should_log:
            # Log basic request info
            user_agent = request.headers.get("User-Agent", "unknown")
            referer = request.headers.get("Referer", "-")

            # Truncate useragent if it's too long
            user_agent_short = user_agent[:30] + "..." if len(user_agent) > 30 else user_agent

            log_message = (
                f"[{current_time}] [request]{method}[/request] [cyan]{path}[/cyan] - "
                f"IP:[blue]{client_ip}[/blue]"
            )

            if request_id:
                log_message += f" ID:[dim]{request_id}[/dim]"

            log_message += f" UA:[dim]{user_agent_short}[/dim]"

            # Log additional headers if requested
            if self.log_request_headers:
                headers = "\n".join([f"  {k}: {v}" for k, v in request.headers.items()])
                log_message += f"\nHeaders:\n{headers}"

            self.logger.debug(log_message)

        try:
            # Process request
            response = await call_next(request)

            # Calculate request duration
            duration = time.time() - start_time
            duration_ms = round(duration * 1000, 2)

            # Log based on status code if we should log this path
            if should_log:
                if response.status_code >= 500:
                    self.logger.critical(
                        f"[{current_time}] [critical]SERVER ERROR[/critical] [request]{method}[/request] "
                        f"[cyan]{path}[/cyan] - [bold red]{response.status_code}[/bold red] ({duration_ms}ms)"
                    )
                elif response.status_code >= 400:
                    self.logger.error(
                        f"[{current_time}] [error]CLIENT ERROR[/error] [request]{method}[/request] "
                        f"[cyan]{path}[/cyan] - [bold red]{response.status_code}[/bold red] ({duration_ms}ms)"
                    )
                elif path.startswith(('/auth', '/api/v1/auth', '/login', '/register')):
                    self.logger.info(
                        f"[{current_time}] [auth]AUTH REQUEST[/auth] [request]{method}[/request] "
                        f"[cyan]{path}[/cyan] - [green]{response.status_code}[/green] ({duration_ms}ms)"
                    )
                else:
                    self.logger.info(
                        f"[{current_time}] [response]{method}[/response] [cyan]{path}[/cyan] - "
                        f"[green]{response.status_code}[/green] ({duration_ms}ms)"
                    )

            return response

        except asyncio.CancelledError:
            self.logger.warning(
                f"[{current_time}] [yellow]Request cancelled: {method} {path}[/yellow]"
            )
            raise  # Re-raise to allow proper handling

        except Exception as e:
            self.logger.error(
                f"[{current_time}] [bold red]Request error: {method} {path} - {str(e)}[/bold red]"
            )
            raise  # Re-raise to allow FastAPI to handle the exception


def setup_request_logging(
    app: FastAPI,
    logger: logging.Logger,
    exclude_paths: List[str] = None,
    log_request_headers: bool = False
) -> None:
    """
    Set up request logging middleware for a FastAPI application.

    Args:
        app: FastAPI application
        logger: Logger instance to use
        exclude_paths: List of path prefixes to exclude from logging
        log_request_headers: Whether to log request headers
    """
    app.add_middleware(
        RequestLoggingMiddleware,
        logger=logger,
        exclude_paths=exclude_paths,
        log_request_headers=log_request_headers
    )

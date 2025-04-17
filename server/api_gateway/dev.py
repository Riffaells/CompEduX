#!/usr/bin/env python
"""
Script for local development of the API Gateway.
Configures the environment for testing microservices on Windows or Linux.

Usage:
    python dev.py [--help] [--host HOST] [--port PORT] [--check-services]

Examples:
    python dev.py                  # Run with default settings
    python dev.py --port 8888      # Run on a non-standard port
    python dev.py --check-services # Check service availability before starting
"""
import os
import sys
import argparse
import socket
import asyncio
import uvicorn
import logging
from datetime import datetime
import platform

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger("dev")

# Define the absolute path to the project's root directory
root_dir = os.path.abspath(os.path.dirname(__file__))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

# Function to check port availability
def is_port_available(host, port):
    """Checks if the specified port is available on the host"""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        try:
            s.bind((host, port))
            return True
        except OSError:
            return False

# Function to find a free port
def find_free_port(host, start_port, max_attempts=10):
    """Finds a free port, starting from the specified port"""
    for port in range(start_port, start_port + max_attempts):
        if is_port_available(host, port):
            return port
    return None

# Asynchronous function to check service availability
async def check_service(host, port, service_name):
    """Checks service availability by host and port"""
    try:
        # Create a connection with timeout
        reader, writer = await asyncio.wait_for(
            asyncio.open_connection(host, port), timeout=2.0
        )
        writer.close()
        await writer.wait_closed()
        logger.info(f"✓ Service {service_name} is available at {host}:{port}")
        return True
    except (ConnectionRefusedError, asyncio.TimeoutError, OSError) as e:
        logger.warning(f"✗ Service {service_name} is unavailable at {host}:{port}: {str(e)}")
        return False

# Function to check all services
async def check_all_services():
    """Checks the availability of all services"""
    # Import configuration
    from app.core.config import settings, SERVICE_ROUTES

    # Check each service
    results = {}
    tasks = []

    for service_name, config in SERVICE_ROUTES.items():
        if not config.get("base_url"):
            continue

        url = config["base_url"]
        if not url.startswith("http"):
            continue

        # Extract host and port from URL
        host = url.split("://")[1].split(":")[0]
        port = int(url.split(":")[-1].split("/")[0])

        # Create a task for checking
        task = asyncio.create_task(
            check_service(host, port, service_name)
        )
        tasks.append((service_name, task))

    # Wait for all tasks to complete
    for service_name, task in tasks:
        results[service_name] = await task

    # Summarize results
    available = sum(1 for v in results.values() if v)
    total = len(results)

    if available == 0:
        logger.warning(f"⚠ No services are available ({total} checked)")
    elif available < total:
        logger.warning(f"⚠ {available} out of {total} services are available")
    else:
        logger.info(f"✓ All services ({total}) are available")

    return results

def main():
    """Main function for running the API Gateway in development mode"""
    # Parse command line arguments
    parser = argparse.ArgumentParser(description="Run API Gateway in development mode")
    parser.add_argument("--host", default="0.0.0.0", help="Host to bind to (default: 0.0.0.0)")
    parser.add_argument("--port", type=int, default=8000, help="Port to run on (default: 8000)")
    parser.add_argument("--check-services", action="store_true", help="Check service availability before starting")
    parser.add_argument("--reload", action="store_true", help="Enable automatic reload when files change")

    args = parser.parse_args()

    # Display system information
    logger.info(f"Starting API Gateway in development mode")
    logger.info(f"System: {platform.system()} {platform.release()} ({platform.architecture()[0]})")
    logger.info(f"Python: {platform.python_version()}")

    # Check port availability
    if not is_port_available(args.host, args.port):
        logger.warning(f"⚠ Port {args.port} is occupied, looking for a free port...")
        free_port = find_free_port(args.host, 8001)
        if free_port:
            logger.info(f"✓ Found free port: {free_port}")
            args.port = free_port
        else:
            logger.error("✗ Could not find a free port!")
            sys.exit(1)

    # Check service availability if the option is specified
    if args.check_services:
        logger.info("Checking service availability...")
        loop = asyncio.get_event_loop()
        loop.run_until_complete(check_all_services())

    # Start Uvicorn
    logger.info(f"Starting API Gateway on {args.host}:{args.port}")

    uvicorn.run(
        "app.main:app",
        host=args.host,
        port=args.port,
        reload=args.reload,
        log_level="info"
    )

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        logger.info("Shutting down at user request")
    except Exception as e:
        logger.error(f"Error during startup: {str(e)}", exc_info=True)
        sys.exit(1)

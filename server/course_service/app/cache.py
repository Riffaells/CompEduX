"""
Cache module for the Course Service.

This module provides Redis-based caching functionality to improve API performance.
It includes functions to configure the cache, manage cached data, and clean up connections.
"""
import asyncio
import functools
import json
from typing import Optional, Any, Callable

import redis.asyncio as redis
from app.core.config import settings

from common.logger import get_logger

# Создаем логгер для кэша
logger = get_logger("course_service.cache")

# Глобальный клиент Redis
_redis_client: Optional[redis.Redis] = None


def configure_cache(redis_client: redis.Redis) -> None:
    """
    Configure the cache with a Redis client.

    Args:
        redis_client: Initialized Redis client
    """
    global _redis_client
    _redis_client = redis_client
    logger.info(f"Cache configured with Redis at {settings.REDIS_HOST}:{settings.REDIS_PORT}")


async def cleanup_cache() -> None:
    """
    Clean up cache resources when the application shuts down.
    """
    global _redis_client
    if _redis_client:
        logger.info("Closing Redis connections...")
        _redis_client = None


def cache(
        expire: int = None,
        namespace: str = None,
        key_builder: Callable = None
):
    """
    Cache decorator for API endpoints.

    Args:
        expire: Cache expiration time in seconds (default: from settings)
        namespace: Cache key namespace
        key_builder: Custom function to build cache keys

    Returns:
        Decorator function
    """

    def decorator(func):
        # Check if we're dealing with an async function
        is_async = asyncio.iscoroutinefunction(func)

        @functools.wraps(func)
        async def async_wrapper(*args, **kwargs):
            if not settings.REDIS_ENABLED or not _redis_client:
                return await func(*args, **kwargs)

            # Get request object from function args (for FastAPI endpoints)
            request = next((arg for arg in args if hasattr(arg, 'method')), None)

            # Build cache key
            if key_builder:
                key = key_builder(*args, **kwargs)
            else:
                # Default key builder
                prefix = namespace or func.__module__ + "." + func.__name__
                # Convert args to a string representation
                str_args = [str(arg) for arg in args if not hasattr(arg, 'method')]
                str_kwargs = [f"{k}:{v}" for k, v in kwargs.items()]
                key = f"{prefix}:{':'.join(str_args + str_kwargs)}"

            # Try to get cached value
            try:
                cached_data = await _redis_client.get(key)
                if cached_data:
                    logger.debug(f"Cache hit for key: {key}")
                    return json.loads(cached_data)

                logger.debug(f"Cache miss for key: {key}")
                # Execute the function and cache the result
                result = await func(*args, **kwargs)

                # Skip caching for empty results or error responses
                if result is None:
                    return result

                # Determine expiration time
                expiration = expire or settings.CACHE_EXPIRE_IN_SECONDS

                # Cache the result
                await _redis_client.setex(
                    key,
                    expiration,
                    json.dumps(result, default=_json_serializer)
                )
                return result
            except Exception as e:
                logger.error(f"Cache error: {str(e)}")
                # Fallback to uncached function call
                return await func(*args, **kwargs)

        @functools.wraps(func)
        def sync_wrapper(*args, **kwargs):
            if not settings.REDIS_ENABLED or not _redis_client:
                return func(*args, **kwargs)

            # Get request object from function args (for FastAPI endpoints)
            request = next((arg for arg in args if hasattr(arg, 'method')), None)

            # Build cache key
            if key_builder:
                key = key_builder(*args, **kwargs)
            else:
                # Default key builder
                prefix = namespace or func.__module__ + "." + func.__name__
                # Convert args to a string representation
                str_args = [str(arg) for arg in args if not hasattr(arg, 'method')]
                str_kwargs = [f"{k}:{v}" for k, v in kwargs.items()]
                key = f"{prefix}:{':'.join(str_args + str_kwargs)}"

            # Try to get cached value
            try:
                cached_data = _redis_client.get(key)
                if cached_data:
                    logger.debug(f"Cache hit for key: {key}")
                    return json.loads(cached_data)

                logger.debug(f"Cache miss for key: {key}")
                # Execute the function and cache the result
                result = func(*args, **kwargs)

                # Skip caching for empty results or error responses
                if result is None:
                    return result

                # Determine expiration time
                expiration = expire or settings.CACHE_EXPIRE_IN_SECONDS

                # Cache the result
                _redis_client.setex(
                    key,
                    expiration,
                    json.dumps(result, default=_json_serializer)
                )
                return result
            except Exception as e:
                logger.error(f"Cache error: {str(e)}")
                # Fallback to uncached function call
                return func(*args, **kwargs)

        # Return appropriate wrapper based on function type
        return async_wrapper if is_async else sync_wrapper

    return decorator


def _json_serializer(obj: Any) -> Any:
    """
    Custom JSON serializer for objects not serializable by default json code

    Args:
        obj: Object to be serialized

    Returns:
        JSON serializable version of the object
    """
    if hasattr(obj, '__dict__'):
        return obj.__dict__
    elif hasattr(obj, 'isoformat'):
        return obj.isoformat()
    else:
        return str(obj)


async def invalidate_cache(key_pattern: str) -> int:
    """
    Invalidate cache keys matching a pattern

    Args:
        key_pattern: Redis key pattern to match

    Returns:
        Number of keys deleted
    """
    if not settings.REDIS_ENABLED or not _redis_client:
        return 0

    try:
        # Get all keys matching the pattern
        keys = await _redis_client.keys(key_pattern)
        if not keys:
            return 0

        # Delete all matching keys
        deleted = await _redis_client.delete(*keys)
        logger.info(f"Invalidated {deleted} cache keys matching pattern: {key_pattern}")
        return deleted
    except Exception as e:
        logger.error(f"Error invalidating cache: {str(e)}")
        return 0


async def clear_all_cache() -> bool:
    """
    Clear entire cache

    Returns:
        True if cache was cleared successfully
    """
    if not settings.REDIS_ENABLED or not _redis_client:
        return False

    try:
        await _redis_client.flushdb()
        logger.info("Cleared entire cache")
        return True
    except Exception as e:
        logger.error(f"Error clearing cache: {str(e)}")
        return False

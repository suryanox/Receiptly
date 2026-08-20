from __future__ import annotations

from contextlib import contextmanager
from typing import Iterator

from psycopg2 import extensions, pool

from reciply_ocr.config import DatabaseSettings


class Database:
    """Owns a psycopg2 connection pool and exposes transactional cursors."""

    def __init__(self, settings: DatabaseSettings) -> None:
        self._pool = pool.SimpleConnectionPool(
            1,
            settings.max_pool_size,
            dsn=settings.url,
            user=settings.user,
            password=settings.password,
        )

    @contextmanager
    def transaction(self) -> Iterator[extensions.cursor]:
        """Yield a cursor inside an autocommitted-on-success transaction."""
        conn = self._pool.getconn()
        try:
            with conn.cursor() as cursor:
                yield cursor
            conn.commit()
        except BaseException:
            conn.rollback()
            raise
        finally:
            self._pool.putconn(conn)

    def close(self) -> None:
        self._pool.closeall()

from __future__ import annotations

import os
from dataclasses import dataclass
from functools import lru_cache
from typing import Any

from pyhocon import ConfigFactory, ConfigTree


@dataclass(frozen=True)
class DatabaseSettings:
    url: str
    user: str
    password: str
    max_pool_size: int = 5


@dataclass(frozen=True)
class LLMClientSettings:
    api_key: str
    model: str | None = None


@dataclass(frozen=True)
class TelegramSettings:
    bot_token: str


@dataclass(frozen=True)
class AppSettings:
    name: str
    poll_interval_seconds: int
    database: DatabaseSettings
    llmclient: LLMClientSettings
    telegram: TelegramSettings


_CONFIG_DIR = os.path.join(os.path.dirname(__file__), "resources")
_REFERENCE_CONF = os.path.join(_CONFIG_DIR, "reference.conf")
_PROD_CONF = os.path.join(_CONFIG_DIR, "application.prod.conf")

_ENV_OVERRIDES: dict[str, str] = {
    "app.pollIntervalSeconds": "POLL_INTERVAL_SECONDS",
    "database.url": "DATABASE_URL",
    "database.user": "DATABASE_USER",
    "database.password": "DATABASE_PASSWORD",
    "database.maxPoolSize": "DATABASE_MAX_POOL_SIZE",
    "llmclient.apiKey": "LLMCLIENT_API_KEY",
    "llmclient.model": "LLMCLIENT_MODEL",
    "telegram.botToken": "TELEGRAM_BOT_TOKEN",
}


def _active_config_path() -> str:
    is_prod = os.environ.get("TIER", "").lower() == "prod"
    return _PROD_CONF if is_prod and os.path.exists(_PROD_CONF) else _REFERENCE_CONF


def _env_overlay() -> dict[str, Any]:
    overlay: dict[str, Any] = {}
    for key, var in _ENV_OVERRIDES.items():
        if var not in os.environ:
            continue
        target = overlay
        parts = key.split(".")
        for part in parts[:-1]:
            target = target.setdefault(part, {})
        target[parts[-1]] = os.environ[var]
    return overlay


@lru_cache(maxsize=1)
def load_settings() -> AppSettings:
    file_config = ConfigFactory.parse_file(_active_config_path())
    env_overrides = _env_overlay()
    config: ConfigTree = (
        ConfigFactory.from_dict(env_overrides).with_fallback(file_config)
        if env_overrides
        else file_config
    )

    explicit_model = config.get("llmclient.model", default="")

    return AppSettings(
        name=config.get_string("app.name"),
        poll_interval_seconds=config.get_int("app.pollIntervalSeconds"),
        database=DatabaseSettings(
            url=config.get_string("database.url"),
            user=config.get_string("database.user"),
            password=config.get_string("database.password"),
            max_pool_size=config.get_int("database.maxPoolSize"),
        ),
        llmclient=LLMClientSettings(
            api_key=config.get_string("llmclient.apiKey"),
            model=explicit_model or None,
        ),
        telegram=TelegramSettings(bot_token=config.get_string("telegram.botToken")),
    )

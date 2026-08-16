import os

from pyhocon import ConfigFactory

_CONFIG_DIR = os.path.join(os.path.dirname(__file__), "resources")
_PROD_CONF = os.path.join(_CONFIG_DIR, "application.prod.conf")
_REFERENCE_CONF = os.path.join(_CONFIG_DIR, "reference.conf")


def load_config():
    path = _PROD_CONF if os.path.exists(_PROD_CONF) else _REFERENCE_CONF
    try:
        config = ConfigFactory.parse_file(path, resolve_env=True)
    except TypeError:
        config = ConfigFactory.parse_file(path)
        resolver = getattr(config, "resolve_env", None)
        if callable(resolver):
            resolver()
    return config

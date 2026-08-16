import os

from pyhocon import ConfigFactory

_CONFIG_DIR = os.path.join(os.path.dirname(__file__), "resources")
_PROD_CONF = os.path.join(_CONFIG_DIR, "application.prod.conf")
_REFERENCE_CONF = os.path.join(_CONFIG_DIR, "reference.conf")


def load_config():
    use_prod = os.environ.get("Tier") == "Prod"
    path = _PROD_CONF if (use_prod and os.path.exists(_PROD_CONF)) else _REFERENCE_CONF
    return ConfigFactory.parse_file(path)

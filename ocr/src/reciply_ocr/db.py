from psycopg2 import pool

_pool = None


def init_db(config):
    global _pool
    db = config["database"]
    _pool = pool.SimpleConnectionPool(
        1,
        db["maxPoolSize"],
        dsn=db["url"],
        user=db["user"],
        password=db["password"],
    )


def get_conn():
    return _pool.getconn()


def put_conn(conn):
    _pool.putconn(conn)

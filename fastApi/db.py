import os

def get_connection():
    psycopg, dict_row = _load_psycopg()
    return psycopg.connect(
        host=os.getenv('DB_HOST'),
        user=os.getenv('DB_USER'),
        password=os.getenv('DB_PASSWORD'),
        dbname=os.getenv('DB_NAME'),
        port=int(os.getenv('DB_PORT', '5432')),
        connect_timeout=3,
        row_factory=dict_row,
    )


def get_connection_local():
    psycopg, dict_row = _load_psycopg()
    return psycopg.connect(
        host=os.getenv('DB_HOST_LOCAL', os.getenv('DB_HOST')),
        user=os.getenv('DB_USER_LOCAL', os.getenv('DB_USER')),
        password=os.getenv('DB_PASSWORD_LOCAL', os.getenv('DB_PASSWORD')),
        dbname=os.getenv('DB_NAME_LOCAL', os.getenv('DB_NAME')),
        port=int(os.getenv('DB_PORT_LOCAL', os.getenv('DB_PORT', '5432'))),
        connect_timeout=3,
        row_factory=dict_row,
    )


def _load_psycopg():
    import psycopg
    from psycopg.rows import dict_row

    return psycopg, dict_row


def read_sql_file(file_path: str, separator: str = "-- QUERY_SEPARATOR") -> list:
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read().split(separator)


def fetch_sql_data(connection, sql_file: str, params: list) -> list:
    queries = read_sql_file(sql_file)
    results = []
    with connection.cursor() as cursor:
        for i, query in enumerate(queries):
            query = query.strip()
            if query:
                cursor.execute(query, params[i])
                results.append(cursor.fetchall())
    return results


def insert_analysis_history(connection, store_id, analysis_result):
    query = "INSERT INTO analysis_history (store_id, analysis_script) VALUES (%s, %s)"
    try:
        with connection.cursor() as cursor:
            cursor.execute(query, (store_id, analysis_result))
        connection.commit()
    except Exception as e:
        raise RuntimeError(f"분석 기록 저장 오류: {str(e)}") from e

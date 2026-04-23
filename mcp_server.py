# mcp_server.py (Conceptual implementation for local use)
import asyncio
import json
from mcp.server.fastmcp import FastMCP
import asyncpg

mcp = FastMCP("TossBeneface-DB-Tool")

# DB Connection settings (from .env)
DB_URL = "postgresql://user:password@localhost:5432/toss_db"

@mcp.tool()
async def get_table_schema(table_name: str) -> str:
    """Fetch the schema and index information for a specific table."""
    conn = await asyncpg.connect(DB_URL)
    try:
        # Schema query
        schema = await conn.fetch(f"""
            SELECT column_name, data_type, is_nullable 
            FROM information_schema.columns 
            WHERE table_name = '{table_name}';
        """)
        # Index query
        indexes = await conn.fetch(f"SELECT indexname, indexdef FROM pg_indexes WHERE tablename = '{table_name}';")
        return json.dumps({"schema": [dict(r) for r in schema], "indexes": [dict(r) for r in indexes]}, indent=2)
    finally:
        await conn.close()

@mcp.tool()
async def explain_query(query: str) -> str:
    """Run EXPLAIN ANALYZE on a given SQL query to check performance."""
    conn = await asyncpg.connect(DB_URL)
    try:
        plan = await conn.fetch(f"EXPLAIN ANALYZE {query}")
        return "\n".join([r[0] for r in plan])
    finally:
        await conn.close()

if __name__ == "__main__":
    mcp.run()

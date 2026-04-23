import logging
from dataclasses import asdict, dataclass

from db import get_connection

logger = logging.getLogger(__name__)

_EXACT_MATCH_QUERY = """
    SELECT
        c.card_name,
        c.card_company,
        COALESCE(cb.shop, '') AS shop,
        COALESCE(cb.summary, '') AS summary,
        COALESCE(cb.benefit, 0) AS benefit,
        COALESCE(cb.limit_once, 0) AS limit_once,
        COALESCE(cb.limit_month, 0) AS limit_month,
        COALESCE(cb.min_pay, 0) AS min_pay,
        COALESCE(cb.min_per, 0) AS min_per,
        COALESCE(cb.monthly, 0) AS monthly,
        COALESCE(u.last_per, 0) AS last_per,
        COALESCE(u.now_per, 0) AS now_per,
        COALESCE(u.monthly_split, 0) AS monthly_split,
        COALESCE(u.accrue_benefit, 0) AS accrue_benefit
    FROM user_data_test u
    JOIN card c ON c.id = u.card_id
    JOIN card_benefit cb ON cb.card_id = u.card_id
    WHERE u.member_id = %s
      AND LOWER(TRIM(cb.shop)) = LOWER(TRIM(%s))
    ORDER BY c.card_name, cb.benefit DESC, cb.limit_once DESC
"""

_PARTIAL_MATCH_QUERY = """
    SELECT
        c.card_name,
        c.card_company,
        COALESCE(cb.shop, '') AS shop,
        COALESCE(cb.summary, '') AS summary,
        COALESCE(cb.benefit, 0) AS benefit,
        COALESCE(cb.limit_once, 0) AS limit_once,
        COALESCE(cb.limit_month, 0) AS limit_month,
        COALESCE(cb.min_pay, 0) AS min_pay,
        COALESCE(cb.min_per, 0) AS min_per,
        COALESCE(cb.monthly, 0) AS monthly,
        COALESCE(u.last_per, 0) AS last_per,
        COALESCE(u.now_per, 0) AS now_per,
        COALESCE(u.monthly_split, 0) AS monthly_split,
        COALESCE(u.accrue_benefit, 0) AS accrue_benefit
    FROM user_data_test u
    JOIN card c ON c.id = u.card_id
    JOIN card_benefit cb ON cb.card_id = u.card_id
    WHERE u.member_id = %s
      AND cb.shop ILIKE %s
    ORDER BY c.card_name, cb.benefit DESC, cb.limit_once DESC
"""

_ALL_BENEFITS_QUERY = """
    SELECT
        c.card_name,
        c.card_company,
        COALESCE(cb.shop, '') AS shop,
        COALESCE(cb.summary, '') AS summary,
        COALESCE(cb.benefit, 0) AS benefit,
        COALESCE(cb.limit_once, 0) AS limit_once,
        COALESCE(cb.limit_month, 0) AS limit_month,
        COALESCE(cb.min_pay, 0) AS min_pay,
        COALESCE(cb.min_per, 0) AS min_per,
        COALESCE(cb.monthly, 0) AS monthly,
        COALESCE(u.last_per, 0) AS last_per,
        COALESCE(u.now_per, 0) AS now_per,
        COALESCE(u.monthly_split, 0) AS monthly_split,
        COALESCE(u.accrue_benefit, 0) AS accrue_benefit
    FROM user_data_test u
    JOIN card c ON c.id = u.card_id
    JOIN card_benefit cb ON cb.card_id = u.card_id
    WHERE u.member_id = %s
"""

_VISIT_COUNT_QUERY = """
    SELECT COUNT(*) as count
    FROM payment
    WHERE member_id = %s
      AND order_name ILIKE %s
      AND approved_at IS NOT NULL
      AND SUBSTR(approved_at, 1, 7) = TO_CHAR(CURRENT_DATE, 'YYYY-MM')
"""

@dataclass(frozen=True)
class BenefitCandidateInput:
    card_name: str
    card_company: str
    shop: str
    summary: str
    benefit: int
    limit_once: int
    limit_month: int
    min_pay: int
    min_per: int
    monthly: int
    last_per: int
    now_per: int
    monthly_split: int
    accrue_benefit: int
    visit_count: int = 0

    def to_payload(self) -> dict:
        return asdict(self)


def list_member_benefit_inputs(member_id: int, store_name: str) -> list[BenefitCandidateInput]:
    normalized_store = store_name.strip()
    if not normalized_store:
        return []

    with get_connection() as connection:
        rows = _query_candidates(connection, _EXACT_MATCH_QUERY, (member_id, normalized_store))
        if rows:
            return rows

        logger.info(
            "No exact benefit rows found for memberId=%s, storeName=%s. Trying partial match.",
            member_id,
            normalized_store,
        )
        return _query_candidates(connection, _PARTIAL_MATCH_QUERY, (member_id, f"%{normalized_store}%"))


def list_all_member_benefit_inputs(member_id: int) -> list[BenefitCandidateInput]:
    with get_connection() as connection:
        return _query_candidates(connection, _ALL_BENEFITS_QUERY, (member_id,))


def count_monthly_store_visits(member_id: int, store_name: str) -> int:
    with get_connection() as connection:
        with connection.cursor() as cursor:
            cursor.execute(_VISIT_COUNT_QUERY, (member_id, f"%{store_name}%"))
            row = cursor.fetchone()
            return int(row["count"]) if row else 0


def _query_candidates(connection, query: str, params: tuple) -> list[BenefitCandidateInput]:
    with connection.cursor() as cursor:
        cursor.execute(query, params)
        rows = cursor.fetchall()

    return [
        BenefitCandidateInput(
            card_name=str(row["card_name"]),
            card_company=str(row["card_company"]),
            shop=str(row["shop"]),
            summary=str(row["summary"]),
            benefit=int(row["benefit"]),
            limit_once=int(row["limit_once"]),
            limit_month=int(row["limit_month"]),
            min_pay=int(row["min_pay"]),
            min_per=int(row["min_per"]),
            monthly=int(row["monthly"]),
            last_per=int(row["last_per"]),
            now_per=int(row["now_per"]),
            monthly_split=int(row["monthly_split"]),
            accrue_benefit=int(row["accrue_benefit"]),
        )
        for row in rows
    ]

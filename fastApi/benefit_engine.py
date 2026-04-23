import importlib
import json
import logging
import time
from dataclasses import asdict, dataclass

from benefit_repository import (
    BenefitCandidateInput,
    count_monthly_store_visits,
    list_all_member_benefit_inputs,
    list_member_benefit_inputs,
)

logger = logging.getLogger(__name__)

try:
    from metrics import BENEFIT_ENGINE_LATENCY, BENEFIT_ENGINE_REQUESTS
except Exception:
    BENEFIT_ENGINE_LATENCY = None
    BENEFIT_ENGINE_REQUESTS = None


@dataclass(frozen=True, slots=True)
class BenefitOption:
    card_name: str
    discount_amount: int
    benefit_type: str
    description: str


@dataclass(frozen=True, slots=True)
class BenefitAnalysis:
    best_card_name: str
    total_potential_benefit: int
    all_options: list[BenefitOption]
    engine: str

    @classmethod
    def from_mapping(cls, payload: dict, engine: str) -> "BenefitAnalysis":
        options = [
            BenefitOption(
                card_name=str(option["card_name"]),
                discount_amount=int(option["discount_amount"]),
                benefit_type=str(option["benefit_type"]),
                description=str(option["description"]),
            )
            for option in payload["all_options"]
        ]
        return cls(
            best_card_name=str(payload["best_card_name"]),
            total_potential_benefit=int(payload["total_potential_benefit"]),
            all_options=options,
            engine=engine,
        )


def analyze_best_benefit(member_id: int, store_name: str, category: str, amount: int) -> BenefitAnalysis:
    start_time = time.perf_counter()
    is_batch = "false"

    try:
        raw_candidates = list_member_benefit_inputs(member_id, store_name)
    except Exception:
        logger.exception(
            "Failed to load card benefit inputs for memberId=%s, storeName=%s. Falling back to default engine.",
            member_id,
            store_name,
        )
        analysis = _analyze_best_benefit_python(member_id, store_name, category, amount)
        _record_metrics("python_dummy", is_batch, start_time)
        return analysis

    if not raw_candidates:
        logger.info(
            "No member-specific card benefit data found for memberId=%s, storeName=%s. Falling back to default engine.",
            member_id,
            store_name,
        )
        analysis = _analyze_best_benefit_python(member_id, store_name, category, amount)
        _record_metrics("python_dummy", is_batch, start_time)
        return analysis

    visit_count = _safe_count_monthly_store_visits(member_id, store_name)
    candidates = [_with_visit_count(candidate, visit_count) for candidate in raw_candidates]

    try:
        rust_module = importlib.import_module("card_benefit_rust")
        raw_payload = rust_module.analyze_best_benefit(
            json.dumps([candidate.to_payload() for candidate in candidates], ensure_ascii=False),
            amount,
        )
        payload = json.loads(raw_payload)
        analysis = BenefitAnalysis.from_mapping(payload, engine="rust")
        logger.info(
            "AnalyzeBestBenefit served by Rust engine for memberId=%s, storeName=%s (visits: %s)",
            member_id,
            store_name,
            visit_count,
        )
        _record_metrics("rust", is_batch, start_time)
        return analysis
    except Exception:
        logger.exception("Rust benefit engine failed. Falling back to Python evaluator.")

    analysis = _analyze_best_benefit_candidates_python(candidates, amount)
    _record_metrics("python", is_batch, start_time)
    return analysis


def analyze_batch_benefits(member_id: int, stores: list[dict]) -> list[dict]:
    start_time = time.perf_counter()
    is_batch = "true"

    try:
        all_candidates = list_all_member_benefit_inputs(member_id)
    except Exception:
        logger.exception("Failed to load all card benefits for memberId=%s", member_id)
        results = _analyze_batch_benefits_python(member_id, stores)
        _record_metrics("python_dummy", is_batch, start_time)
        return results

    if not all_candidates:
        results = _analyze_batch_benefits_python(member_id, stores)
        _record_metrics("python_dummy", is_batch, start_time)
        return results

    try:
        rust_module = importlib.import_module("card_benefit_rust")
        store_requests = _build_store_requests(member_id, stores)
        raw_payload = rust_module.analyze_batch_benefits_from_candidates(
            json.dumps(store_requests, ensure_ascii=False),
            json.dumps([candidate.to_payload() for candidate in all_candidates], ensure_ascii=False),
        )
        payload = json.loads(raw_payload)
        logger.info(
            "Batch benefits served by Rust grouped engine for memberId=%s, storesCount=%s",
            member_id,
            len(stores),
        )
        _record_metrics("rust", is_batch, start_time)
        return payload["results"]
    except Exception:
        logger.exception("Rust batch engine failed. Falling back to Python batch evaluator.")

    batch_input = _build_batch_input(member_id, stores, all_candidates)
    results = _analyze_batch_benefits_candidates_python(batch_input)
    _record_metrics("python", is_batch, start_time)
    return results


def _build_store_requests(member_id: int, stores: list[dict]) -> list[dict]:
    return [
        {
            "store_name": store["store_name"],
            "amount": store["amount"],
            "visit_count": _safe_count_monthly_store_visits(member_id, store["store_name"]),
        }
        for store in stores
    ]


def _build_batch_input(
    member_id: int,
    stores: list[dict],
    all_candidates: list[BenefitCandidateInput],
) -> list[dict]:
    batch_input = []
    for store in stores:
        store_name = store["store_name"]
        visit_count = _safe_count_monthly_store_visits(member_id, store_name)
        candidates_for_store = [
            _with_visit_count(candidate, visit_count)
            for candidate in all_candidates
            if _store_matches(store_name, candidate.shop)
        ]
        batch_input.append(
            {
                "store_name": store_name,
                "amount": store["amount"],
                "candidates": [candidate.to_payload() for candidate in candidates_for_store],
            }
        )
    return batch_input


def _safe_count_monthly_store_visits(member_id: int, store_name: str) -> int:
    try:
        return count_monthly_store_visits(member_id, store_name)
    except Exception as exc:
        logger.warning(
            "Failed to load monthly visit count for memberId=%s, storeName=%s. Continuing without progressive bonus: %s",
            member_id,
            store_name,
            exc,
        )
        return 0


def _with_visit_count(candidate: BenefitCandidateInput, visit_count: int) -> BenefitCandidateInput:
    return BenefitCandidateInput(**{**asdict(candidate), "visit_count": visit_count})


def _store_matches(store_name: str, candidate_shop: str) -> bool:
    normalized_store = store_name.strip().lower()
    normalized_shop = candidate_shop.strip().lower()
    if not normalized_store or not normalized_shop:
        return False
    return normalized_store in normalized_shop or normalized_shop in normalized_store


def _record_metrics(engine_type: str, is_batch: str, start_time: float) -> None:
    if BENEFIT_ENGINE_LATENCY is None or BENEFIT_ENGINE_REQUESTS is None:
        return

    try:
        latency = time.perf_counter() - start_time
        BENEFIT_ENGINE_LATENCY.labels(engine_type=engine_type, is_batch=is_batch).observe(latency)
        BENEFIT_ENGINE_REQUESTS.labels(engine_type=engine_type, is_batch=is_batch).inc()
    except Exception:
        logger.exception("Failed to record benefit engine metrics.")


def _analyze_best_benefit_candidates_python(
    candidates: list[BenefitCandidateInput],
    amount: int,
) -> BenefitAnalysis:
    if not candidates:
        return BenefitAnalysis(
            best_card_name="No Card",
            total_potential_benefit=0,
            all_options=[],
            engine="python",
        )

    safe_amount = max(int(amount), 0)
    best_by_card: dict[tuple[str, str], dict] = {}

    for candidate in candidates:
        discount_amount, description = _evaluate_candidate(candidate, safe_amount)
        option = BenefitOption(
            card_name=candidate.card_name,
            discount_amount=discount_amount,
            benefit_type="DISCOUNT",
            description=description,
        )
        rank = (discount_amount, candidate.benefit, candidate.limit_once)
        key = (candidate.card_name, candidate.card_company)
        current = best_by_card.get(key)
        if current is None or rank > current["rank"]:
            best_by_card[key] = {"option": option, "rank": rank}

    ranked_options = list(best_by_card.values())
    ranked_options.sort(
        key=lambda item: (
            -item["rank"][0],
            -item["rank"][1],
            -item["rank"][2],
            item["option"].card_name,
        )
    )
    all_options = [item["option"] for item in ranked_options]
    best_option = all_options[0] if all_options else BenefitOption("No Card", 0, "NONE", "No benefit found")

    return BenefitAnalysis(
        best_card_name=best_option.card_name,
        total_potential_benefit=best_option.discount_amount,
        all_options=all_options,
        engine="python",
    )


def _evaluate_candidate(candidate: BenefitCandidateInput, amount: int) -> tuple[int, str]:
    if amount < candidate.min_pay:
        return 0, f"{candidate.min_pay:,}원 이상 결제 시 적용"

    if candidate.last_per < candidate.min_per:
        return 0, f"전월 실적 {candidate.min_per:,}원 필요"

    if candidate.monthly > 0 and candidate.monthly_split >= candidate.monthly:
        return 0, f"월 {candidate.monthly}회 혜택 한도 소진"

    effective_benefit = candidate.benefit
    bonus_msg = ""
    if candidate.visit_count >= 5:
        effective_benefit += 10
        bonus_msg = f" (단골 보너스 +10% 적용, 총 {candidate.visit_count + 1}회 방문)"
    elif candidate.visit_count >= 2:
        effective_benefit += 5
        bonus_msg = f" (재방문 보너스 +5% 적용, 총 {candidate.visit_count + 1}회 방문)"

    discount_amount = amount * effective_benefit // 100
    if candidate.limit_once > 0:
        discount_amount = min(discount_amount, candidate.limit_once)

    if candidate.limit_month > 0:
        remaining_monthly_benefit = max(candidate.limit_month - candidate.accrue_benefit, 0)
        if remaining_monthly_benefit <= 0:
            return 0, f"월 혜택 한도 {candidate.limit_month:,}원 소진"
        discount_amount = min(discount_amount, remaining_monthly_benefit)

    if discount_amount <= 0:
        return 0, "적용 가능한 혜택 없음"

    base_desc = candidate.summary if candidate.summary else f"{effective_benefit}% 할인 적용"
    return discount_amount, f"{base_desc}{bonus_msg}"


def _analyze_best_benefit_python(member_id: int, store_name: str, category: str, amount: int) -> BenefitAnalysis:
    normalized_store = store_name.strip() or "unknown-store"
    normalized_category = category.strip().upper() or "ETC"
    charge_amount = max(int(amount), 0)

    if normalized_category == "CAFE":
        primary_discount = min(max(charge_amount // 2, 1000), 5000)
        secondary_discount = min(max(charge_amount // 5, 500), 2000)
        primary_card_name = "Toss Beneface Card"
        secondary_card_name = "Daily Cashback Card"
    else:
        primary_discount = min(max(charge_amount // 10, 300), 3000)
        secondary_discount = min(max(charge_amount // 20, 100), 1000)
        primary_card_name = "Category Saver Card"
        secondary_card_name = "Daily Cashback Card"

    all_options = [
        BenefitOption(
            card_name=primary_card_name,
            discount_amount=primary_discount,
            benefit_type="DISCOUNT",
            description=f"{normalized_store} {normalized_category} category optimized discount",
        ),
        BenefitOption(
            card_name=secondary_card_name,
            discount_amount=secondary_discount,
            benefit_type="CASHBACK",
            description=f"Fallback cashback option for member {member_id}",
        ),
    ]

    return BenefitAnalysis(
        best_card_name=all_options[0].card_name,
        total_potential_benefit=all_options[0].discount_amount,
        all_options=all_options,
        engine="python",
    )


def _analyze_batch_benefits_candidates_python(batch_input: list[dict]) -> list[dict]:
    results = []
    for item in batch_input:
        candidates = [BenefitCandidateInput(**candidate) for candidate in item["candidates"]]
        if not candidates:
            results.append(
                {
                    "store_name": item["store_name"],
                    "best_card_name": "No Card",
                    "total_potential_benefit": 0,
                }
            )
            continue

        analysis = _analyze_best_benefit_candidates_python(candidates, item["amount"])
        results.append(
            {
                "store_name": item["store_name"],
                "best_card_name": analysis.best_card_name,
                "total_potential_benefit": analysis.total_potential_benefit,
            }
        )
    return results


def _analyze_batch_benefits_python(member_id: int, stores: list[dict]) -> list[dict]:
    results = []
    for store in stores:
        analysis = _analyze_best_benefit_python(
            member_id,
            store["store_name"],
            store.get("category", "ETC"),
            store["amount"],
        )
        results.append(
            {
                "store_name": store["store_name"],
                "best_card_name": analysis.best_card_name,
                "total_potential_benefit": analysis.total_potential_benefit,
            }
        )
    return results

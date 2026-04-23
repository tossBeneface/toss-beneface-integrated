import importlib
import json
import sys
import unittest
from dataclasses import asdict
from pathlib import Path

FASTAPI_ROOT = Path(__file__).resolve().parents[1]
if str(FASTAPI_ROOT) not in sys.path:
    sys.path.insert(0, str(FASTAPI_ROOT))

from benefit_engine import _analyze_batch_benefits_candidates_python, _analyze_best_benefit_candidates_python
from benefit_repository import BenefitCandidateInput


def _candidate(**overrides) -> BenefitCandidateInput:
    base = {
        "card_name": "Toss Beneface Card",
        "card_company": "Toss Bank",
        "shop": "스타벅스",
        "summary": "스타벅스 50% 할인",
        "benefit": 50,
        "limit_once": 0,
        "limit_month": 0,
        "min_pay": 1000,
        "min_per": 0,
        "monthly": 0,
        "last_per": 100000,
        "now_per": 0,
        "monthly_split": 0,
        "accrue_benefit": 0,
        "visit_count": 0,
    }
    base.update(overrides)
    return BenefitCandidateInput(**base)


def _normalize_best_result(result) -> dict:
    if isinstance(result, str):
        return json.loads(result)

    payload = asdict(result)
    payload.pop("engine", None)
    return payload


class RustBenefitEngineRegressionTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls) -> None:
        try:
            cls.rust_module = importlib.import_module("card_benefit_rust")
        except ModuleNotFoundError as exc:
            raise unittest.SkipTest("card_benefit_rust is not installed") from exc

    def test_rust_matches_python_single_store_contract(self) -> None:
        candidates = [
            _candidate(
                card_name="Premium Cafe Card",
                benefit=30,
                limit_once=7000,
                limit_month=10000,
                min_pay=5000,
                min_per=300,
                last_per=500000,
                accrue_benefit=1000,
                visit_count=2,
            ),
            _candidate(
                card_name="Basic Cafe Card",
                benefit=10,
                limit_once=2000,
                summary="스타벅스 10% 할인",
            ),
            _candidate(
                card_name="Locked Card",
                benefit=80,
                min_per=300,
                last_per=10,
                summary="전월 실적 미달 카드",
            ),
        ]

        python_result = _normalize_best_result(_analyze_best_benefit_candidates_python(candidates, 10000))
        rust_result = _normalize_best_result(
            self.rust_module.analyze_best_benefit(
                json.dumps([candidate.to_payload() for candidate in candidates], ensure_ascii=False),
                10000,
            )
        )

        self.assertEqual(rust_result, python_result)

    def test_rust_matches_python_batch_contract(self) -> None:
        batch_input = [
            {
                "store_name": "스타벅스",
                "amount": 10000,
                "candidates": [
                    _candidate(card_name="Premium Cafe Card", benefit=30, visit_count=2).to_payload(),
                    _candidate(card_name="Basic Cafe Card", benefit=10).to_payload(),
                ],
            },
            {
                "store_name": "편의점",
                "amount": 5000,
                "candidates": [
                    _candidate(card_name="Convenience Card", shop="편의점", benefit=20).to_payload(),
                ],
            },
        ]

        python_result = _analyze_batch_benefits_candidates_python(batch_input)
        rust_result = json.loads(
            self.rust_module.analyze_batch_benefits(json.dumps(batch_input, ensure_ascii=False))
        )["results"]

        self.assertEqual(rust_result, python_result)

    def test_rust_grouped_batch_matches_python_contract(self) -> None:
        stores = [
            {"store_name": "스타벅스", "amount": 10000, "visit_count": 2},
            {"store_name": "편의점", "amount": 5000, "visit_count": 0},
        ]
        all_candidates = [
            _candidate(card_name="Premium Cafe Card", benefit=30, shop="스타벅스").to_payload(),
            _candidate(card_name="Basic Cafe Card", benefit=10, shop="스타벅스").to_payload(),
            _candidate(card_name="Convenience Card", benefit=20, shop="편의점").to_payload(),
        ]

        batch_input = [
            {
                "store_name": store["store_name"],
                "amount": store["amount"],
                "candidates": [
                    {**candidate, "visit_count": store["visit_count"]}
                    for candidate in all_candidates
                    if store["store_name"] in candidate["shop"] or candidate["shop"] in store["store_name"]
                ],
            }
            for store in stores
        ]

        python_result = _analyze_batch_benefits_candidates_python(batch_input)
        rust_result = json.loads(
            self.rust_module.analyze_batch_benefits_from_candidates(
                json.dumps(stores, ensure_ascii=False),
                json.dumps(all_candidates, ensure_ascii=False),
            )
        )["results"]

        self.assertEqual(rust_result, python_result)


if __name__ == "__main__":
    unittest.main()

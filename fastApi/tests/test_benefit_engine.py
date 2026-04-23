import sys
import unittest
from pathlib import Path
from unittest.mock import patch

FASTAPI_ROOT = Path(__file__).resolve().parents[1]
if str(FASTAPI_ROOT) not in sys.path:
    sys.path.insert(0, str(FASTAPI_ROOT))

from benefit_repository import BenefitCandidateInput
from benefit_engine import _analyze_best_benefit_python, analyze_best_benefit


class BenefitEngineTest(unittest.TestCase):

    def test_python_fallback_preserves_expected_grpc_contract(self) -> None:
        result = _analyze_best_benefit_python(
            member_id=1,
            store_name="스타벅스",
            category="CAFE",
            amount=10000,
        )

        self.assertEqual(result.engine, "python")
        self.assertEqual(result.best_card_name, "Toss Beneface Card")
        self.assertEqual(result.total_potential_benefit, 5000)
        self.assertEqual(len(result.all_options), 2)

    def test_wrapper_falls_back_when_rust_module_is_missing(self) -> None:
        with patch("benefit_engine.list_member_benefit_inputs", return_value=[]):
            with patch("benefit_engine.importlib.import_module", side_effect=ModuleNotFoundError):
                result = analyze_best_benefit(
                    member_id=1,
                    store_name="스타벅스",
                    category="CAFE",
                    amount=10000,
                )

        self.assertEqual(result.engine, "python")
        self.assertEqual(result.best_card_name, "Toss Beneface Card")

    def test_member_card_data_is_used_when_available_even_without_rust(self) -> None:
        candidates = [
            BenefitCandidateInput(
                card_name="Premium Cafe Card",
                card_company="TestCard",
                shop="스타벅스",
                summary="스타벅스 30% 할인",
                benefit=30,
                limit_once=7000,
                limit_month=10000,
                min_pay=5000,
                min_per=300000,
                monthly=5,
                last_per=500000,
                now_per=100000,
                monthly_split=1,
                accrue_benefit=1000,
            ),
            BenefitCandidateInput(
                card_name="Basic Cafe Card",
                card_company="TestCard",
                shop="스타벅스",
                summary="스타벅스 10% 할인",
                benefit=10,
                limit_once=2000,
                limit_month=5000,
                min_pay=0,
                min_per=0,
                monthly=10,
                last_per=100000,
                now_per=100000,
                monthly_split=0,
                accrue_benefit=0,
            ),
        ]

        with patch("benefit_engine.list_member_benefit_inputs", return_value=candidates):
            with patch("benefit_engine.importlib.import_module", side_effect=ModuleNotFoundError):
                result = analyze_best_benefit(
                    member_id=1,
                    store_name="스타벅스",
                    category="CAFE",
                    amount=10000,
                )

        self.assertEqual(result.engine, "python")
        self.assertEqual(result.best_card_name, "Premium Cafe Card")
        self.assertEqual(result.total_potential_benefit, 3000)
        self.assertEqual(len(result.all_options), 2)


if __name__ == "__main__":
    unittest.main()

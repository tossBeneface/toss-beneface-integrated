import argparse
import json
import random
import statistics
import sys
import time
import importlib
from pathlib import Path

FASTAPI_ROOT = Path(__file__).resolve().parents[1]
if str(FASTAPI_ROOT) not in sys.path:
    sys.path.insert(0, str(FASTAPI_ROOT))

from benefit_engine import _analyze_best_benefit_candidates_python, _store_matches
from benefit_repository import BenefitCandidateInput

def generate_mock_all_candidates(card_count: int, store_count: int, rng: random.Random) -> tuple[list[dict], list[str]]:
    all_candidates = []
    stores = [f"Store {i}" for i in range(store_count)]
    
    for c_idx in range(card_count):
        for s_idx in range(store_count):
            all_candidates.append({
                "card_name": f"Card {c_idx}",
                "card_company": "Test Bank",
                "shop": stores[s_idx],
                "summary": f"Benefit for {stores[s_idx]}",
                "benefit": rng.randint(1, 50),
                "limit_once": 5000,
                "limit_month": 50000,
                "min_pay": 1000,
                "min_per": 0,
                "monthly": 0,
                "last_per": 100000,
                "now_per": 0,
                "monthly_split": 0,
                "accrue_benefit": 0,
                "visit_count": rng.randint(0, 8),
            })
    return all_candidates, stores

def analyze_batch_from_candidates_python(stores: list[dict], all_candidates: list[dict]) -> list[dict]:
    results = []
    for store in stores:
        store_name = store["store_name"]
        candidates = [
            BenefitCandidateInput(**{**candidate, "visit_count": store["visit_count"]})
            for candidate in all_candidates
            if _store_matches(store_name, candidate["shop"])
        ]
        analysis = _analyze_best_benefit_candidates_python(candidates, store["amount"])
        results.append({
            "store_name": store_name,
            "best_card_name": analysis.best_card_name,
            "total_potential_benefit": analysis.total_potential_benefit,
        })
    return results

def run_batch_benchmark(card_count: int = 10, store_count: int = 100, iterations: int = 500, seed: int = 42) -> dict:
    print(f"Starting batch benefit engine benchmark ({store_count} stores x {card_count} cards)")
    rng = random.Random(seed)
    
    all_candidates_raw, stores = generate_mock_all_candidates(card_count, store_count, rng)
    
    store_requests = [
        {
            "store_name": store_name,
            "amount": 10000 + (index % 5) * 1000,
            "visit_count": rng.randint(0, 8),
        }
        for index, store_name in enumerate(stores)
    ]
    
    stores_json = json.dumps(store_requests, ensure_ascii=False)
    candidates_json = json.dumps(all_candidates_raw, ensure_ascii=False)
    rust_module = importlib.import_module("card_benefit_rust")

    python_contract = analyze_batch_from_candidates_python(store_requests, all_candidates_raw)
    rust_contract = json.loads(
        rust_module.analyze_batch_benefits_from_candidates(stores_json, candidates_json)
    )["results"]
    if rust_contract != python_contract:
        raise AssertionError("Rust grouped batch result does not match Python contract")

    print("Running Python grouped batch processor...")
    py_latencies = []
    for _ in range(iterations):
        start = time.perf_counter()
        analyze_batch_from_candidates_python(store_requests, all_candidates_raw)
        py_latencies.append((time.perf_counter() - start) * 1000)
    
    print("Running Rust grouped batch processor...")
    rs_latencies = []
    for _ in range(iterations):
        start = time.perf_counter()
        rust_module.analyze_batch_benefits_from_candidates(stores_json, candidates_json)
        rs_latencies.append((time.perf_counter() - start) * 1000)

    py_avg = statistics.mean(py_latencies)
    rs_avg = statistics.mean(rs_latencies)
    speedup = py_avg / rs_avg if rs_avg else 0
    
    print(f"\nBatch processing results ({store_count} stores x {card_count} cards)")
    print("-" * 50)
    print(f"[Python] Avg Latency: {py_avg:.4f}ms")
    print(f"[Rust]   Avg Latency: {rs_avg:.4f}ms")
    print("-" * 50)
    print(f"Speedup ratio (Python avg / Rust avg): {speedup:.2f}x")

    return {
        "store_count": store_count,
        "card_count": card_count,
        "iterations": iterations,
        "python_avg_ms": round(py_avg, 6),
        "rust_avg_ms": round(rs_avg, 6),
        "speedup": round(speedup, 2),
    }

def parse_args():
    parser = argparse.ArgumentParser(description="Benchmark Python vs Rust batch card benefit engines.")
    parser.add_argument("--iterations", type=int, default=500)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--card-count", type=int, default=10)
    parser.add_argument("--store-count", type=int, default=100)
    parser.add_argument("--json-out", type=str, default="")
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()
    benchmark_result = run_batch_benchmark(
        card_count=args.card_count,
        store_count=args.store_count,
        iterations=args.iterations,
        seed=args.seed,
    )
    if args.json_out:
        with open(args.json_out, "w", encoding="utf-8") as f:
            json.dump(benchmark_result, f, ensure_ascii=False, indent=2)
        print(f"\nWrote benchmark results to {args.json_out}")

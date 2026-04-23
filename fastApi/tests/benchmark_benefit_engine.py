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

from benefit_engine import BenefitCandidateInput, _analyze_best_benefit_candidates_python

def generate_mock_candidates(count: int, rng: random.Random) -> list[BenefitCandidateInput]:
    candidates = []
    for i in range(count):
        candidates.append(BenefitCandidateInput(
            card_name=f"Card {i}",
            card_company="Test Bank",
            shop="스타벅스",
            summary=f"Benefit summary {i}",
            benefit=rng.randint(1, 50),
            limit_once=rng.randint(1000, 10000),
            limit_month=rng.randint(10000, 50000),
            min_pay=rng.randint(0, 5000),
            min_per=rng.randint(0, 300000),
            monthly=rng.randint(0, 10),
            last_per=rng.randint(0, 500000),
            now_per=rng.randint(0, 100000),
            monthly_split=rng.randint(0, 5),
            accrue_benefit=rng.randint(0, 5000),
            visit_count=rng.randint(0, 8),
        ))
    return candidates

def benchmark_engine(engine_name, func, candidates, amount, iterations=1000):
    latencies = []
    for _ in range(iterations):
        start = time.perf_counter()
        func(candidates, amount)
        end = time.perf_counter()
        latencies.append((end - start) * 1000)  # ms
    
    avg_latency = statistics.mean(latencies)
    p99_latency = statistics.quantiles(latencies, n=100)[98]
    throughput = iterations / (sum(latencies) / 1000)
    
    return {
        "engine": engine_name,
        "avg_ms": round(avg_latency, 6),
        "p99_ms": round(p99_latency, 6),
        "throughput_ops": round(throughput, 2),
    }

def run_benchmarks(iterations: int = 2000, seed: int = 42) -> list[dict]:
    print("Starting single-store benefit engine benchmark")
    amount = 10000
    rng = random.Random(seed)

    try:
        rust_module = importlib.import_module("card_benefit_rust")
        def rust_func(candidates, amt):
            return rust_module.analyze_best_benefit(
                json.dumps([c.to_payload() for c in candidates], ensure_ascii=False),
                amt
            )
    except Exception as e:
        raise SystemExit(f"Failed to load Rust module: {e}") from e

    python_func = _analyze_best_benefit_candidates_python

    test_cases = [
        ("small", 5, generate_mock_candidates(5, rng)),
        ("medium", 20, generate_mock_candidates(20, rng)),
        ("large", 100, generate_mock_candidates(100, rng)),
    ]

    results = []
    for label, candidate_count, candidates in test_cases:
        print(f"\nCase: {label} ({candidate_count} candidates)")
        print("-" * 50)

        py_result = benchmark_engine("Python", python_func, candidates, amount, iterations)
        rs_result = benchmark_engine("Rust", rust_func, candidates, amount, iterations)

        for res in [py_result, rs_result]:
            print(
                f"[{res['engine']}] "
                f"Avg: {res['avg_ms']:.4f}ms, "
                f"P99: {res['p99_ms']:.4f}ms, "
                f"Throughput: {res['throughput_ops']:.2f} ops/sec"
            )

        speedup = round(py_result["avg_ms"] / rs_result["avg_ms"], 2) if rs_result["avg_ms"] else 0
        print(f"Speedup ratio (Python avg / Rust avg): {speedup:.2f}x")
        results.append({
            "case": label,
            "candidate_count": candidate_count,
            "iterations": iterations,
            "python": py_result,
            "rust": rs_result,
            "speedup": speedup,
        })

    return results

def parse_args():
    parser = argparse.ArgumentParser(description="Benchmark Python vs Rust card benefit engines.")
    parser.add_argument("--iterations", type=int, default=2000)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--json-out", type=str, default="")
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()
    benchmark_results = run_benchmarks(iterations=args.iterations, seed=args.seed)
    if args.json_out:
        with open(args.json_out, "w", encoding="utf-8") as f:
            json.dump(benchmark_results, f, ensure_ascii=False, indent=2)
        print(f"\nWrote benchmark results to {args.json_out}")

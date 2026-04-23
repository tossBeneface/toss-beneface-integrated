import time
import json
import random
import statistics
import importlib
from dataclasses import asdict
from benefit_engine import BenefitCandidateInput, _analyze_best_benefit_candidates_python

def generate_mock_candidates(count: int) -> list[BenefitCandidateInput]:
    candidates = []
    for i in range(count):
        candidates.append(BenefitCandidateInput(
            card_name=f"Card {i}",
            card_company="Test Bank",
            shop="스타벅스",
            summary=f"Benefit summary {i}",
            benefit=random.randint(1, 50),
            limit_once=random.randint(1000, 10000),
            limit_month=random.randint(10000, 50000),
            min_pay=random.randint(0, 5000),
            min_per=random.randint(0, 300000),
            monthly=random.randint(0, 10),
            last_per=random.randint(0, 500000),
            now_per=random.randint(0, 100000),
            monthly_split=random.randint(0, 5),
            accrue_benefit=random.randint(0, 5000)
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
        "avg_ms": avg_latency,
        "p99_ms": p99_latency,
        "throughput_ops": throughput
    }

def run_benchmarks():
    print("🚀 Starting Benefit Engine Benchmark...")
    amount = 10000
    iterations = 2000
    
    # Load Rust module
    try:
        rust_module = importlib.import_module("card_benefit_rust")
        def rust_func(candidates, amt):
            return rust_module.analyze_best_benefit(
                json.dumps([c.to_payload() for c in candidates], ensure_ascii=False),
                amt
            )
    except Exception as e:
        print(f"❌ Failed to load Rust module: {e}")
        return

    python_func = _analyze_best_benefit_candidates_python

    test_cases = [
        ("Small (5 candidates)", generate_mock_candidates(5)),
        ("Medium (20 candidates)", generate_mock_candidates(20)),
        ("Large (100 candidates)", generate_mock_candidates(100))
    ]

    for label, candidates in test_cases:
        print(f"\n📊 Case: {label}")
        print("-" * 50)
        
        py_result = benchmark_engine("Python", python_func, candidates, amount, iterations)
        rs_result = benchmark_engine("Rust", rust_func, candidates, amount, iterations)
        
        for res in [py_result, rs_result]:
            print(f"[{res['engine']}] Avg: {res['avg_ms']:.4f}ms, P99: {res['p99_ms']:.4f}ms, Throughput: {res['throughput_ops']:.2f} ops/sec")
        
        speedup = py_result['avg_ms'] / rs_result['avg_ms']
        print(f"✅ Speedup: {speedup:.2f}x faster with Rust")

if __name__ == "__main__":
    run_benchmarks()

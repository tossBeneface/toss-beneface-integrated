import time
import json
import random
import statistics
import importlib
from benefit_engine import BenefitCandidateInput, _analyze_batch_benefits_candidates_python

def generate_mock_all_candidates(card_count: int, store_count: int) -> list[dict]:
    # 사용자의 카드 10개 x 각 카드당 매장 혜택 100개 = 1000개 데이터
    all_candidates = []
    stores = [f"Store {i}" for i in range(store_count)]
    
    for c_idx in range(card_count):
        for s_idx in range(store_count):
            all_candidates.append({
                "card_name": f"Card {c_idx}",
                "card_company": "Test Bank",
                "shop": stores[s_idx],
                "summary": f"Benefit for {stores[s_idx]}",
                "benefit": random.randint(1, 50),
                "limit_once": 5000,
                "limit_month": 50000,
                "min_pay": 1000,
                "min_per": 0,
                "monthly": 0,
                "last_per": 100000,
                "now_per": 0,
                "monthly_split": 0,
                "accrue_benefit": 0
            })
    return all_candidates, stores

def run_batch_benchmark():
    print("🚀 Starting BATCH Benefit Engine Benchmark (100 Stores)...")
    card_count = 10
    store_count = 100
    iterations = 500
    
    all_candidates_raw, stores = generate_mock_all_candidates(card_count, store_count)
    
    # Prepare batch input format
    batch_input = []
    for store_name in stores:
        candidates = [c for c in all_candidates_raw if c["shop"] == store_name]
        batch_input.append({
            "store_name": store_name,
            "amount": 10000,
            "candidates": candidates
        })
    
    batch_json = json.dumps(batch_input, ensure_ascii=False)

    # 1. Python Benchmark
    print("🐍 Running Python Batch Processor...")
    py_latencies = []
    for _ in range(iterations):
        start = time.perf_counter()
        _analyze_batch_benefits_candidates_python(batch_input)
        py_latencies.append((time.perf_counter() - start) * 1000)
    
    # 2. Rust Benchmark
    print("🦀 Running Rust Batch Processor...")
    rust_module = importlib.import_module("card_benefit_rust")
    rs_latencies = []
    for _ in range(iterations):
        start = time.perf_counter()
        rust_module.analyze_batch_benefits(batch_json)
        rs_latencies.append((time.perf_counter() - start) * 1000)

    py_avg = statistics.mean(py_latencies)
    rs_avg = statistics.mean(rs_latencies)
    
    print("\n📊 Batch Processing Results (100 Stores x 10 Cards)")
    print("-" * 50)
    print(f"[Python] Avg Latency: {py_avg:.4f}ms")
    print(f"[Rust]   Avg Latency: {rs_avg:.4f}ms")
    print("-" * 50)
    print(f"✅ Speedup: {py_avg / rs_avg:.2f}x faster with Rust")

if __name__ == "__main__":
    run_batch_benchmark()

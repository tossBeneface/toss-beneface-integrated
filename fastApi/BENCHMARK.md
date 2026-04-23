# Benefit Engine Benchmark

This directory contains two kinds of verification for the card benefit engine.

## Regression Tests

Run this when checking correctness. It verifies the Python fallback contract and, when the Rust wheel is installed, checks that Rust returns the same contract as Python.

```bash
cd fastApi
pytest -q
```

## Rust Wheel Build

Run this before benchmarking if `card_benefit_rust` is not installed in the current Python environment.

```bash
cd fastApi
rm -rf rust/card_benefit_engine/target/wheels
maturin build --manifest-path rust/card_benefit_engine/Cargo.toml --release --interpreter python3
pip3 install --force-reinstall "$(find rust/card_benefit_engine/target/wheels -maxdepth 1 -name '*.whl' | head -n 1)"
```

## Single-Store Benchmark

```bash
cd fastApi
python3 tests/benchmark_benefit_engine.py --iterations 2000 --seed 42 --json-out benchmark-single.json
```

## Grouped Batch Benchmark

```bash
cd fastApi
python3 tests/benchmark_batch_engine.py --iterations 500 --seed 42 --card-count 10 --store-count 100 --json-out benchmark-batch.json
```

---

## Results (macOS ARM64, Apple M-series, 2026-04-24)

### Single-Store: Python vs Rust (PyO3 + JSON boundary)

> **Note:** Single-store calls include PyO3 serialization overhead (JSON encode → Rust → JSON decode).
> For small inputs, this overhead dominates. The batch benchmark is the real optimization target.

| Case | Candidates | Engine | Avg (ms) | P99 (ms) | Throughput (ops/s) |
|------|-----------|--------|----------|----------|--------------------|
| small | 5 | Python | 0.0115 | 0.0378 | 86,782 |
| small | 5 | **Rust** | 0.1590 | 0.6773 | 6,289 |
| medium | 20 | Python | 0.1502 | 1.6567 | 6,660 |
| medium | 20 | **Rust** | 0.6018 | 3.8711 | 1,662 |
| large | 100 | Python | 0.2605 | 0.6865 | 3,839 |
| large | 100 | **Rust** | 2.1115 | 3.5807 | 474 |

**해석:** 단건 호출은 PyO3 경계 비용(JSON 직렬화/역직렬화)이 순수 계산 비용보다 크기 때문에 Python이 빠릅니다. 이는 설계 상 의도된 트레이드오프입니다 — 실제 서비스에서 단건 경로는 DB I/O 시간이 지배적이므로 엔진 차이는 무의미합니다.

### Grouped Batch: Python vs Rust (100 stores × 10 cards)

Python은 가게별로 100번 반복 호출하지만, Rust는 모든 가게·카드 데이터를 한 번에 받아 내부에서 그룹핑·매칭을 수행합니다.

| Engine | Avg Latency (ms) | Speedup |
|--------|-----------------|---------|
| Python | 41.64 | 1× |
| **Rust** | **2.62** | **15.88×** |

**결론:** 배치 경로에서 Rust는 Python 대비 **약 16배** 빠릅니다. 결제 시점에 여러 가맹점 혜택을 동시에 추천하는 실서비스 시나리오(예: 결제 내역 분석 후 N개 가게 최적 카드 추천)에서 핵심 성능 이점을 제공합니다.

### 면접 발언 가이드

> "Rust 엔진의 진짜 강점은 단건 호출이 아니라 배치 처리입니다.
> 단건은 PyO3 JSON 경계 비용이 계산 비용을 초과하지만, 100개 가맹점 배치에서는 Python 대비 약 16배 빠릅니다.
> 이는 결제 시 여러 가맹점의 최적 카드를 실시간으로 추천하는 시나리오에서 p99 레이턴시를 수십 ms에서 수 ms로 낮춰줍니다."

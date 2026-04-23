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
python -m maturin build --manifest-path rust/card_benefit_engine/Cargo.toml --release --interpreter python
python -m pip install --force-reinstall "$(find rust/card_benefit_engine/target/wheels -maxdepth 1 -name '*.whl' | head -n 1)"
```

## Single-Store Benchmark

```bash
cd fastApi
python tests/benchmark_benefit_engine.py --iterations 2000 --seed 42 --json-out benchmark-single.json
```

## Grouped Batch Benchmark

```bash
cd fastApi
python tests/benchmark_batch_engine.py --iterations 500 --seed 42 --card-count 10 --store-count 100 --json-out benchmark-batch.json
```

Use the JSON outputs as reproducible interview evidence. Latency numbers vary by machine, so quote the command, dataset size, and speedup together.

The single-store benchmark includes PyO3 and JSON boundary overhead, so it can be slower than pure Python for very small inputs. The grouped batch benchmark is the intended optimization target: Python hands Rust all store requests and all member card-benefit candidates once, then Rust performs store matching, grouping, and best-card selection internally.

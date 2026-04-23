from prometheus_client import Counter, Histogram

# Rust/Python 엔진 처리 시간 (Latency)
BENEFIT_ENGINE_LATENCY = Histogram(
    'card_benefit_engine_latency_seconds',
    'Latency of card benefit analysis in seconds',
    ['engine_type', 'is_batch']
)

# 엔진별 요청 횟수 (Throughput)
BENEFIT_ENGINE_REQUESTS = Counter(
    'card_benefit_engine_requests_total',
    'Total number of card benefit analysis requests',
    ['engine_type', 'is_batch']
)

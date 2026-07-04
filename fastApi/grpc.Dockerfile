# gRPC 혜택 분석 서버 전용 슬림 이미지 (dlib/opencv 등 HTTP 서버 의존성 제외)
# syntax=docker/dockerfile:1.7
FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y --profile minimal
ENV PATH="/root/.cargo/bin:${PATH}"

RUN pip install --no-cache-dir \
    "grpcio>=1.62.2" "grpcio-tools>=1.62.2" "protobuf>=4.25.1" \
    "psycopg[binary]==3.3.3" prometheus-client==0.20.0 "maturin>=1.13,<2"

COPY protos ./protos
RUN python -m grpc_tools.protoc -I protos --python_out . --grpc_python_out . protos/card_benefit.proto

COPY rust/card_benefit_engine ./rust/card_benefit_engine
RUN python -m maturin build --manifest-path rust/card_benefit_engine/Cargo.toml --release \
    && pip install "$(find rust/card_benefit_engine/target/wheels -maxdepth 1 -name '*.whl' | head -n 1)"

COPY grpc_server.py benefit_engine.py benefit_repository.py metrics.py db.py ./

EXPOSE 50051
CMD ["python", "grpc_server.py"]

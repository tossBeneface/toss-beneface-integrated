# TossBeneface K8s Migration Strategy

이 문서는 현재 `docker-compose.local.yml` 기반의 로컬 개발 환경을 Kubernetes(K8s) 클러스터로 이식하기 위한 전략을 정리합니다.

## 1. 애플리케이션 아키텍처 (Pod 구성 전략)

### Option A: Sidecar 패턴 (추천)
- **구성:** 하나의 Pod 안에 `spring-boot-backend` 컨테이너와 `fastapi-rust-engine` 컨테이너를 함께 배치.
- **장점:**
    - **gRPC 성능 극대화:** `localhost:50051`을 통해 통신하므로 네트워크 지연(Latency)이 거의 없음.
    - **단순한 서비스 관리:** 두 서비스의 라이프사이클을 하나로 관리 가능.
- **단점:** 컨테이너 단위의 독립적인 스케일링이 불가능함.

### Option B: 독립 Deployment 패턴
- **구성:** Spring Boot와 FastAPI를 각각 별도의 Deployment와 Service로 분리.
- **장점:** 각 서비스의 부하에 따라 개별적으로 Horizontal Pod Autoscaler(HPA) 적용 가능. (예: Rust 엔진 연산이 많으면 FastAPI만 증설)
- **단점:** K8s ClusterIP를 통한 네트워크 통신 오버헤드 발생.

## 2. 인프라 서비스 이식

| 서비스 | K8s 이식 방안 | 비고 |
| :--- | :--- | :--- |
| **PostgreSQL** | StatefulSet + PVC | 또는 AWS RDS / GCP Cloud SQL 활용 권장 |
| **Kafka** | Strimzi Operator 활용 | KRaft 모드 유지 또는 Managed Service(MSK 등) 활용 |
| **Redis** | Redis Operator 또는 Helm Chart | 분산 캐시로 활용 |
| **Prometheus** | Prometheus Operator (kube-prometheus-stack) | ServiceMonitor를 통해 자동 Target 탐색 |
| **Grafana** | Helm Chart | ConfigMap을 통한 대시보드 코드화(IaC) |

## 3. 주요 설정 변경 사항 (ConfigMap/Secret)

- **gRPC Endpoint:** Sidecar 시 `localhost:50051`, 분리 시 `fastapi-service:50051`.
- **Kafka Bootstrap:** `kafka-cluster-bootstrap:9092`.
- **Database URL:** Secret으로 관리되는 DB Credentials 연동.

## 4. 모니터링 및 가시성 (Observability)

- **FastAPI Metrics:** `pod` 또는 `service` 라벨을 통해 어떤 파드에서 Rust 엔진이 더 많이 활약하는지 시각화.
- **Alertmanager:** K8s 내부에 Alertmanager Config Secret을 생성하여 Slack Webhook URL 관리.

## 5. 단계별 이식 로드맵

1.  **Phase 1:** Docker 이미지를 Container Registry(ECR/GCR 등)에 푸시.
2.  **Phase 2:** DB 및 Kafka 등 인프라 서비스를 먼저 클러스터에 배포.
3.  **Phase 3:** 애플리케이션 Pod(Sidecar 권장) 배포 및 gRPC 통신 확인.
4.  **Phase 4:** Prometheus ServiceMonitor를 등록하여 `/metrics` 수집 자동화.

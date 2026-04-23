# TossBeneface K8s Migration Strategy

현재 `docker-compose.local.yml` 기반 로컬 환경을 Kubernetes 클러스터로 이식하기 위한 전략 및 아키텍처 설계 문서.

---

## 현재 구조 (docker-compose)

```
┌─────────────────────────────────────────────────────────┐
│                    Host Network                          │
│                                                          │
│  [Nginx :80] ──► [Spring Boot :8080] ──gRPC──► [FastAPI :50051]
│                         │                          │     │
│                    [PostgreSQL]    [Kafka]    [Redis]    │
│                                                          │
│  [Prometheus :9090] ◄── scrape ── /actuator/prometheus  │
│  [Grafana :3000]    ◄── query  ── Prometheus            │
└─────────────────────────────────────────────────────────┘
```

**문제점:**
- 서비스 간 통신이 host network를 거쳐 불필요한 오버헤드 발생
- Spring Boot와 FastAPI의 스케일링 단위가 묶여 있음
- gRPC가 `localhost:50051`이지만 실제론 컨테이너 간 브리지 네트워크

---

## K8s 목표 구조

### Option A: Sidecar 패턴 (gRPC 레이턴시 최소화 우선)

```
┌─────────────────────────────────────────────────────────────┐
│  Ingress (Nginx Ingress Controller)                          │
│     /api/  ──► backend-service (ClusterIP :8080)            │
└─────────────────────────────────────────────────────────────┘
                         │
         ┌───────────────▼────────────────┐
         │   Pod: tossbeneface-backend     │
         │  ┌──────────────────────────┐  │
         │  │  spring-boot (port 8080) │  │  ← 메인 컨테이너
         │  │                          │  │
         │  │  gRPC → localhost:50051  │  │  ← 네트워크 없이 loopback
         │  └──────────────────────────┘  │
         │  ┌──────────────────────────┐  │
         │  │  fastapi-rust (port      │  │  ← 사이드카 컨테이너
         │  │  50051 / 8000)           │  │
         │  └──────────────────────────┘  │
         │  공유: emptyDir (로그)          │
         └────────────────────────────────┘
                  │              │
         ┌────────▼──┐    ┌──────▼──────┐
         │ Kafka     │    │ PostgreSQL   │
         │ StatefulSet│   │ StatefulSet  │
         └───────────┘    └─────────────┘
```

**선택 기준:** gRPC 호출이 결제 직전 실시간 경로에 있어 레이턴시가 핵심일 때.
loopback 통신으로 네트워크 홉을 제거하고 동일 Pod 내 공유 메모리 IPC도 활용 가능.

---

### Option B: 독립 Deployment 패턴 (AI 부하 독립 스케일링 우선)

```
┌─────────────────────────────────────────────────────────────┐
│  Ingress                                                     │
│  /api/ ──► backend-svc    /ai/ ──► fastapi-svc              │
└─────────────────────────────────────────────────────────────┘
        │                                  │
┌───────▼────────┐              ┌──────────▼──────────┐
│ Deployment     │              │ Deployment           │
│ spring-boot    │──gRPC──────► │ fastapi-rust         │
│ replicas: 3    │  ClusterIP   │ replicas: 5 (HPA)    │
│ HPA: CPU 60%   │  :50051      │ HPA: CPU 70%         │
└────────────────┘              └──────────────────────┘
```

**선택 기준:** AI 추론 부하가 웹 API 트래픽과 독립적으로 증가할 때.
Rust 엔진 연산이 급증해도 Spring Boot를 늘리지 않고 FastAPI만 수평 확장 가능.

---

## 핵심 선택 기준 비교

| 항목 | Sidecar (A) | 독립 Deployment (B) |
|---|---|---|
| gRPC 레이턴시 | loopback (< 0.1ms) | ClusterIP (1~3ms) |
| 스케일링 단위 | Pod 전체 | 컨테이너별 독립 |
| 배포 복잡도 | 낮음 | 높음 (서비스 디스커버리 필요) |
| 장애 격리 | Spring Boot 크래시 시 FastAPI도 재시작 | 완전 독립 |
| 추천 시나리오 | 결제 실시간 추천, 레이턴시 SLA < 5ms | 배치 AI, 트래픽 패턴이 다른 경우 |

**TossBeneface 결론:** 결제 직전 실시간 혜택 추천은 레이턴시가 핵심 → **Option A (Sidecar)** 로 시작, 트래픽 패턴 확인 후 B로 분리.

---

## Sidecar Pod YAML 예시

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tossbeneface-backend
  namespace: tossbeneface
spec:
  replicas: 2
  selector:
    matchLabels:
      app: tossbeneface-backend
  template:
    metadata:
      labels:
        app: tossbeneface-backend
    spec:
      containers:
        # ── 메인: Spring Boot ──────────────────────────────
        - name: spring-boot
          image: tossbeneface/backend:latest
          ports:
            - containerPort: 8080
          env:
            - name: GRPC_TARGET
              value: "localhost:50051"       # sidecar이므로 loopback
            - name: SPRING_DATASOURCE_URL
              valueFrom:
                secretKeyRef:
                  name: tossbeneface-secrets
                  key: db-url
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 20
            periodSeconds: 10

        # ── 사이드카: FastAPI + Rust engine ───────────────
        - name: fastapi-rust
          image: tossbeneface/fastapi:latest
          ports:
            - containerPort: 50051   # gRPC
            - containerPort: 8000    # HTTP (메트릭 전용)
          env:
            - name: KAFKA_BOOTSTRAP
              value: "kafka-cluster-bootstrap:9092"
          readinessProbe:
            exec:
              command: ["python3", "-c", "import card_benefit_rust"]
            initialDelaySeconds: 10
            periodSeconds: 15
          resources:
            requests:
              cpu: "500m"
              memory: "512Mi"
            limits:
              cpu: "2"
              memory: "1Gi"

      volumes:
        - name: shared-logs
          emptyDir: {}
```

---

## 인프라 서비스 이식 계획

| 서비스 | K8s 이식 방안 | 비고 |
|---|---|---|
| **PostgreSQL** | StatefulSet + PVC | 프로덕션은 RDS/Cloud SQL 권장 |
| **Kafka** | Strimzi Operator (KRaft 유지) | MSK 등 Managed Service 고려 |
| **Redis** | Redis Operator 또는 Helm | `appendonly yes` 유지 |
| **Prometheus** | kube-prometheus-stack Helm | ServiceMonitor로 자동 Target 탐색 |
| **Grafana** | Helm + ConfigMap | 대시보드를 코드로 관리 (IaC) |

---

## ConfigMap / Secret 분리 전략

```
docker-compose 환경변수          K8s 이식 후
─────────────────────────────────────────────────
POSTGRES_PASSWORD=ai0310    →  Secret: tossbeneface-secrets
KAFKA_BOOTSTRAP=kafka:29092 →  ConfigMap: tossbeneface-config
GRPC_TARGET=fastapi:50051   →  ConfigMap: localhost:50051 (Sidecar 시)
```

---

## 단계별 이식 로드맵

```
Phase 1  컨테이너 이미지 빌드 + Registry(ECR/GCR) 푸시
         └─ Dockerfile 멀티스테이지 빌드 검증

Phase 2  인프라 선행 배포
         └─ PostgreSQL StatefulSet → Kafka Strimzi → Redis

Phase 3  애플리케이션 Pod 배포 (Sidecar)
         └─ gRPC localhost:50051 통신 확인
         └─ /actuator/health readiness gate 통과 후 트래픽 개방

Phase 4  Observability 연결
         └─ ServiceMonitor 등록 → Prometheus 자동 수집
         └─ Grafana: Rust 엔진 vs Python 폴백 비율 대시보드

Phase 5  HPA 튜닝
         └─ CPU 메트릭 기반 자동 스케일링 임계값 설정
         └─ 필요 시 Sidecar → 독립 Deployment 분리
```

---

## 면접 발언 가이드

> "현재 docker-compose에서 Spring Boot와 FastAPI는 각자 포트를 열고 통신하지만,
> K8s에서는 같은 Pod에 Sidecar로 묶어서 gRPC를 loopback으로 처리합니다.
> 네트워크 홉이 사라지므로 ClusterIP 경유 대비 1~3ms 절감되고,
> 결제 직전 실시간 혜택 추천처럼 레이턴시 SLA가 타이트한 경로에 유리합니다.
> 단, AI 부하가 독립적으로 급증하는 시나리오에서는 독립 Deployment로 분리해서
> FastAPI만 HPA로 수평 확장하는 방향으로 전환할 수 있습니다."

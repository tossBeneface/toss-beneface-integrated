# 🎯 우선순위 실행 계획 (PRIORITY_EXECUTION_PLAN)

`TOTAL_STRATEGY_PLAN.md`를 바탕으로 현재 구현 상태를 점검하고, 다음 면접 전까지 가장 임팩트가 큰 작업들을 우선순위별로 정리했습니다.

---

## 📊 현재 구현 상태 점검 (Status Check)

| 항목 | 상태 | 확인 내용 |
| :--- | :--- | :--- |
| **Kotlin 전환** | ✅ 완료 | 100% Kotlin (189개 파일) |
| **Kafka & Outbox** | ✅ 완료 | `OutboxPublisher`, `VoiceController` 연동 완료 |
| **FastAPI 비동기** | ✅ 완료 | `VoiceKafkaPipeline` 구현 완료 |
| **Spring Batch** | ✅ 완료 | `DailySettlementJobConfig`, Migration V6 완료 |
| **OAuth2** | ⚠️ 부분 완료 | Migration V5 완료, `SecurityConfig` 확인 필요 |
| **모니터링** | ❌ 누락 | `monitoring/` 폴더는 있으나 `docker-compose.local.yml`에 없음 |
| **PyO3/gRPC** | ❌ 미구현 | 계획엔 있으나 코드 및 실무 적용 사례 없음 (면접 약점) |

---

## 🚀 우선순위 로드맵 (Priority Roadmap)

### 🥇 1순위: 기술적 깊이 증명 (PyO3 또는 gRPC 통합)
*   **목표:** "러스트를 섞어 인티그레이션" 한다는 발언에 대한 실질적인 증거 마련.
*   **액션:** 
    1.  FastAPI의 핵심 AI 로직(예: 음성 파싱이나 데이터 전처리)의 일부를 Rust(PyO3)로 모듈화하여 성능 향상 증명.
    2.  또는 Spring Boot와 FastAPI 간의 통신을 gRPC(Protobuf)로 전환하여 HTTP/1.1 대비 저지연성 확보.
*   **이유:** 지난 면접에서 가장 "답변이 얕다"고 평가받은 부분임.

### 🥈 2순위: 가시성 및 운영 안정성 (Prometheus/Grafana 가동)
*   **목표:** "모니터링을 통해 장애를 선제적으로 감지한다"는 말을 코드로 증명.
*   **액션:** 
    1.  `docker-compose.local.yml`에 Prometheus와 Grafana 서비스 추가.
    2.  Spring Boot Actuator 메트릭을 Prometheus가 수집하도록 설정.
    3.  Kafka Consumer Lag을 Grafana에서 시각화.
*   **이유:** 인프라 및 운영 역량을 보여주는 가장 직관적인 도구임.

### 🥉 3순위: 인터뷰 서사 리마스터 (Soft Skills Script)
*   **목표:** "열정 식음", "MZ 언급" 등의 리스크 발언을 완벽히 교정.
*   **액션:** 
    1.  `INTERVIEW_SCRIPT.md` 작성.
    2.  열정의 전환(블록체인 -> 실용 AI)을 2문장으로 압축.
    3.  세대 차이가 아닌 '협업 원칙'으로 답변 프레임 전환.
*   **이유:** 기술이 완벽해도 태도나 답변 프레임에서 점수가 깎이면 치명적임.

### 4순위: Sidecar 패턴 배포 전략 문서화
*   **목표:** Python과 Java의 혼용 환경을 어떻게 관리하는지 엔지니어링적 정석 제시.
*   **액션:** 
    1.  현재의 `docker-compose` 구조가 K8s의 Sidecar 패턴으로 어떻게 이식될 수 있는지 아키텍처 다이어그램(Text) 및 설명 추가.
*   **이유:** AI 에이전트 전문 회사에서 기대하는 '에이전트 서빙 아키텍처' 지식 증명.

---

## 🛠 즉시 실행 제안 (Immediate Actions)

지금 바로 **1순위(gRPC 또는 PyO3 기초 구축)** 혹은 **2순위(모니터링 인프라 수정)** 중 어느 것을 먼저 시작할까요? 

개인적으로는 **1순위(기술적 깊이)**가 면접 결과에 가장 결정적일 것으로 보입니다.

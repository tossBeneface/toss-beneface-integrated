#!/bin/bash
# =====================================================
# Kafka 토픽 초기화 스크립트
# kafka-init 컨테이너에서 1회 실행 후 종료
# =====================================================

BROKER="kafka:9092"

echo ">>> Kafka 토픽 생성 시작..."

create_topic() {
  local TOPIC=$1
  local PARTITIONS=$2
  local RETENTION_MS=$3

  kafka-topics \
    --bootstrap-server "$BROKER" \
    --create \
    --if-not-exists \
    --topic "$TOPIC" \
    --partitions "$PARTITIONS" \
    --replication-factor 1 \
    --config retention.ms="$RETENTION_MS"

  echo "  ✅ $TOPIC (partitions=$PARTITIONS, retention=${RETENTION_MS}ms)"
}

# ─────────────────────────────────────────────────────
# 주문 이벤트 토픽
# Partition Key: cafe_id → 같은 카페 주문은 같은 Partition → 순서 보장
# ─────────────────────────────────────────────────────
create_topic "order.created"       3  604800000   # 7일 (주문 처리 완료 전까지 보관)
create_topic "order.status.updated" 3  604800000   # 7일 (PREPARING → READY → COMPLETED)

# ─────────────────────────────────────────────────────
# AI 음성 처리 파이프라인 토픽
# Partition Key: member_id → 한 사용자의 요청은 순서 보장
# 처리 완료 후 결과만 필요하므로 짧은 보존 기간
# ─────────────────────────────────────────────────────
create_topic "voice.requested"  2  3600000    # 1시간 (FastAPI가 소비 후 불필요)
create_topic "voice.completed"  2  3600000    # 1시간 (WebSocket 푸시 후 불필요)

# ─────────────────────────────────────────────────────
# 결제 이벤트 토픽
# Partition Key: member_id
# 정산·알림 Consumer Group이 독립적으로 소비
# ─────────────────────────────────────────────────────
create_topic "payment.completed" 3  2592000000  # 30일 (정산 집계 기간 고려)
create_topic "payment.failed"    3  604800000   # 7일 (모니터링 Alert용)

echo ""
echo ">>> 생성된 토픽 목록:"
kafka-topics --bootstrap-server "$BROKER" --list

echo ""
echo ">>> 토픽 상세 정보:"
kafka-topics --bootstrap-server "$BROKER" --describe \
  --topic order.created,order.status.updated,voice.requested,voice.completed,payment.completed,payment.failed

echo ">>> 토픽 초기화 완료 ✅"

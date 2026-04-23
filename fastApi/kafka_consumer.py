import asyncio
import json
import logging
import os
from typing import Any, Dict, Optional

from aiokafka import AIOKafkaConsumer, AIOKafkaProducer

from models import VoiceData
from routes.voice import generate_voice_response

logger = logging.getLogger(__name__)


BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
VOICE_REQUESTED_TOPIC = os.getenv("VOICE_REQUESTED_TOPIC", "voice.requested")
VOICE_COMPLETED_TOPIC = os.getenv("VOICE_COMPLETED_TOPIC", "voice.completed")
VOICE_CONSUMER_GROUP = os.getenv("VOICE_CONSUMER_GROUP", "fastapi-voice-service")


class VoiceKafkaPipeline:
    def __init__(self) -> None:
        self.consumer: Optional[AIOKafkaConsumer] = None
        self.producer: Optional[AIOKafkaProducer] = None
        self.task: Optional[asyncio.Task] = None

    async def start(self) -> None:
        if self.task is not None:
            return

        self.consumer = AIOKafkaConsumer(
            VOICE_REQUESTED_TOPIC,
            bootstrap_servers=BOOTSTRAP_SERVERS,
            group_id=VOICE_CONSUMER_GROUP,
            auto_offset_reset="earliest",
            enable_auto_commit=False,
            key_deserializer=lambda value: value.decode("utf-8") if value else None,
            value_deserializer=lambda value: json.loads(value.decode("utf-8")),
        )
        self.producer = AIOKafkaProducer(
            bootstrap_servers=BOOTSTRAP_SERVERS,
            key_serializer=lambda value: value.encode("utf-8"),
            value_serializer=lambda value: json.dumps(value, ensure_ascii=False, default=str).encode("utf-8"),
        )

        await self.producer.start()
        try:
            await self.consumer.start()
        except Exception:
            await self.producer.stop()
            self.producer = None
            self.consumer = None
            raise

        self.task = asyncio.create_task(self._consume_loop())
        logger.info("Voice Kafka pipeline started. bootstrap_servers=%s", BOOTSTRAP_SERVERS)

    async def stop(self) -> None:
        if self.task is not None:
            self.task.cancel()
            try:
                await self.task
            except asyncio.CancelledError:
                pass
            finally:
                self.task = None

        if self.consumer is not None:
            await self.consumer.stop()
            self.consumer = None

        if self.producer is not None:
            await self.producer.stop()
            self.producer = None

        logger.info("Voice Kafka pipeline stopped")

    async def _consume_loop(self) -> None:
        assert self.consumer is not None
        assert self.producer is not None

        try:
            async for message in self.consumer:
                payload = message.value
                try:
                    await self._handle_message(payload)
                    await self.consumer.commit()
                except Exception:
                    logger.exception("Voice Kafka message processing failed. payload=%s", payload)
        except asyncio.CancelledError:
            logger.info("Voice Kafka consume loop cancelled")
            raise

    async def _handle_message(self, payload: Dict[str, Any]) -> None:
        assert self.producer is not None

        voice_data = VoiceData(
            text=payload.get("text") or "",
            brand=payload.get("brand"),
            menus=payload.get("menus") or [],
        )
        response = await asyncio.to_thread(generate_voice_response, voice_data)

        completed_event = {
            "requestId": payload["requestId"],
            "memberId": payload["memberId"],
            "result": json.dumps(response, ensure_ascii=False, default=str),
        }

        await self.producer.send_and_wait(
            VOICE_COMPLETED_TOPIC,
            key=str(payload["memberId"]),
            value=completed_event,
        )

        logger.info(
            "Voice Kafka pipeline completed request. requestId=%s, memberId=%s",
            payload["requestId"],
            payload["memberId"],
        )


voice_kafka_pipeline = VoiceKafkaPipeline()

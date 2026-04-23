import logging

import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

load_dotenv()

from face_registry import load_faces_from_api
from kafka_consumer import voice_kafka_pipeline
from routes import face, predict, analysis, ocr, voice, cards

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s - %(message)s")
logger = logging.getLogger(__name__)

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(face.router)
app.include_router(predict.router)
app.include_router(analysis.router)
app.include_router(ocr.router)
app.include_router(voice.router)
app.include_router(cards.router)


from prometheus_client import generate_latest, CONTENT_TYPE_LATEST
from fastapi import Response

@app.get("/metrics")
def metrics():
    return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.on_event("startup")
async def startup_event():
    try:
        load_faces_from_api()
    except Exception:
        logger.exception("Startup face sync failed; FastAPI will continue running.")

    try:
        await voice_kafka_pipeline.start()
    except Exception:
        logger.exception("Voice Kafka pipeline failed to start; FastAPI will continue running.")


@app.on_event("shutdown")
async def shutdown_event():
    await voice_kafka_pipeline.stop()


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)

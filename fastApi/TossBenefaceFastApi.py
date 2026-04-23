import uvicorn
import os
import logging
import threading
import re
import random

from dotenv import load_dotenv
load_dotenv()

import numpy as np
import cv2
import face_recognition
import requests
import pymysql
import joblib
import pandas as pd

from google.cloud import vision
from google.oauth2 import service_account

from fastapi import FastAPI, HTTPException, File, UploadFile, Query
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from openai import OpenAI
from typing import List, Optional

# ------------------------------------------------
# 로깅 설정
# ------------------------------------------------
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s - %(message)s")
logger = logging.getLogger(__name__)

# ------------------------------------------------
# FastAPI 인스턴스 생성 및 CORS 설정
# ------------------------------------------------
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ------------------------------------------------
# 얼굴 인식 — 스레드 안전 FaceRegistry
# ------------------------------------------------
class FaceRegistry:
    """등록된 얼굴 인코딩과 memberId를 스레드 안전하게 관리합니다."""

    def __init__(self):
        self._lock = threading.RLock()
        self._faces: list = []
        self._member_ids: list = []

    def replace_all(self, faces: list, member_ids: list) -> None:
        with self._lock:
            self._faces = faces
            self._member_ids = member_ids

    def extend(self, faces: list, member_ids: list) -> None:
        with self._lock:
            self._faces.extend(faces)
            self._member_ids.extend(member_ids)

    def get_faces(self) -> list:
        with self._lock:
            return list(self._faces)

    def get_member_ids(self) -> list:
        with self._lock:
            return list(self._member_ids)

    def __len__(self) -> int:
        with self._lock:
            return len(self._faces)


face_registry = FaceRegistry()


def _download_face_encodings(faces_data: list) -> tuple[list, list]:
    """faces_data 목록에서 얼굴 인코딩을 추출하여 (faces, member_ids) 튜플로 반환합니다."""
    faces, member_ids = [], []
    for face in faces_data:
        member_id = face["memberId"]
        image_url = face["imageUrl"]
        try:
            img_response = requests.get(image_url, timeout=10)
            if img_response.status_code == 200:
                np_array = np.frombuffer(img_response.content, np.uint8)
                image = cv2.imdecode(np_array, cv2.IMREAD_COLOR)
                encodings = face_recognition.face_encodings(image)
                if encodings:
                    faces.append(encodings[0])
                    member_ids.append(member_id)
            else:
                logger.warning("이미지 다운로드 실패: status=%d, url=%s", img_response.status_code, image_url)
        except Exception as e:
            logger.error("이미지 다운로드 중 예외 발생: %s, url=%s", e, image_url)
    return faces, member_ids


@app.on_event("startup")
def startup_event():
    load_faces_from_api()


def load_faces_from_api() -> None:
    """Spring Boot API에서 전체 얼굴 데이터를 로드하여 registry에 저장합니다."""
    api_url = "https://api.tossbeneface.com/api/faces/all"
    response = requests.get(api_url, timeout=10)
    logger.info("load_faces_from_api response: %s", response)

    if response.status_code != 200:
        logger.error("Spring Boot API 요청 실패. 상태 코드: %d", response.status_code)
        return

    faces, member_ids = _download_face_encodings(response.json())
    face_registry.replace_all(faces, member_ids)
    logger.info("[startup] 등록된 얼굴: %d개", len(face_registry))


def load_last_five_faces_from_api() -> None:
    """최근 5개 얼굴 데이터를 로드하여 registry에 추가합니다."""
    api_url = "https://api.tossbeneface.com/api/faces/last5"
    response = requests.get(api_url, timeout=10)
    logger.info("load_last_five_faces_from_api response: %s", response.text)

    if response.status_code != 200:
        logger.error("Spring Boot API 요청 실패. 상태 코드: %d", response.status_code)
        return

    new_faces, new_member_ids = _download_face_encodings(response.json())
    face_registry.extend(new_faces, new_member_ids)
    logger.info("[refresh] 최근 5개 중 %d개 추가. 총 %d개 보유.", len(new_faces), len(face_registry))


@app.post("/fastapi/refresh-faces")
def refresh_faces():
    load_last_five_faces_from_api()
    return {"message": "Faces reloaded (last 5)"}


@app.post("/fastapi/recognize")
async def recognize_face(file: UploadFile = File(...)):
    """업로드된 이미지에서 얼굴을 인식하고 등록된 얼굴과 비교하여 memberId를 반환합니다."""
    try:
        file_bytes = await file.read()
        np_arr = np.frombuffer(file_bytes, np.uint8)
        img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

        if img is None:
            return JSONResponse(status_code=400, content={"message": "Invalid image file"})

        face_locations = face_recognition.face_locations(img)
        face_encodings = face_recognition.face_encodings(img, face_locations)

        if not face_encodings:
            return JSONResponse(status_code=200, content={"result": "NoFace", "message": "No face detected"})

        known_faces = face_registry.get_faces()
        known_member_ids = face_registry.get_member_ids()

        face_distances = face_recognition.face_distance(known_faces, face_encodings[0])

        if len(face_distances) == 0:
            return JSONResponse(status_code=200, content={"result": "NoMatch", "message": "No faces in DB"})

        best_match_index = int(np.argmin(face_distances))
        THRESHOLD = 0.3
        recognized_member = None

        if face_distances[best_match_index] < THRESHOLD:
            recognized_member = known_member_ids[best_match_index]
            logger.info("[인식 성공] memberId=%s", recognized_member)
        else:
            logger.info("[인식 실패] Threshold(%.1f) 초과 (distance=%.3f)", THRESHOLD, face_distances[best_match_index])

        return JSONResponse(status_code=200, content={"result": "success", "memberId": recognized_member})

    except Exception as e:
        logger.exception("recognize_face 처리 중 오류")
        return JSONResponse(status_code=500, content={"message": f"Error: {str(e)}"})


# ------------------------------------------------
# DB 연결
# ------------------------------------------------
def get_connection():
    return pymysql.connect(
        host=os.getenv('DB_HOST'),
        user=os.getenv('DB_USER'),
        password=os.getenv('DB_PASSWORD'),
        database=os.getenv('DB_NAME'),
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )


def get_connection_local():
    return pymysql.connect(
        host=os.getenv('DB_HOST_LOCAL'),
        user=os.getenv('DB_USER_LOCAL'),
        password=os.getenv('DB_PASSWORD_LOCAL'),
        database=os.getenv('DB_NAME_LOCAL'),
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )


# ------------------------------------------------
# SQL 유틸리티
# ------------------------------------------------
def read_sql_file(file_path: str, separator: str = "-- QUERY_SEPARATOR") -> list:
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read().split(separator)


def fetch_sql_data(connection, sql_file: str, params: list) -> list:
    queries = read_sql_file(sql_file)
    results = []
    with connection.cursor() as cursor:
        for i, query in enumerate(queries):
            query = query.strip()
            if query:
                cursor.execute(query, params[i])
                results.append(cursor.fetchall())
    return results


def insert_analysis_history(connection, store_id, analysis_result):
    query = "INSERT INTO analysis_history (store_id, analysis_script) VALUES (%s, %s)"
    try:
        with connection.cursor() as cursor:
            cursor.execute(query, (store_id, analysis_result))
        connection.commit()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"분석 기록 저장 오류: {str(e)}")


# ------------------------------------------------
# Pydantic 모델
# ------------------------------------------------
class SQLRequest(BaseModel):
    store_params: list
    district_params: list


class ModelInput(BaseModel):
    전월실적: float
    결제금액: float
    혜택받은횟수: float
    이번달실적: float
    혜택받은금액: float
    Benefit: float
    limit_once: float
    limit_month: float
    min_pay: float
    min_per: float
    monthly: float


class VoiceData(BaseModel):
    text: str
    brand: Optional[str] = None
    menus: Optional[List[str]] = None


class CardInfo(BaseModel):
    cardName: str
    cardNumber: str
    cardCompany: str
    expiry: str
    cvc: str
    password: str


class Card(BaseModel):
    cardName: str
    cardCompany: str
    cardImage: str
    amount: int


# ------------------------------------------------
# 예측 모델
# ------------------------------------------------
model_path = r"sql/rf_model_modify.pkl"
with open(model_path, "rb") as f:
    model = joblib.load(f)


@app.post("/fastapi/predict")
async def predict(input_data: ModelInput):
    features = np.array([[
        input_data.전월실적, input_data.결제금액, input_data.혜택받은횟수,
        input_data.이번달실적, input_data.혜택받은금액, input_data.Benefit,
        input_data.limit_once, input_data.limit_month,
        input_data.min_pay, input_data.min_per, input_data.monthly
    ]])
    prediction = model.predict(features)
    return {"prediction": prediction.tolist()}


# ------------------------------------------------
# OpenAI 클라이언트
# ------------------------------------------------
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'
openai_client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


def generate_analysis(store_data, district_data, prediction):
    prompt = f"""
    아래는 가맹점과 상권에 대한 매출 데이터입니다.
    데이터를 분석하여 아래 내용을 포함한 보고서를 작성해주세요:
    - 매출 흐름: 증가, 감소, 변화 패턴 등
    - 주요 매출 요일, 성별, 연령대, 시간대
    - 가맹점과 상권 데이터를 비교한 결과
    - 예측된 매출 흐름

    가맹점 데이터:
    {store_data}

    상권 데이터:
    {district_data}

    예측된 데이터:
    {prediction}

    위 데이터를 비교하여 주요 인사이트를 도출하고, 향후 전략을 제안해주세요.
    각각의 분석 내용을 문단으로 나누어 가독성 좋게 작성해주세요.
    """
    try:
        response = openai_client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "You are an expert data analyst."},
                {"role": "user", "content": prompt},
            ],
            max_tokens=1500,
            temperature=0.7,
        )
        return response.choices[0].message.content.strip()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM 호출 오류: {str(e)}")


@app.post("/fastapi/analyze")
async def analyze_data(request: SQLRequest):
    try:
        connection = get_connection()
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"DB 연결 실패: {str(e)}")

    try:
        store_results = fetch_sql_data(
            connection, "sql/select_store_data.sql",
            [(request.store_params[0]) for _ in range(5)]
        )
        district_results = fetch_sql_data(
            connection, "sql/select_district_data.sql",
            [(request.district_params[0], request.district_params[1]) for _ in range(6)]
        )
        predicted_results = fetch_sql_data(
            connection, "sql/select_predicted_district_data.sql",
            [(request.district_params[0], request.district_params[1]) for _ in range(4)]
        )

        store_data = "\n".join([str(r) for r in store_results])
        district_data = "\n".join([str(r) for r in district_results])
        predicted_data = "\n".join([str(r) for r in predicted_results])
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"SQL 실행 오류: {str(e)}")

    try:
        analysis_result = generate_analysis(store_data, district_data, predicted_data)
        insert_analysis_history(connection, request.store_params[0], analysis_result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM 호출 오류: {str(e)}")
    finally:
        connection.close()

    return {
        "store_data": store_data,
        "district_data": district_data,
        "predicted_data": predicted_data,
        "analysis_result": analysis_result
    }


# ------------------------------------------------
# OCR / 카드 인식
# ------------------------------------------------
json_key_file = "sql/striped-option-449805-v9-4da92e4186de.json"
credentials = service_account.Credentials.from_service_account_file(json_key_file)
vision_client = vision.ImageAnnotatorClient(credentials=credentials)

CSV_PATH = r"sql/cardData_image.csv"


def load_card_data() -> pd.DataFrame:
    if not os.path.exists(CSV_PATH):
        raise FileNotFoundError("CSV 파일을 찾을 수 없습니다.")
    return pd.read_csv(CSV_PATH)


def extract_card_info(text: str) -> tuple[str, str, str, str]:
    """OCR 결과에서 카드번호, 유효기간, CVC, 기타 텍스트를 추출합니다."""
    card_number = re.findall(r'\b\d{4}[\s\-]?\d{4}[\s\-]?\d{4}[\s\-]?\d{4}\b', text)
    card_number = ["".join(re.findall(r'\d', c)) for c in card_number]
    card_number = "\n".join(card_number) if card_number else "[없음]"

    date_info = re.findall(r'\b\d{2}[\/\.\-]\d{2,4}\b', text)
    date_info = [re.sub(r'[\/\.\-]', '', d)[:4] for d in date_info]
    date_info = "\n".join(date_info) if date_info else "[없음]"

    cvc = re.findall(r'\b\d{3,4}\b', text)
    cvc_info = cvc[-1] if cvc else "[없음]"

    other_text = re.sub(
        r'\b(?:\d{4}[\s\-]?\d{4}[\s\-]?\d{4}[\s\-]?\d{4}|\d{2}[\/\.\-]\d{2,4}|\d{3,4})\b',
        '', text
    ).strip() or "[없음]"

    return card_number, date_info, cvc_info, other_text


def get_card_details_by_number(recognized_number: str) -> dict:
    """카드번호 앞 6자리(BIN)로 CSV에서 카드 정보를 조회합니다."""
    try:
        df = load_card_data()
        clean_number = recognized_number.replace(" ", "")
        user_bin = clean_number[:6]

        def parse_bin_list(bin_str):
            return [b.strip().replace(" ", "") for b in str(bin_str).split(",")]

        df["bin_list"] = df["bin"].astype(str).apply(parse_bin_list)
        matched = df[df["bin_list"].apply(lambda bins: user_bin in bins)]

        if not matched.empty:
            matched = matched.drop_duplicates(subset=["카드 이미지", "카드명", "법인"])
            row = matched.iloc[0]
            return {
                "image_url": row["카드 이미지"],
                "card_name": row["카드명"],
                "card_company": row["법인"]
            }
    except Exception as e:
        logger.error("카드 정보 조회 실패: %s", e)

    return {
        "image_url": "https://default-image-url.example.com/default.png",
        "card_name": "[카드명 없음]",
        "card_company": "[카드사 없음]"
    }


@app.post("/fastapi/ocr-card")
async def ocr_card(file: UploadFile = File(...)):
    """업로드된 이미지를 Google Cloud Vision OCR로 분석하여 카드 정보를 반환합니다."""
    content = await file.read()
    image = vision.Image(content=content)
    response = vision_client.text_detection(image=image)
    texts = response.text_annotations

    if not texts:
        return {"card_detected": False}

    extracted_text = texts[0].description
    card_number, date_info, cvc_info, other_text = extract_card_info(extracted_text)

    card_detected = card_number != "[없음]"
    lookup_number = card_number.replace("\n", "").replace(" ", "")
    card_details = get_card_details_by_number(lookup_number)

    return {
        "card_detected": card_detected,
        "card_number": card_number,
        "date_info": date_info,
        "cvc_info": cvc_info,
        "other_text": other_text,
        "card_image_url": card_details.get("image_url", ""),
        "card_name": card_details.get("card_name", ""),
        "card_company": card_details.get("card_company", "")
    }


# ------------------------------------------------
# 음성 주문 처리
# ------------------------------------------------
def clean_gpt_answer(gpt_answer: str) -> str:
    cleaned = re.sub(r"```(?:json)?", "", gpt_answer)
    cleaned = cleaned.replace("```", "")
    cleaned = cleaned.replace("\\n", "")
    cleaned = cleaned.replace("\\\"", "\"")
    cleaned = cleaned.replace("\\", "")
    return cleaned.strip()


@app.post("/fastapi/voice-process")
def process_voice(data: VoiceData):
    logger.debug("Voice process request: %s", data)
    system_prompt = f"""
    너는 {data.brand} 카페의 음성 주문을 받는 직원이다.
    아래는 현재 주문 가능한 메뉴들의 목록이다:
    {', '.join(data.menus or [])}

    1. 고객이 주문한 메뉴와 동일한 메뉴가 있으면 동일한 메뉴를 추천해.
    2. 동일한 메뉴는 없지만 비슷한 메뉴가 있으면 비슷한 메뉴를 추천해.
    3. 비슷한 메뉴도 없으면 보유 메뉴 중 랜덤으로 추천해.
    무조건 메뉴 하나는 추천해야 하고, 응답 형식은 JSON으로 menu, assistant_text 형태로 반환해야해.
    """
    try:
        response = openai_client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": data.text},
            ],
            max_tokens=300,
            temperature=0.7
        )
        gpt_answer = clean_gpt_answer(response.choices[0].message.content.strip())
        return {
            "brand": data.brand,
            "available_menus": data.menus,
            "user_text": data.text,
            "gpt_answer": gpt_answer
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM 호출 오류: {str(e)}")


# ------------------------------------------------
# 카드 데이터 조회/저장
# ------------------------------------------------
@app.get("/fastapi/cards", response_model=List[Card])
async def get_cards(member_id: int):
    connection = None
    try:
        connection = get_connection_local()
        raw_result = fetch_sql_data(connection, "sql/select_card_list.sql", [(member_id)])
        if not raw_result:
            raise HTTPException(status_code=404, detail="No cards found for the user")
        return [Card(**row) for row in raw_result[0]]
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database query failed: {str(e)}")
    finally:
        if connection:
            connection.close()


@app.post("/fastapi/save-card")
async def save_card(card: CardInfo):
    connection = None
    try:
        connection = get_connection_local()
        cursor = connection.cursor()

        cursor.execute(
            "SELECT id FROM card WHERE card_name = %s AND card_company = %s",
            (card.cardName, card.cardCompany)
        )
        if not cursor.fetchone():
            raise HTTPException(status_code=400, detail="존재하지 않는 카드입니다.")

        now_per = random.randint(0, 90000) * 10
        last_per = random.randint(int(now_per / 10), 100000) * 10
        monthly_split = random.randint(0, 3)
        accrue_benefit = random.randint(100, 4000) * 10

        fetch_sql_data(connection, "sql/insert_card_info.sql", [(
            card.cardNumber, card.cvc, card.password, card.expiry, 1,
            now_per, last_per, monthly_split, accrue_benefit,
            card.cardName, card.cardCompany
        )])
        connection.commit()
        return {"message": "카드 정보 저장 성공"}

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"카드 저장 실패: {str(e)}")
    finally:
        if connection:
            connection.close()


@app.get("/fastapi/get-card-image")
async def get_card_image(card_number: str = Query(..., min_length=6)):
    """카드번호 앞 8자리(없으면 6자리)로 BIN 매칭하여 카드 이미지 목록을 반환합니다."""
    try:
        df = load_card_data()
        clean_number = card_number.replace(" ", "")

        def parse_bin_list(bin_str):
            return [b.strip().replace(" ", "") for b in str(bin_str).split(",")]

        df["bin_list"] = df["bin"].astype(str).apply(parse_bin_list)

        matched = df[df["bin_list"].apply(lambda bins: clean_number[:8] in bins)]
        if matched.empty:
            matched = df[df["bin_list"].apply(lambda bins: clean_number[:6] in bins)]

        if matched.empty:
            return {"message": "카드 이미지 없음", "card_count": 0, "cards": []}

        columns = ["카드 이미지", "카드명", "법인"]
        card_list = matched[columns].drop_duplicates(subset=columns).to_dict(orient="records")
        return {"message": f"{len(card_list)}개의 카드가 매칭되었습니다.", "card_count": len(card_list), "cards": card_list}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ------------------------------------------------
# 애플리케이션 실행
# ------------------------------------------------
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)

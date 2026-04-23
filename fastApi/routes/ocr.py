import os
import re
import logging

import pandas as pd

from google.cloud import vision
from google.oauth2 import service_account

from fastapi import APIRouter, File, UploadFile

logger = logging.getLogger(__name__)

router = APIRouter()

_json_key_file = "sql/striped-option-449805-v9-4da92e4186de.json"
_credentials = service_account.Credentials.from_service_account_file(_json_key_file)
vision_client = vision.ImageAnnotatorClient(credentials=_credentials)

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


@router.post("/fastapi/ocr-card")
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

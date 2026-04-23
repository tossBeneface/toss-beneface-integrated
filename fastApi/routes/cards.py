import random
import logging

import pandas as pd

from fastapi import APIRouter, HTTPException, Query
from typing import List

from db import get_connection_local, fetch_sql_data
from models import Card, CardInfo
from routes.ocr import CSV_PATH

logger = logging.getLogger(__name__)

router = APIRouter()


def _load_card_data() -> pd.DataFrame:
    import os
    if not os.path.exists(CSV_PATH):
        raise FileNotFoundError("CSV 파일을 찾을 수 없습니다.")
    return pd.read_csv(CSV_PATH)


@router.get("/fastapi/cards", response_model=List[Card])
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


@router.post("/fastapi/save-card")
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


@router.get("/fastapi/get-card-image")
async def get_card_image(card_number: str = Query(..., min_length=6)):
    """카드번호 앞 8자리(없으면 6자리)로 BIN 매칭하여 카드 이미지 목록을 반환합니다."""
    try:
        df = _load_card_data()
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

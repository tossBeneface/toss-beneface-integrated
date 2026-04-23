import os
import logging

from fastapi import APIRouter, HTTPException
from openai import OpenAI

from db import get_connection, fetch_sql_data, insert_analysis_history
from models import SQLRequest

logger = logging.getLogger(__name__)

router = APIRouter()

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


@router.post("/fastapi/analyze")
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

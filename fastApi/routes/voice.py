import os
import re
import logging
import json
from typing import Dict, Any

from fastapi import APIRouter, HTTPException
from openai import OpenAI

from models import VoiceData

logger = logging.getLogger(__name__)

router = APIRouter()

openai_client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


def clean_gpt_answer(gpt_answer: str) -> str:
    """LLM 응답에서 마크다운 태그 등을 제거하고 순수 JSON 문자열만 추출합니다."""
    # ```json ... ``` 또는 ``` ... ``` 블록 추출
    match = re.search(r"```(?:json)?\s*(.*?)\s*```", gpt_answer, re.DOTALL)
    if match:
        cleaned = match.group(1)
    else:
        cleaned = gpt_answer.strip()
    
    # 제어 문자 및 불필요한 이스케이프 제거
    cleaned = re.sub(r"[\x00-\x1F\x7F]", "", cleaned)
    return cleaned


def generate_voice_response(data: VoiceData) -> Dict[str, Any]:
    logger.debug("Voice process request: %s", data)
    
    # 1. Prompt Engineering: 명확한 페르소나와 제약 사항 부여
    system_prompt = f"""
    당신은 {data.brand} 카페의 AI 주문 도우미입니다.
    사용자의 음성 입력(text)을 분석하여 가장 적합한 메뉴를 추천하세요.

    [주문 가능 메뉴]
    {', '.join(data.menus or ["기본 메뉴"])}

    [규칙]
    1. 사용자가 언급한 메뉴가 주문 가능 목록에 있으면 해당 메뉴를 선택하세요.
    2. 비슷한 메뉴가 있다면 가장 유사한 메뉴를 추천하세요.
    3. 일치하거나 유사한 메뉴가 전혀 없다면 주문 가능 목록 중 하나를 랜덤으로 추천하세요.
    4. 반드시 아래 JSON 형식으로만 응답하세요. 다른 설명은 생략하세요.

    [응답 형식]
    {{
        "menu": "추천된 메뉴 이름",
        "assistant_text": "고객에게 전달할 친절한 응답 문구"
    }}
    """
    
    try:
        # 2. LLM Call: GPT-4o 사용
        response = openai_client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": data.text},
            ],
            max_tokens=300,
            temperature=0.2, # 일관된 JSON 응답을 위해 낮은 temperature 설정
            response_format={"type": "json_object"} # JSON 모드 강제
        )
        
        raw_content = response.choices[0].message.content.strip()
        cleaned_json = clean_gpt_answer(raw_content)
        
        # 3. Validation: JSON 파싱 및 구조 검증
        try:
            parsed_answer = json.loads(cleaned_json)
        except json.JSONDecodeError as je:
            logger.error("JSON 파싱 실패: %s, Raw: %s", str(je), raw_content)
            # 파싱 실패 시 기본 응답 생성 (Fallback 로직)
            parsed_answer = {
                "menu": data.menus[0] if data.menus else "기본 음료",
                "assistant_text": "주문하신 내용을 정확히 이해하지 못해 기본 메뉴를 추천해 드립니다."
            }

        return {
            "brand": data.brand,
            "available_menus": data.menus,
            "user_text": data.text,
            "gpt_answer": parsed_answer
        }
        
    except Exception as e:
        logger.exception("LLM 처리 중 오류 발생")
        raise HTTPException(status_code=500, detail=f"AI 서비스 연동 오류: {str(e)}")


@router.post("/fastapi/voice-process")
def process_voice(data: VoiceData) -> Dict[str, Any]:
    return generate_voice_response(data)

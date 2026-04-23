from pydantic import BaseModel
from typing import List, Optional


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

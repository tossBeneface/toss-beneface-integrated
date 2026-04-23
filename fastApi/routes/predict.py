import joblib
import numpy as np

from fastapi import APIRouter

from models import ModelInput

router = APIRouter()

with open("sql/rf_model_modify.pkl", "rb") as f:
    model = joblib.load(f)


@router.post("/fastapi/predict")
async def predict(input_data: ModelInput):
    features = np.array([[
        input_data.전월실적, input_data.결제금액, input_data.혜택받은횟수,
        input_data.이번달실적, input_data.혜택받은금액, input_data.Benefit,
        input_data.limit_once, input_data.limit_month,
        input_data.min_pay, input_data.min_per, input_data.monthly
    ]])
    prediction = model.predict(features)
    return {"prediction": prediction.tolist()}

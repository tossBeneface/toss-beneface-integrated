import logging

import numpy as np
import cv2
import face_recognition

from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse

from face_registry import face_registry, load_last_five_faces_from_api

logger = logging.getLogger(__name__)

router = APIRouter()

THRESHOLD = 0.3


@router.post("/fastapi/refresh-faces")
def refresh_faces():
    load_last_five_faces_from_api()
    return {"message": "Faces reloaded (last 5)"}


@router.post("/fastapi/recognize")
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

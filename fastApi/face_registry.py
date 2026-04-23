import os
import logging
import threading

import numpy as np
import cv2
import face_recognition
import requests

logger = logging.getLogger(__name__)

SPRING_API_URL = os.getenv("SPRING_API_URL", "https://api.tossbeneface.com")


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


def load_faces_from_api() -> None:
    """Spring Boot API에서 전체 얼굴 데이터를 로드하여 registry에 저장합니다."""
    api_url = f"{SPRING_API_URL}/api/faces/all"
    try:
        response = requests.get(api_url, timeout=10)
    except requests.RequestException as e:
        logger.warning("Spring Boot API 연결 실패: %s", e)
        return

    logger.info("load_faces_from_api response: %s", response)

    if response.status_code != 200:
        logger.error("Spring Boot API 요청 실패. 상태 코드: %d", response.status_code)
        return

    faces, member_ids = _download_face_encodings(response.json())
    face_registry.replace_all(faces, member_ids)
    logger.info("[startup] 등록된 얼굴: %d개", len(face_registry))


def load_last_five_faces_from_api() -> None:
    """최근 5개 얼굴 데이터를 로드하여 registry에 추가합니다."""
    api_url = f"{SPRING_API_URL}/api/faces/last5"
    response = requests.get(api_url, timeout=10)
    logger.info("load_last_five_faces_from_api response: %s", response.text)

    if response.status_code != 200:
        logger.error("Spring Boot API 요청 실패. 상태 코드: %d", response.status_code)
        return

    new_faces, new_member_ids = _download_face_encodings(response.json())
    face_registry.extend(new_faces, new_member_ids)
    logger.info("[refresh] 최근 5개 중 %d개 추가. 총 %d개 보유.", len(new_faces), len(face_registry))

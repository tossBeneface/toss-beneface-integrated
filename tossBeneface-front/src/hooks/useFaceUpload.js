import { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import * as faceapi from 'face-api.js';
import { jwtDecode } from 'jwt-decode';
import { useSelector } from 'react-redux';

const ANGLES = ['정면', '왼쪽', '오른쪽', '위', '아래'];

export default function useFaceUpload() {
  const [angleIndex, setAngleIndex] = useState(0);
  const [subPhase, setSubPhase] = useState('intro');
  const [modelLoaded, setModelLoaded] = useState(false);
  const [progressValue, setProgressValue] = useState(0);
  const [refreshCalled, setRefreshCalled] = useState(false);
  const [memberId, setMemberId] = useState(null);
  const webcamRef = useRef(null);

  const accessToken = useSelector(state => state.auth.accessToken);

  // memberId from JWT
  useEffect(() => {
    if (accessToken) {
      try {
        const decoded = jwtDecode(accessToken);
        setMemberId(decoded.memberId);
      } catch (error) {
        console.error('JWT 디코딩 실패:', error);
      }
    }
  }, [accessToken]);

  // A. face-api 모델 로드
  useEffect(() => {
    faceapi.nets.tinyFaceDetector.loadFromUri('/models')
      .then(() => {
        setModelLoaded(true);
        console.log('Face API 모델 로딩 완료');
      });
  }, []);

  // B. intro → detect (3초 후)
  useEffect(() => {
    if (!modelLoaded || subPhase !== 'intro') return;
    const timer = setTimeout(() => setSubPhase('detect'), 3000);
    return () => clearTimeout(timer);
  }, [modelLoaded, subPhase]);

  // C. detect → progress (얼굴 감지 시)
  useEffect(() => {
    if (!modelLoaded || subPhase !== 'detect' || angleIndex >= ANGLES.length) return;
    const interval = setInterval(async () => {
      if (!webcamRef.current?.video) return;
      const detections = await faceapi.detectAllFaces(webcamRef.current.video, new faceapi.TinyFaceDetectorOptions());
      if (detections.length > 0) {
        console.log(`[${ANGLES[angleIndex]}] 얼굴 감지! 3초 카운트 시작`);
        setSubPhase('progress');
      }
    }, 500);
    return () => clearInterval(interval);
  }, [modelLoaded, subPhase, angleIndex]);

  // D. progress → 캡처 & 업로드 (3초 로딩바)
  useEffect(() => {
    if (subPhase !== 'progress') return;
    setProgressValue(0);
    const startTime = Date.now();
    const interval = setInterval(() => {
      const fraction = (Date.now() - startTime) / 3000;
      if (fraction >= 1) {
        setProgressValue(100);
        clearInterval(interval);
        doCaptureAndUpload();
      } else {
        setProgressValue(Math.round(fraction * 100));
      }
    }, 50);
    return () => clearInterval(interval);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subPhase]);

  // E. 캡처 & 업로드
  const doCaptureAndUpload = async () => {
    if (!webcamRef.current) return;
    const imageSrc = webcamRef.current.getScreenshot();
    try {
      const blob = await (await fetch(imageSrc)).blob();
      const formData = new FormData();
      formData.append('file', blob, `${ANGLES[angleIndex]}.jpg`);
      formData.append('memberId', memberId);
      const res = await axios.post(`${process.env.REACT_APP_API_BASE_URL}/faces/upload`, formData);
      console.log('업로드 성공:', res.data);
    } catch (err) {
      console.error('업로드 실패:', err);
    }
    const nextIndex = angleIndex + 1;
    if (nextIndex < ANGLES.length) {
      setAngleIndex(nextIndex);
      setSubPhase('detect');
    } else {
      setSubPhase('done');
    }
  };

  // F. 모든 각도 완료 후 Refresh API 호출 (1회)
  useEffect(() => {
    const done = angleIndex >= ANGLES.length || subPhase === 'done';
    if (!refreshCalled && done) {
      setRefreshCalled(true);
      console.log('모든 각도 등록 완료 → Refresh 엔드포인트 호출');
      fetch(`${process.env.REACT_APP_API_BASE_URL}/refresh-faces`, { method: 'POST' })
        .then(res => res.json())
        .then(data => console.log('Refresh API 응답:', data))
        .catch(err => console.error('Refresh API 오류:', err));
    }
  }, [angleIndex, subPhase, refreshCalled]);

  return { angleIndex, subPhase, modelLoaded, progressValue, webcamRef, angles: ANGLES };
}

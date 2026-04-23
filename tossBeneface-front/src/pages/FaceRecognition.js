import React, { useRef, useState, useEffect } from "react";
import Webcam from "react-webcam";
import { useNavigate, useLocation } from "react-router-dom";

function FaceRecognition() {
  const webcamRef = useRef(null);
  const [isIdentified, setIsIdentified] = useState(false); // 신원 식별 여부
  const [animationState, setAnimationState] = useState("enter"); // 애니메이션 상태 관리
  const [recognizedName, setRecognizedName] = useState(""); // 인식된 이름 저장
  const [memberId, setMemberId] = useState(null); // 인식된 memberId 저장

  const videoConstraints = {
    width: 1920,
    height: 1080,
    facingMode: "user", // 전면 카메라
  };

  const navigate = useNavigate();
  const location = useLocation();
  const { brand } = location.state || { brand: "매장" }; // 다른 페이지에서 넘어온 brand 정보 (없으면 '매장' 기본)

  // memberId를 받아 백엔드 API를 호출하고 회원 이름을 받아오는 함수
  const fetchMemberName = async (memberId) => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_BASE_URL}/member/name?memberId=${memberId}`
      );
      if (!response.ok) {
        console.error("회원 이름 조회 오류:", response.statusText);
        return;
      }
      // 응답이 단순 텍스트라고 가정 (만약 JSON 형식이면 response.json() 사용)
      const name = await response.text();
      console.log("인식된 회원 이름:", name);
      setRecognizedName(name);
    } catch (error) {
      console.error("회원 이름 조회 중 에러:", error);
    }
  };

  // 컴포넌트 마운트 시, 2초 간격으로 캡처 & API 호출
  useEffect(() => {
    const interval = setInterval(() => {
      captureAndSendPhoto();
    }, 2000); // 2초 간격

    return () => clearInterval(interval); // 언마운트 시 제거
  }, []);

  /**
   * 웹캠 스크린샷을 찍고 FastAPI 서버 (/api/recognize)에 전송하여 얼굴 인식 결과를 처리합니다.
   */
  const captureAndSendPhoto = async () => {
    if (!webcamRef.current) return;

    const screenshot = webcamRef.current.getScreenshot();
    if (!screenshot) return;

    try {
      // Base64 이미지를 Blob으로 변환
      const blob = await (await fetch(screenshot)).blob();
      const formData = new FormData();
      formData.append("file", blob, "image.jpg");

      // FastAPI에 POST 요청
      const response = await fetch("https://benefacefastapi20-frgtcya5bnefbdfs.koreacentral-01.azurewebsites.net/fastapi/recognize", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) {
        console.error("서버 에러:", response.statusText);
        return;
      }

      const data = await response.json();
      console.log("인식 결과 data:", data);
      // 예시 응답: { result: "success", memberId: 1 }
      
      if (data.result === "success") {
        if (data.memberId) {
          console.log("인식된 memberId:", data.memberId);
          // 백엔드의 회원 이름 조회 API를 호출하여 recognizedName 업데이트
          fetchMemberName(data.memberId);
          setMemberId(data.memberId); // FastAPI에서 반환한 memberId 저장
          setIsIdentified(true);
          setAnimationState("enter");

          // 2초 후 애니메이션 fade-out 처리
          setTimeout(() => {
            setAnimationState("fade-out");
            setTimeout(() => {
              setIsIdentified(false);
            }, 1000);
          }, 2000);
        } else {
          console.log("인식 결과가 Unknown 이거나 memberId가 없음");
        }
      } else if (data.result === "NoFace") {
        console.log("얼굴이 없습니다.");
      } else {
        console.log("기타 결과:", data);
      }
    } catch (error) {
      console.error("Error:", error);
    }
  };

  // memberId가 반환되고 isIdentified가 true이면 3초 후에 카드 추천 화면으로 이동
  useEffect(() => {
    if (isIdentified && memberId !== null) {
      const timer = setTimeout(() => {
        navigate("/CardRecommendation", { state: { memberId } });
      }, 3000); // 3초 후 페이지 이동

      return () => clearTimeout(timer);
    }
  }, [isIdentified, memberId, navigate]);

  return (
    <>
      {/* 애니메이션 스타일 */}
      <style>
        {`
          .payment-complete-container {
            opacity: 0;
            transform: translateY(20px);
            transition: opacity 1s ease, transform 1s ease;
          }

          .payment-complete-container.enter {
            opacity: 1;
            transform: translateY(0);
          }

          .payment-complete-container.fade-out {
            opacity: 0;
            transform: translateY(-20px);
          }

          .webcam-container {
            position: relative;
            width: 100vw;
            height: 100vh;
            overflow: hidden;
            display: flex;
            justify-content: center;
            align-items: center;
          }

          .face-indicator {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 300px;
            height: 300px;
            border: 5px solid #007bff;
            border-radius: 50%;
            display: flex;
            justify-content: center;
            align-items: center;
            background: rgba(0, 0, 0, 0.3);
          }
        `}
      </style>

      {/* 인식 성공 시 보여줄 오버레이 */}
      {isIdentified && (
        <div
          className={`payment-complete-container ${animationState}`}
          style={{
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            height: "100vh",
            fontFamily: "Arial, sans-serif",
            backgroundColor: "#f9f9f9",
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            zIndex: 9999,
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              width: "150px",
              height: "150px",
              border: "5px solid #007bff",
              borderRadius: "50%",
              marginBottom: "20px",
            }}
          >
            <span style={{ fontSize: "64px", color: "#007bff" }}>😊</span>
          </div>
          <h2 style={{ color: "#007bff", marginBottom: "10px" }}>
            결제 완료!
          </h2>
          <p style={{ color: "#333", fontSize: "16px" }}>
            {recognizedName}님 어서오세요!
          </p>
          <p style={{ color: "#333", fontSize: "16px" }}>
            토스뱅크 | 3456으로
          </p>
        </div>
      )}

      {/* Webcam Component */}
      <div className="webcam-container">
        <Webcam
          audio={false}
          ref={webcamRef}
          screenshotFormat="image/jpeg"
          videoConstraints={videoConstraints}
          style={{
            width: "100%",
            height: "100%",
            objectFit: "cover",
          }}
        />
        <div className="face-indicator">
          <span
            style={{
              fontSize: "24px",
              fontWeight: "bold",
              color: "#fff",
            }}
          >
            얼굴을 여기에 맞추세요
          </span>
        </div>
      </div>
    </>
  );
}

export default FaceRecognition;
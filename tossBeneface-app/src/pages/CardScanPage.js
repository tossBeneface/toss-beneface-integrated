import React, { useRef, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import withAuth from "../redux/hoc/withAuth";
import Webcam from "react-webcam";

function CardScanPage() {
  const navigate = useNavigate();
  const webcamRef = useRef(null);

  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [scanFailed, setScanFailed] = useState(false);

  // 자동 스캔 간격 (밀리초 단위, 3000ms = 3초)
  const scanInterval = 3000;

  const handleScanComplete = async () => {
    if (isLoading) return; // 스캔 중이면 중복 실행 방지

    try {
      setIsLoading(true);
      setScanFailed(false);

      // 비디오 캡처
      if (!webcamRef.current) throw new Error("카메라를 찾을 수 없습니다.");
      const dataURL = webcamRef.current.getScreenshot();
      if (!dataURL) throw new Error("캡처 실패");

      // Blob 변환
      const blob = await fetch(dataURL).then((res) => res.blob());

      // FormData 생성 및 API 요청
      const formData = new FormData();
      formData.append("file", blob, "card_scan.png");

      const response = await fetch("https://benefacefastapi20-frgtcya5bnefbdfs.koreacentral-01.azurewebsites.net/fastapi/ocr-card", {
        method: "POST",
        body: formData,
      });

      if (!response.ok) throw new Error("카드 OCR 실패");

      const data = await response.json();
      setIsLoading(false);

      if (data.card_detected) {
        navigate("/card-preview", {
          state: {
            cardName: data.card_name,
            cardImage: data.card_image_url,
            cardCompany: data.card_company,
            cardNumber: data.card_number,
            expiry: data.date_info,
          },
        });
      } else {
        setScanFailed(true);
      }
    } catch (error) {
      console.error("스캔 실패:", error);
      setErrorMsg("카드 OCR 처리 중 오류가 발생했습니다.");
      setIsLoading(false);
      setScanFailed(true);
    }
  };

  // 일정 간격마다 자동 스캔 실행
  useEffect(() => {
    const intervalId = setInterval(() => {
      handleScanComplete();
    }, scanInterval);

    return () => clearInterval(intervalId); // 컴포넌트 언마운트 시 정리
  }, []);

  return (
    <div className="flex flex-col h-screen bg-black relative">
      <div className="absolute inset-0">
        {errorMsg ? (
          <div className="flex items-center justify-center text-red-500">{errorMsg}</div>
        ) : (
          <Webcam
            audio={false}
            ref={webcamRef}
            screenshotFormat="image/png"
            className="w-full h-full object-cover"
            videoConstraints={{ facingMode: "environment" }}
          />
        )}
      </div>

      {isLoading && (
        <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="text-white text-lg font-bold">스캔 중...</div>
        </div>
      )}

      <div className="absolute inset-0 flex flex-col justify-between z-10">
        <header className="flex items-center text-white p-4">
          <button className="text-lg" onClick={() => navigate(-1)}>{"<"}</button>
          <h2 className="text-lg font-bold ml-4">카드 사진 촬영</h2>
        </header>

        <div className="flex flex-col items-center text-white">
          <p className="text-lg font-bold">카드를 사각형에 맞게 놓아주세요</p>
          <span className="text-sm text-gray-400">카메라로 자동 촬영됩니다</span>
          <div className="mt-4 w-72 h-44 border-2 border-gray-400" />
        </div>

        <div className="flex flex-col items-center gap-4 p-4">
          {scanFailed && (
            <>
              <div className="text-red-500">카드를 인식하지 못했습니다.</div>
              <div className="flex gap-4">
                <button
                  className="bg-blue-500 text-white px-6 py-2 rounded-md"
                  onClick={() => navigate("/card-input")}
                >
                  직접 입력하기
                </button>
                <button
                  className="bg-gray-600 text-white px-6 py-2 rounded-md"
                  onClick={() => {
                    setScanFailed(false);
                    handleScanComplete(); // 즉시 다시 스캔
                  }}
                >
                  다시 스캔하기
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default withAuth(CardScanPage);

import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import withAuth from "../redux/hoc/withAuth";
import axios from "axios";

function CardPreviewPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // 다른 페이지(스캔 페이지 등)에서 넘어온 state (cardNumber, expiry 등)
  const { cardName, cardNumber, expiry, cardCompany } = location.state || {};

  // 서버에서 가져온 카드 목록(배열)
  const [cardOptions, setCardOptions] = useState([]);
  // 모달 관련 상태: 사용자가 등록할 카드의 인덱스 (모달을 띄울 때 임시 저장)
  const [pendingCardIndex, setPendingCardIndex] = useState(null);
  // 모달 표시 상태
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  // 서버에서 카드 목록(GET /api/get-card-image) 불러오기
  useEffect(() => {
    if (cardNumber) {
      axios
        .get("https://benefacefastapi20-frgtcya5bnefbdfs.koreacentral-01.azurewebsites.net/fastapi/get-card-image", {
          params: { card_number: cardNumber },
        })
        .then((response) => {
          const { cards = [] } = response.data; // cards는 배열
          setCardOptions(cards);
        })
        .catch((error) => {
          console.error("카드 이미지 로드 실패:", error);
        });
    }
  }, [cardNumber]);

  // 카드 클릭 시 모달 열기
  const handleCardClick = (index) => {
    setPendingCardIndex(index);
    setShowConfirmModal(true);
  };

  // 모달에서 "등록" 눌렀을 때 → 바로 다음 페이지로 이동
  const handleConfirmSelect = () => {
    if (pendingCardIndex !== null) {
      const selectedCard = cardOptions[pendingCardIndex];
      // CSV 컬럼명: "카드 이미지", "카드명", "법인"
      const csvCardName = selectedCard["카드명"] || "이름 미확인";
      const csvCardCompany = selectedCard["법인"] || "법인 정보 없음";

      navigate("/card-input", {
        state: {
          cardName: csvCardName,
          cardCompany: csvCardCompany,
          cardNumber: cardNumber, // OCR에서 받은 카드번호
          expiry: expiry,
        },
      });
    }
    setPendingCardIndex(null);
    setShowConfirmModal(false);
  };

  // 모달에서 "취소" 눌렀을 때
  const handleCancelSelect = () => {
    setPendingCardIndex(null);
    setShowConfirmModal(false);
  };

  return (
    // 전체 화면을 채우고, 넘칠 경우 스크롤하도록 (하단 여백 없음)
    <div className="flex flex-col h-screen overflow-auto">
      {/* 헤더 영역 */}
      <header className="flex items-center border-b border-gray-300 p-4">
        <button className="text-lg" onClick={() => navigate(-1)}>
          {"<"}
        </button>
        <h1 className="text-xl font-bold ml-4">카드 미리보기</h1>
      </header>

      {/* 본문: 카드 목록 (내용이 많으면 스크롤됨) */}
      <div className="flex-1 p-4">
        {cardOptions.length === 0 ? (
          <div className="w-full h-96 bg-gray-200 flex items-center justify-center">
            No Image
          </div>
        ) : (
          <div className="w-full max-w-md mx-auto">
            <p className="text-lg font-bold mb-2">매칭된 카드 목록:</p>
            {/* 카드 목록 스크롤 영역 (화면에 꽉 차도록) */}
            <div className="max-h-[calc(100vh-8rem)] overflow-auto grid grid-cols-1 gap-4">
              {cardOptions.map((card, index) => {
                const cardImage = card["카드 이미지"]; // 이미지 URL
                const name = card["카드명"] || "알 수 없는 카드명";
                const company = card["법인"] || "카드사 정보 없음";

                return (
                  <div
                    key={index}
                    className="border p-2 rounded bg-white hover:bg-blue-50 cursor-pointer"
                    onClick={() => handleCardClick(index)}
                  >
                    {cardImage ? (
                      <img
                        src={cardImage}
                        alt="Scanned Card"
                        className="w-full h-auto object-contain mb-2"
                      />
                    ) : (
                      <div className="w-full h-48 bg-gray-200 flex items-center justify-center">
                        No Image
                      </div>
                    )}
                    <div className="text-lg font-bold">{name}</div>
                    <div className="text-gray-600">{company}</div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* 모달 (등록/취소) - 하단 여백 없이 화면 중앙에 띄움 */}
      {showConfirmModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white rounded p-6 w-80 text-center">
            <h2 className="text-lg font-bold mb-4">이 카드를 등록하시겠습니까?</h2>
            <div className="flex justify-center gap-4">
              <button
                className="bg-blue-500 text-white px-4 py-2 rounded"
                onClick={handleConfirmSelect}
              >
                등록
              </button>
              <button
                className="bg-gray-300 text-gray-800 px-4 py-2 rounded"
                onClick={handleCancelSelect}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default withAuth(CardPreviewPage);
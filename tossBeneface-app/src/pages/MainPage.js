import React, { useEffect, useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import withAuth from "../redux/hoc/withAuth";
import apiClient from "../api/apiClient";

function MainPage() {
  const navigate = useNavigate();
  const [cards, setCards] = useState([]);
  const [isMenuOpen, setMenuOpen] = useState(false); // 모바일 메뉴 상태
  const scrollRef = useRef(null);

  const fetchCards = async () => {
    try {
      const response = await apiClient.get("/api/user-cards");
      setCards(response.data);
    } catch (error) {
      console.error("Error fetching card data:", error);
    }
  };

  useEffect(() => {
    fetchCards();
  }, []);

  // 좌우 화살표 클릭 시 스크롤 이동 함수
  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: -300, behavior: "smooth" });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: 300, behavior: "smooth" });
    }
  };

  // 모바일 메뉴 토글 함수
  const toggleMenu = () => setMenuOpen((prev) => !prev);

  return (
    <div className="flex flex-col min-h-screen overflow-hidden bg-gray-50">
      {/* 헤더 영역 */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-6 flex justify-between items-center">
          {/* 로고 영역 */}
          <div className="flex items-center">
            <img
              src="/Toss_Symbol_Primary.png"
              alt="Toss Logo"
              className="h-8 sm:h-10 mr-2"
            />
            <span className="text-base sm:text-lg font-bold text-gray-800">
              toss <span className="font-light">Beneface</span>
            </span>
          </div>

          {/* 데스크탑용 네비게이션 (md 이상에서 표시) */}
          <nav className="hidden md:flex space-x-6">
            <button
              onClick={() => navigate("/home")}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              홈
            </button>
            <button
              onClick={() => navigate("/qrcode")}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              QR
            </button>
            <button
              onClick={() => navigate("/FaceRecognition")}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              얼굴등록
            </button>
            <button
              onClick={() => navigate("/menu")}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              결제내역
            </button>
          </nav>

          {/* 모바일용 햄버거 아이콘 및 드롭다운 (md 미만에서 표시) */}
          <div className="relative md:hidden">
            <button
              onClick={toggleMenu}
              className="text-gray-600 hover:text-blue-600 transition-colors focus:outline-none"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-8 w-8"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
            {/* 드롭다운 메뉴는 항상 렌더링되고, isMenuOpen 상태에 따라 opacity와 scale이 변합니다 */}
            <div
              className={`absolute right-0 mt-2 w-40 bg-white shadow-md rounded-md py-2 z-50 transform origin-top-right transition duration-200 ease-out ${
                isMenuOpen ? "opacity-100 scale-100" : "opacity-0 scale-95 pointer-events-none"
              }`}
            >
              <button
                onClick={() => {
                  toggleMenu();
                  navigate("/home");
                }}
                className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
              >
                홈
              </button>
              <button
                onClick={() => {
                  toggleMenu();
                  navigate("/qrcode");
                }}
                className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
              >
                QR
              </button>
              <button
                onClick={() => {
                  toggleMenu();
                  navigate("/FaceRecognition");
                }}
                className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
              >
                얼굴등록
              </button>
              <button
                onClick={() => {
                  toggleMenu();
                  navigate("/menu");
                }}
                className="block w-full text-left px-4 py-2 text-gray-800 hover:bg-gray-100"
              >
                결제내역
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* 메인 콘텐츠 영역 */}
      <main className="flex-grow overflow-y-auto">
        {/* 히어로 섹션 */}
        <section
          className="relative w-full aspect-video md:aspect-[3/2] bg-cover bg-center"
          style={{
            backgroundImage:
              'url(https://static.toss.im/assets/homepage/brand/img-press-work-2.jpg)',
          }}
        >
          {/* 어두운 오버레이 */}
          <div className="absolute inset-0 bg-black opacity-40"></div>

          {/* 중앙 텍스트 및 버튼 */}
          <div className="relative z-10 flex flex-col items-center justify-center h-full text-center text-white px-4">
            <h1 className="text-3xl md:text-4xl font-bold mb-4">
              나만의 카드 관리 솔루션
            </h1>
            <p className="text-lg md:text-xl mb-6">
              쉽고 빠르게, 당신의 카드를 한 곳에 모으세요
            </p>
            <button
              onClick={() => navigate("/card-register")}
              className="bg-white text-blue-600 px-6 py-3 rounded-full font-semibold hover:bg-gray-200 transition"
            >
              카드 등록하기
            </button>
          </div>
        </section>

        {/* 카드 목록 영역 (가로 스크롤 캐러셀) */}
        <section className="py-8 sm:py-12 relative">
          <div className="max-w-7xl mx-auto px-4">
            <h2 className="text-3xl font-bold text-gray-800 mb-6">내 카드</h2>
            {cards.length > 0 ? (
              <div className="relative">
                {/* 카드 목록 컨테이너 */}
                <div
                  ref={scrollRef}
                  className="flex space-x-4 overflow-x-auto scroll-smooth pb-4"
                >
                  {cards.map((card) => (
                    <div
                      key={card.id}
                      onClick={() => navigate(`/card-detail/${card.id}`)}
                      className="min-w-[250px] bg-white rounded-xl shadow-lg overflow-hidden transform hover:scale-105 transition duration-300 cursor-pointer"
                    >
                      <img
                        src={card.cardImage}
                        alt="Card"
                        className="w-full h-56 object-cover"
                      />
                      <div className="p-6">
                        <h3 className="text-xl font-semibold text-gray-800">
                          {card.cardName}
                        </h3>
                        <p className="mt-2 text-gray-600">
                          {Number(card.amount).toLocaleString()} 원
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* 좌측 화살표 버튼 */}
                <button
                  onClick={scrollLeft}
                  className="absolute top-1/2 left-0 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-100 transition"
                >
                  <span className="material-icons text-gray-600">
                    arrow_back_ios
                  </span>
                </button>

                {/* 우측 화살표 버튼 */}
                <button
                  onClick={scrollRight}
                  className="absolute top-1/2 right-0 transform -translate-y-1/2 bg-white rounded-full p-2 shadow-md hover:bg-gray-100 transition"
                >
                  <span className="material-icons text-gray-600">
                    arrow_forward_ios
                  </span>
                </button>
              </div>
            ) : (
              // 카드가 없는 경우: 카드 등록 유도 UI
              <div
                onClick={() => navigate("/card-register")}
                className="flex flex-col items-center justify-center border-2 border-dashed border-gray-300 rounded-xl p-12 cursor-pointer hover:bg-gray-50 transition"
              >
                <span className="material-icons text-6xl text-gray-400 mb-4">
                  credit_card
                </span>
                <p className="text-2xl font-medium text-gray-600">
                  카드를 등록하세요
                </p>
              </div>
            )}
          </div>
        </section>
      </main>

      {/* 푸터 네비게이션 */}
      <footer className="bg-white shadow py-4">
        <div className="max-w-7xl mx-auto px-4 flex justify-around">
          <button
            onClick={() => navigate("/home")}
            className="flex flex-col items-center text-gray-600 hover:text-blue-600 transition-colors"
          >
            <span className="material-icons text-2xl">home</span>
            <span className="text-sm">홈</span>
          </button>
          <button
            onClick={() => navigate("/qrcode")}
            className="flex flex-col items-center text-gray-600 hover:text-blue-600 transition-colors"
          >
            <span className="material-icons text-2xl">qr_code_scanner</span>
            <span className="text-sm">QR 코드</span>
          </button>
          <button
            onClick={() => navigate("/FaceRecognition")}
            className="flex flex-col items-center text-gray-600 hover:text-blue-600 transition-colors"
          >
            <span className="material-symbols-outlined text-2xl">
              ar_on_you
            </span>
            <span className="text-sm">얼굴등록</span>
          </button>
          <button
            onClick={() => navigate("/menu")}
            className="flex flex-col items-center text-gray-600 hover:text-blue-600 transition-colors"
          >
            <span className="material-icons text-2xl">receipt_long</span>
            <span className="text-sm">결제내역</span>
          </button>
        </div>
      </footer>
    </div>
  );
}

export default MainPage;

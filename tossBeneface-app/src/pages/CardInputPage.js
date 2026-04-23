import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import withAuth from "../redux/hoc/withAuth";
import apiClient from "../api/apiClient";

function CardInputPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // 이전 화면(예: 카드 프리뷰)에서 넘어온 초기값들
  const {
    cardName: initCardName,
    cardNumber: initCardNumber,
    expiry: initExpiry,
    cardCompany: initCardCompany,
  } = location.state || {};

  /* ===== 기본 상태 ===== */
  const [cardCompany, setCardCompany] = useState(initCardCompany || "");
  const [cardName, setCardName] = useState(initCardName || "");
  const [cardNumber, setCardNumber] = useState(initCardNumber || "");
  const [expiry, setExpiry] = useState(initExpiry || "");
  const [cvc, setCvc] = useState("");
  const [password, setPassword] = useState("");

  // activeField: 현재 포커스된 입력란, showKeypad: 커스텀 키패드 표시 여부,
  // loading: API 요청 중 여부, keypadHeight: 키패드 높이,
  // errorFields: 각 입력란 오류 여부, showError: 제출 실패 팝업 표시,
  // preventBlur: 키패드 영역과 상호작용할 때 onBlur에 의해 activeField가 지워지는 것을 방지
  const [activeField, setActiveField] = useState("");
  const [showKeypad, setShowKeypad] = useState(false);
  const [loading, setLoading] = useState(false);
  const [keypadHeight, setKeypadHeight] = useState(0);
  const [errorFields, setErrorFields] = useState({});
  const [showError, setShowError] = useState(false);
  const [preventBlur, setPreventBlur] = useState(false);

  // 사용자가 직접 수정한 경우 플래그 (자동완성 무시)
  const [isCardCompanyEdited, setIsCardCompanyEdited] = useState(false);
  const [isCardNameEdited, setIsCardNameEdited] = useState(false);

  // 자동완성 관련 (BIN 관련)
  const [fetchedCardBin, setFetchedCardBin] = useState("");
  const [binCards, setBinCards] = useState([]);
  const [selectedCard, setSelectedCard] = useState(null);

  // 각 입력란 내부 포커스 상태 (키패드 입력 시 실제 값 노출 여부)
  const [cardNumberFocused, setCardNumberFocused] = useState(false);
  const [expiryFocused, setExpiryFocused] = useState(false);
  const [cvcFocused, setCvcFocused] = useState(false);
  const [passwordFocused, setPasswordFocused] = useState(false);

  // 초기 로드 플래그 – 초기값이 있는 경우 드롭다운이 바로 뜨지 않도록
  const [isInitialLoad, setIsInitialLoad] = useState(!!initCardNumber);

  /* ===== Ref 설정 ===== */
  const cardCompanyRef = useRef(null);
  const cardNameRef = useRef(null);
  const cardNumberRef = useRef(null);
  const expiryRef = useRef(null);
  const cvcRef = useRef(null);
  const passwordRef = useRef(null);
  // 입력란들이 들어있는 스크롤 컨테이너
  const containerRef = useRef(null);

  /* ===== Effect: 키패드 높이 업데이트 ===== */
  useEffect(() => {
    if (showKeypad) {
      const keypadElement = document.getElementById("custom-keypad");
      if (keypadElement) {
        setKeypadHeight(keypadElement.offsetHeight);
      }
    } else {
      setKeypadHeight(0);
    }
  }, [showKeypad]);

  /* ===== Helper: 카드번호 정규화 (공백, 하이픈 제거) ===== */
  const normalizeCardNumber = (num) => num.replace(/[\s-]/g, "");

  /* ===== Helper: 스크롤 처리 =====
     containerRef 내부에서 해당 입력란이 키패드에 가리지 않도록 스크롤 처리 */
  const scrollFieldIntoView = (ref) => {
    if (ref && ref.current && containerRef.current) {
      const containerRect = containerRef.current.getBoundingClientRect();
      const elementRect = ref.current.getBoundingClientRect();
      // 기본 여유 공간; CVC와 카드비밀번호는 여유를 늘림 (60px)
      let extraMargin = activeField === "cvc" || activeField === "password" ? 60 : 20;
      const desiredBottom = containerRect.bottom - keypadHeight - extraMargin;
      if (elementRect.bottom > desiredBottom) {
        const delta = elementRect.bottom - desiredBottom;
        containerRef.current.scrollBy({ top: delta, behavior: "smooth" });
      }
      const desiredTop = containerRect.top + extraMargin;
      if (elementRect.top < desiredTop) {
        const delta = desiredTop - elementRect.top;
        containerRef.current.scrollBy({ top: -delta, behavior: "smooth" });
      }
    }
  };

  /* ===== Helper: 입력 검증 =====
     조건 충족 시 errorFields false (회색 테두리),
     미충족 시 errorFields true (빨간 테두리) */
  const validateField = (fieldName) => {
    let valid = false;
    if (fieldName === "cardCompany") {
      valid = cardCompany.trim() !== "";
    } else if (fieldName === "cardName") {
      valid = cardName.trim() !== "";
    } else if (fieldName === "cardNumber") {
      valid = cardNumber.length === 16;
    } else if (fieldName === "expiry") {
      valid = expiry.length === 4;
    } else if (fieldName === "cvc") {
      valid = cvc.length === 3;
    } else if (fieldName === "password") {
      valid = password.length === 4;
    }
    setErrorFields((prev) => ({ ...prev, [fieldName]: !valid }));
  };

  /* ===== Effect: 카드번호 자동완성 =====
     카드번호가 6자리 이상이면 서버에서 BIN 데이터 조회.
     단, 서버에서 받은 CVC 값이 "[없음]"이면 cvc 필드 자동 입력은 하지 않음. */
  useEffect(() => {
    if (cardNumber.length >= 6 && !isCardCompanyEdited && !isCardNameEdited) {
      const normalized = normalizeCardNumber(cardNumber);
      const bin = normalized.slice(0, 6);
      setFetchedCardBin(bin);
      fetch(`https://benefacefastapi20-frgtcya5bnefbdfs.koreacentral-01.azurewebsites.net/fastapi/get-card-image?card_number=${cardNumber}`)
        .then((res) => res.json())
        .then((data) => {
          if (data.card_count > 0 && data.cards && data.cards.length > 0) {
            setBinCards(data.cards);
            if (data.cards.length === 1) {
              const card = data.cards[0];
              setSelectedCard(card);
              setCardName(card["카드명"] || "");
              setCardCompany(card["법인"] || "");
              if (card["CVC"] && card["CVC"] !== "[없음]") {
                // 숫자만 남도록 처리
                setCvc(card["CVC"].replace(/\D/g, ""));
              }
            }
          } else {
            setBinCards([]);
          }
        })
        .catch((err) => console.error("자동 완성 에러:", err));
    }
  }, [cardNumber, isCardCompanyEdited, isCardNameEdited]);

  /* ===== Effect: 카드번호 변경 시 BIN 비교 ===== */
  useEffect(() => {
    if (fetchedCardBin && cardNumber) {
      const normalizedCardNumber = normalizeCardNumber(cardNumber);
      const currentBin = normalizedCardNumber.slice(0, fetchedCardBin.length);
      if (currentBin !== fetchedCardBin) {
        setCardCompany("");
        setCardName("");
        setFetchedCardBin("");
        setSelectedCard(null);
        setIsCardCompanyEdited(false);
        setIsCardNameEdited(false);
        setBinCards([]);
      }
    }
  }, [cardNumber, fetchedCardBin]);

  /* ===== Effect: 초기 로드 플래그 ===== */
  useEffect(() => {
    if (initCardNumber && cardNumber !== initCardNumber) {
      setIsInitialLoad(false);
    }
  }, [cardNumber, initCardNumber]);

  /* ===== Helper: 카드번호 포맷팅 및 마스킹 ===== */
  const formatCardNumber = (num) => num.replace(/(.{4})/g, "$1 ").trim();
  const maskCardNumber = (num) => {
    const digits = num.slice(0, 16);
    const visible = digits.slice(0, 4);
    const hidden = digits.slice(4).replace(/\d/g, "*");
    return formatCardNumber(visible + hidden);
  };
  const displayCardNumber = cardNumberFocused
    ? formatCardNumber(cardNumber)
    : maskCardNumber(cardNumber);

  /* ===== Helper: 테두리 색상 결정 =====
     - 포커스 중(activeField)이면 파란색,
     - 오류(errorFields)가 있으면 빨간색,
     - 그 외 기본 회색 */
  const getInputBorderClass = (fieldName) => {
    if (errorFields[fieldName]) return "border-red-500";
    if (activeField === fieldName) return "border-blue-500";
    return "border-gray-300";
  };

  /* ===== onFocus / onBlur 핸들러 ===== */

  // 카드사 (일반 입력)
  const handleCardCompanyFocus = () => {
    setActiveField("cardCompany");
    setShowKeypad(false);
    setErrorFields((prev) => ({ ...prev, cardCompany: false }));
    scrollFieldIntoView(cardCompanyRef);
  };
  const handleCardCompanyBlur = () => {
    validateField("cardCompany");
    if (!preventBlur) setActiveField("");
  };

  // 카드명 (일반 입력)
  const handleCardNameFocus = () => {
    setActiveField("cardName");
    setShowKeypad(false);
    setErrorFields((prev) => ({ ...prev, cardName: false }));
    scrollFieldIntoView(cardNameRef);
  };
  const handleCardNameBlur = () => {
    validateField("cardName");
    if (!preventBlur) setActiveField("");
  };

  // 카드번호 (키패드 입력용 div)
  const handleCardNumberFocus = () => {
    setCardNumberFocused(true);
    setActiveField("cardNumber");
    setErrorFields((prev) => ({ ...prev, cardNumber: false }));
    scrollFieldIntoView(cardNumberRef);
    setShowKeypad(true);
  };
  const handleCardNumberBlur = () => {
    setCardNumberFocused(false);
    validateField("cardNumber");
    if (!preventBlur) setActiveField("");
  };

  // 유효기간 (키패드 입력, readOnly)
  const handleExpiryFocus = () => {
    setExpiryFocused(true);
    setActiveField("expiry");
    setErrorFields((prev) => ({ ...prev, expiry: false }));
    scrollFieldIntoView(expiryRef);
    setShowKeypad(true);
  };
  const handleExpiryBlur = () => {
    setExpiryFocused(false);
    validateField("expiry");
    if (!preventBlur) setActiveField("");
  };

  // CVC (키패드 입력, readOnly)
  const handleCvcFocus = () => {
    setCvcFocused(true);
    setActiveField("cvc");
    setErrorFields((prev) => ({ ...prev, cvc: false }));
    scrollFieldIntoView(cvcRef);
    setShowKeypad(true);
  };
  const handleCvcBlur = () => {
    setCvcFocused(false);
    validateField("cvc");
    if (!preventBlur) setActiveField("");
  };

  // 카드비밀번호 (키패드 입력, readOnly)
  const handlePasswordFocus = () => {
    setPasswordFocused(true);
    setActiveField("password");
    setErrorFields((prev) => ({ ...prev, password: false }));
    scrollFieldIntoView(passwordRef);
    setShowKeypad(true);
  };
  const handlePasswordBlur = () => {
    setPasswordFocused(false);
    validateField("password");
    if (!preventBlur) setActiveField("");
  };

  /* ===== 필드별 값이 유효하면 에러 상태 제거 (즉시 회색으로) ===== */
  useEffect(() => {
    if (cardCompany.trim() !== "" && errorFields.cardCompany) {
      setErrorFields((prev) => ({ ...prev, cardCompany: false }));
    }
  }, [cardCompany, errorFields.cardCompany]);

  useEffect(() => {
    if (cardName.trim() !== "" && errorFields.cardName) {
      setErrorFields((prev) => ({ ...prev, cardName: false }));
    }
  }, [cardName, errorFields.cardName]);

  useEffect(() => {
    if (cardNumber.length === 16 && errorFields.cardNumber) {
      setErrorFields((prev) => ({ ...prev, cardNumber: false }));
    }
  }, [cardNumber, errorFields.cardNumber]);

  useEffect(() => {
    if (expiry.length === 4 && errorFields.expiry) {
      setErrorFields((prev) => ({ ...prev, expiry: false }));
    }
  }, [expiry, errorFields.expiry]);

  useEffect(() => {
    if (cvc.length === 3 && errorFields.cvc) {
      setErrorFields((prev) => ({ ...prev, cvc: false }));
    }
  }, [cvc, errorFields.cvc]);

  useEffect(() => {
    if (password.length === 4 && errorFields.password) {
      setErrorFields((prev) => ({ ...prev, password: false }));
    }
  }, [password, errorFields.password]);

  /* ===== 최종 제출 (API 호출) ===== */
  const handleConfirm = async () => {
    if (loading) return;

    // 필드 검증
    const newErrorFields = {
      cardCompany: !cardCompany,
      cardName: !cardName,
      cardNumber: cardNumber.length !== 16,
      expiry: expiry.length !== 4,
      cvc: cvc.length !== 3,
      password: password.length !== 4,
    };
    setErrorFields(newErrorFields);

    if (Object.values(newErrorFields).some((value) => value)) return;

    setLoading(true);
    try {
      // Spring Boot API로 카드 등록 요청
      const response = await apiClient.post("/user-cards/register", {
        
        cardCompany,
        cardName,
        cardNumber,
        expiryDate: expiry, // DTO에 맞게 수정
        cvc: Number(cvc), // 🔥 int 변환
        pwd: Number(password), // 🔥 int 변환
      });

      if (response.status === 200) {
        navigate("/home"); // 🔥 등록 성공 후 홈으로 이동
      } else {
        console.error("카드 등록 실패:", response.data);
        setShowError(true);
      }
    } catch (error) {
      console.error("API 요청 실패:", error);
      setShowError(true);
    } finally {
      setLoading(false);
    }
  };

  /* ===== 키패드 관련 핸들러 ===== */

  // 숫자 입력 버튼 클릭
  const handleNumberInput = (digit) => {
    if (!activeField || loading) return;
    if (activeField === "cardNumber" && isInitialLoad) {
      setIsInitialLoad(false);
    }
    switch (activeField) {
      case "cardNumber": {
        const newValue = (cardNumber + digit).slice(0, 16);
        setCardNumber(newValue);
        if (newValue.length === 16 && errorFields.cardNumber) {
          setErrorFields((prev) => ({ ...prev, cardNumber: false }));
        }
        break;
      }
      case "expiry": {
        const newValue = (expiry + digit).slice(0, 4);
        setExpiry(newValue);
        if (newValue.length === 4 && errorFields.expiry) {
          setErrorFields((prev) => ({ ...prev, expiry: false }));
        }
        break;
      }
      case "cvc": {
        const newValue = (cvc + digit).slice(0, 3);
        setCvc(newValue);
        if (newValue.length === 3 && errorFields.cvc) {
          setErrorFields((prev) => ({ ...prev, cvc: false }));
        }
        break;
      }
      case "password": {
        const newValue = (password + digit).slice(0, 4);
        setPassword(newValue);
        if (newValue.length === 4 && errorFields.password) {
          setErrorFields((prev) => ({ ...prev, password: false }));
        }
        break;
      }
      default:
        break;
    }
  };

  // 삭제 버튼 클릭
  const handleDelete = () => {
    if (!activeField || loading) return;
    if (activeField === "cardNumber" && isInitialLoad) {
      setIsInitialLoad(false);
    }
    switch (activeField) {
      case "cardNumber":
        setCardNumber((prev) => prev.slice(0, -1));
        break;
      case "expiry":
        setExpiry((prev) => prev.slice(0, -1));
        break;
      case "cvc":
        setCvc((prev) => prev.slice(0, -1));
        break;
      case "password":
        setPassword((prev) => prev.slice(0, -1));
        break;
      default:
        break;
    }
  };

  // 키패드의 "확인" 버튼 클릭 시 처리
  const handleKeypadConfirm = () => {
    if (activeField === "cardNumber") {
      if (cardNumber.length !== 16) {
        setErrorFields((prev) => ({ ...prev, cardNumber: true }));
        return;
      }
      setErrorFields((prev) => ({ ...prev, cardNumber: false }));
      handleExpiryFocus();
    } else if (activeField === "expiry") {
      if (expiry.length !== 4) {
        setErrorFields((prev) => ({ ...prev, expiry: true }));
        return;
      }
      setErrorFields((prev) => ({ ...prev, expiry: false }));
      handleCvcFocus();
    } else if (activeField === "cvc") {
      if (cvc.length !== 3) {
        setErrorFields((prev) => ({ ...prev, cvc: true }));
        return;
      }
      setErrorFields((prev) => ({ ...prev, cvc: false }));
      handlePasswordFocus();
    } else if (activeField === "password") {
      if (password.length !== 4) {
        setErrorFields((prev) => ({ ...prev, password: true }));
        return;
      }
      setErrorFields((prev) => ({ ...prev, password: false }));
      setShowKeypad(false);
      handleConfirm();
    }
    setShowKeypad(false);
  };

  // 키패드 영역 닫기
  const handleCloseKeypad = () => {
    setActiveField("");
    setShowKeypad(false);
  };

  // 일반 입력 필드 변경 핸들러
  const handleCardCompanyChange = (e) => {
    const value = e.target.value;
    setCardCompany(value);
    setIsCardCompanyEdited(true);
    if (value.trim() !== "" && errorFields.cardCompany) {
      setErrorFields((prev) => ({ ...prev, cardCompany: false }));
    }
  };
  const handleCardNameChange = (e) => {
    const value = e.target.value;
    setCardName(value);
    setIsCardNameEdited(true);
    if (value.trim() !== "" && errorFields.cardName) {
      setErrorFields((prev) => ({ ...prev, cardName: false }));
    }
  };

  // 드롭다운: 자동완성된 카드가 여러 건일 경우 선택
  const handleCardSelect = (card) => {
    setSelectedCard(card);
    setCardName(card["카드명"] || "");
    setCardCompany(card["법인"] || "");
    setBinCards([]);
  };

  return (
    <div className="relative flex flex-col h-screen max-w-md mx-auto bg-white">
      <header className="flex items-center h-14 border-b border-gray-200 px-4">
        <button className="text-lg" onClick={() => navigate(-1)}>
          {"<"}
        </button>
        <h2 className="ml-4 text-lg font-bold">카드 정보 입력</h2>
      </header>

      {/* 스크롤 컨테이너 */}
      <div
        ref={containerRef}
        className="flex-1 overflow-y-auto px-4 py-6"
        style={{ marginBottom: `${keypadHeight}px` }}
      >
        <p className="mb-6 text-sm text-gray-600">
          본인 명의의 카드만 등록 가능합니다.
        </p>

        {/* 카드사 입력 */}
        <label className="block text-sm text-gray-600 mb-1">카드사</label>
        <input
          ref={cardCompanyRef}
          type="text"
          value={cardCompany}
          onFocus={handleCardCompanyFocus}
          onBlur={handleCardCompanyBlur}
          onChange={handleCardCompanyChange}
          placeholder="카드사를 입력해주세요"
          className={`w-full pb-2 border-b ${getInputBorderClass("cardCompany")}`}
          inputMode="text"
        />

        {/* 카드명 입력 */}
        <label className="block mt-6 text-sm text-gray-600 mb-1">카드명</label>
        <input
          ref={cardNameRef}
          type="text"
          value={cardName}
          onFocus={handleCardNameFocus}
          onBlur={handleCardNameBlur}
          onChange={handleCardNameChange}
          placeholder="카드 이름을 입력해주세요"
          className={`w-full pb-2 border-b ${getInputBorderClass("cardName")}`}
          inputMode="text"
        />

        {/* 카드번호 입력 (커스텀 키패드, readOnly) */}
        <label className="block mt-6 text-sm text-gray-600 mb-1">카드번호</label>
        <div
          ref={cardNumberRef}
          tabIndex={0}
          onFocus={handleCardNumberFocus}
          onBlur={handleCardNumberBlur}
          onClick={() => setActiveField("cardNumber")}
          className={`w-full pb-2 cursor-pointer border-b ${getInputBorderClass("cardNumber")}`}
        >
          {cardNumber ? displayCardNumber : "16자리 숫자를 입력하세요"}
        </div>

        {/* 드롭다운 (자동완성된 카드가 여러 건일 경우) */}
        {!isInitialLoad && binCards.length > 1 && !selectedCard && (
          <ul className="bg-white border mt-2 rounded shadow">
            {binCards.map((card, index) => (
              <li
                key={index}
                className="p-2 cursor-pointer hover:bg-gray-100"
                onClick={() => handleCardSelect(card)}
              >
                {card["카드명"]} - {card["법인"]}
              </li>
            ))}
          </ul>
        )}

        {/* 유효기간 입력 (커스텀 키패드, readOnly) */}
        <label className="block mt-6 text-sm text-gray-600 mb-1">
          유효기간 (MMYY)
        </label>
        <div
          ref={expiryRef}
          onClick={() => setActiveField("expiry")}
          className={`w-full pb-2 cursor-pointer border-b ${getInputBorderClass("expiry")}`}
        >
          <input
            readOnly
            tabIndex={0}
            type={expiryFocused ? "text" : "password"}
            value={expiry}
            onFocus={handleExpiryFocus}
            onBlur={handleExpiryBlur}
            placeholder="월/년도(MMYY) 4자리"
            className="w-full pb-2"
          />
        </div>

        {/* CVC 입력 (커스텀 키패드, readOnly) */}
        <label className="block mt-6 text-sm text-gray-600 mb-1">CVC번호</label>
        <div
          ref={cvcRef}
          onClick={() => setActiveField("cvc")}
          className={`w-full pb-2 cursor-pointer border-b ${getInputBorderClass("cvc")}`}
        >
          <input
            readOnly
            tabIndex={0}
            type={cvcFocused ? "text" : "password"}
            value={cvc}
            onFocus={handleCvcFocus}
            onBlur={handleCvcBlur}
            placeholder="3자리 보안코드"
            className="w-full pb-2"
          />
        </div>

        {/* 카드비밀번호 입력 (커스텀 키패드, readOnly) */}
        <label className="block mt-6 text-sm text-gray-600 mb-1">
          카드비밀번호
        </label>
        <div
          ref={passwordRef}
          onClick={() => setActiveField("password")}
          className={`w-full pb-2 cursor-pointer border-b ${getInputBorderClass("password")}`}
        >
          <input
            readOnly
            tabIndex={0}
            type={passwordFocused ? "text" : "password"}
            value={password}
            onFocus={handlePasswordFocus}
            onBlur={handlePasswordBlur}
            placeholder="4자리 비밀번호"
            className="w-full pb-2"
          />
        </div>

        <button
          className="mt-8 w-full bg-blue-500 text-white py-3 rounded-md font-bold hover:bg-blue-600"
          onClick={handleKeypadConfirm}
          disabled={loading}
        >
          {loading ? "로딩중..." : "확인"}
        </button>
      </div>

      {/* 커스텀 키패드 영역 */}
      {showKeypad && (
        <div
          id="custom-keypad"
          onMouseDown={() => setPreventBlur(true)}
          onMouseUp={() => setPreventBlur(false)}
          onTouchStart={() => setPreventBlur(true)}
          onTouchEnd={() => setPreventBlur(false)}
          className="fixed bottom-0 left-0 w-full bg-white shadow-lg border-t"
        >
          <button
            onClick={handleKeypadConfirm}
            className="w-full bg-blue-500 text-white py-4 font-bold"
          >
            확인
          </button>
          <div className="grid grid-cols-3 gap-2 p-4">
            {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
              <button
                key={num}
                onClick={() => handleNumberInput(num.toString())}
                className="w-full h-16 bg-gray-100 text-lg font-bold rounded-md shadow-md hover:bg-gray-200"
              >
                {num}
              </button>
            ))}
            <button
              onClick={handleDelete}
              className="col-span-1 w-full h-16 bg-gray-300 text-lg font-bold rounded-md shadow-md hover:bg-gray-400"
            >
              ←
            </button>
            <button
              onClick={() => handleNumberInput("0")}
              className="col-span-1 w-full h-16 bg-gray-100 text-lg font-bold rounded-md shadow-md hover:bg-gray-200"
            >
              0
            </button>
            <button
              onClick={handleCloseKeypad}
              className="col-span-1 w-full h-16 bg-gray-500 text-white text-lg font-bold rounded-md shadow-md hover:bg-gray-600"
            >
              X
            </button>
          </div>
        </div>
      )}

      {showError && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded-md shadow-md text-center">
            <p className="text-black-500 font-bold">
              카드 인증 중 에러가 발생했습니다. 카드사로 문의해주세요.
            </p>
            <button
              onClick={() => setShowError(false)}
              className="mt-4 bg-blue-500 text-white px-4 py-2 rounded-md"
            >
              확인
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default withAuth(CardInputPage);

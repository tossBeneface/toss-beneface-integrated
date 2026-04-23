import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const TermsModal = ({ onClose, onAgree }) => {
  const navigate = useNavigate();
  const [terms, setTerms] = useState([
    { id: 1, title: "토스 회원 약관 및 동의사항", agreed: false, required: true },
    { id: 2, title: "본인 확인 서비스 약관 및 동의사항", agreed: false, required: true },
    { id: 3, title: "마케팅 수신 동의사항", agreed: false, required: false },
  ]);

  const toggleAgree = (id) => {
    setTerms((prev) =>
      prev.map((term) =>
        term.id === id ? { ...term, agreed: !term.agreed } : term
      )
    );
  };

  // 필수 약관만 필터링하여 모두 동의했는지 체크
  const allRequiredAgreed = terms.filter(term => term.required).every(term => term.agreed);

  const handleAgree = () => {
    if (allRequiredAgreed) {
      onAgree();
    } else {
      alert("필수 약관에 동의해주세요.");
    }
  };

  return (
    <div className="fixed inset-0 flex justify-center items-center bg-black bg-opacity-50">
      <div className="bg-white p-6 rounded-lg shadow-lg w-11/12 md:w-96">
        <h2 className="text-xl font-bold mb-4">토스를 쓰려면 동의가 필요해요</h2>
        <ul className="space-y-2">
          {terms.map((term) => (
            <li
              key={term.id}
              className="flex items-center justify-between border-b py-2 text-sm"
            >
              <span>
                {term.required ? "[필수] " : "[선택] "}
                {term.title}
              </span>
              <div className="flex items-center">
                <button
                  className="text-blue-500 underline mr-2"
                  onClick={() => navigate(`/terms/${term.id}`)} // 상세 페이지로 이동
                >
                  {">"}
                </button>
                <input
                  type="checkbox"
                  checked={term.agreed}
                  onChange={() => toggleAgree(term.id)}
                />
              </div>
            </li>
          ))}
        </ul>
        <button
          onClick={handleAgree}
          className="mt-4 w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600"
        >
          동의하기
        </button>
        <button
          onClick={onClose}
          className="mt-2 w-full text-gray-500 py-2 rounded hover:text-gray-700"
        >
          닫기
        </button>
      </div>
    </div>
  );
};

export default TermsModal;

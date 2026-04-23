import React from "react";
import { useNavigate } from "react-router-dom";
import withAuth from "../redux/hoc/withAuth";

function CardRegisterPage() {
  const navigate = useNavigate();

  return (
    <div className="w-full h-screen flex flex-col bg-white max-w-md mx-auto">
      {/* 헤더 영역 */}
      <header className="flex items-center h-14 border-b border-gray-200 px-4">
        <button className="text-lg" onClick={() => navigate(-1)}>{"<"}</button>
        <h2 className="ml-4 text-lg font-bold">카드를 어떻게 등록할까요?</h2>
      </header>

      {/* 등록 옵션 리스트 */}
      <div className="mt-6 px-4">
        <div
          className="flex items-center justify-between p-4 border-b border-gray-300 cursor-pointer hover:bg-gray-100 rounded-lg transition"
          onClick={() => navigate("/card-scan")}
        >
          <div className="flex items-center">
            <div className="w-12 h-12 bg-gray-300 rounded-lg flex items-center justify-center text-white text-3xl font-bold">
              📷
            </div>
            <span className="ml-4 text-lg font-medium">카드 스캔하기</span>
          </div>
          <span className="text-gray-500 text-xl">{">"}</span>
        </div>

        <div
          className="flex items-center justify-between p-4 border-b border-gray-300 cursor-pointer hover:bg-gray-100 rounded-lg transition mt-4"
          onClick={() => navigate("/card-input")}
        >
          <div className="flex items-center">
            <div className="w-12 h-12 bg-blue-300 rounded-lg flex items-center justify-center text-white text-lg font-bold">
              123
            </div>
            <span className="ml-4 text-lg font-medium">직접 번호 입력하기</span>
          </div>
          <span className="text-gray-500 text-xl">{">"}</span>
        </div>
      </div>
    </div>
  );
}

export default withAuth(CardRegisterPage);

import React from "react";
import { useParams, useNavigate } from "react-router-dom";
import Terms1 from "../components/termsDetail/Terms1";
import Terms2 from "../components/termsDetail/Terms2";
import Terms3 from "../components/termsDetail/Terms3";

const termsComponents = {
  1: Terms1,
  2: Terms2,
  3: Terms3,
};

const TermsDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const SelectedTerms = termsComponents[id];

  return (
    <div className="min-h-screen flex flex-col items-center bg-gray-100">
      {/* 공통 헤더 */}
      <header className="sticky top-0 w-full max-w-[430px] bg-white shadow-md py-4 px-4 flex justify-center items-center z-10">
        <button
          onClick={() => navigate(-1)}
          className="absolute left-4 text-blue-500"
        >
          {"< 뒤로가기"}
        </button>
        <h1 className="text-lg font-bold text-center">약관 상세</h1>
      </header>

      {/* 콘텐츠 영역 */}
      <main className="flex-grow w-full max-w-[430px] min-w-[384px] mx-auto bg-white shadow-md rounded-lg overflow-y-auto max-h-[calc(100vh-80px)] p-4">
        {SelectedTerms ? (
          <SelectedTerms />
        ) : (
          <p className="text-gray-500">약관을 찾을 수 없습니다.</p>
        )}
      </main>
    </div>
  );
};

export default TermsDetailPage;

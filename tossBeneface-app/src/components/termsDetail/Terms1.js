import React from "react";

const Terms1 = () => {
  return (
    <div className="min-h-screen flex flex-col bg-gray-100 items-center">
      {/* 콘텐츠 영역 */}
      <main className="flex-grow w-full max-w-[430px] min-w-[393px] mx-auto bg-white shadow-md rounded-lg overflow-y-auto p-4">
        <h2 className="text-xl font-bold text-center mb-4">서비스 이용약관</h2>
        <div className="text-sm text-gray-700 leading-relaxed">
          <section className="mb-4">
            <h3 className="font-bold mb-2">제1장 총칙</h3>
            <p>
              제1조 (목적) 본 약관은 주식회사 비바리퍼블리카(이하 "회사")가 제공하는
              전자금융거래 서비스 및 관련 서비스(이하 "서비스"라 함)의 이용과 관련하여,
              회사와 회원 간 또는 회원 간의 권리 및 의무 및 책임사항 및 서비스 이용조건 및
              절차 등 기본적인 사항을 규정함을 목적으로 합니다.
            </p>
          </section>
          <section className="mb-4">
            <h3 className="font-bold mb-2">제2장 서비스 이용</h3>
            <p>
              제2조 (이용 계약의 성립) 서비스 이용 계약은 이용자의 이용 신청에 대한 회사의
              승낙으로 성립됩니다.
            </p>
            <p>
              제3조 (이용자의 의무) 이용자는 서비스 이용 시 다음 각 호의 행위를 하여서는 안 됩니다...
            </p>
          </section>
          <section className="mb-4">
            <h3 className="font-bold mb-2">제3장 기타</h3>
            <p>
              제4조 (손해배상) 회사는 서비스 이용과 관련하여 이용자에게 발생한 어떠한
              손해에 대해서도 책임을 지지 않습니다.
            </p>
            <p>
              제5조 (면책 조항) 회사는 천재지변, 전쟁, 정부의 통제 등 불가항력적인 사유로
              서비스를 제공할 수 없는 경우 책임을 면합니다...
            </p>
          </section>

          {/* 스크롤 테스트용 더미 데이터 */}
          {/* {[...Array(50)].map((_, index) => (
            <p key={index}>
              추가적인 예제 내용입니다. 스크롤 테스트를 위해 반복된 텍스트를 추가하고 있습니다. {index + 1}
            </p>
          ))} */}
        </div>
      </main>
    </div>
  );
};

export default Terms1;

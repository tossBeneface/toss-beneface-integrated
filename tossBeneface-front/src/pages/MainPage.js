// MainPage.js
import React from 'react';
import { useNavigate } from 'react-router-dom';
import './main.css';

const MainPage = () => {
  const navigate = useNavigate();

  const handleFloatingBarClick = () => {
    // '/map' 경로로 이동 (해당 경로는 LazyLoad(Map)가 렌더링되는 페이지여야 함)
    navigate('/map');
  };
  return (
    <div className="main-container">
      {/* 헤더 영역 */}
      <header>
        <a href="https://toss.im/">
          <img
            src="https://framerusercontent.com/images/t1w4UaHb50FmWPlwURqgvIXchYA.jpg"
            alt="토스 로고"
          />
        </a>
        <nav>
          <ul className="banner">
            <li>
              <a href="">프로젝트 소개</a>
            </li>
            <li>
              <a href="">기능 소개</a>
            </li>
            <li>
              <a href="">칭찬의 한마디</a>
            </li>
            <li>
              <a href="">자유게시판</a>
            </li>
            <li>
              <a href="">마무리</a>
            </li>
          </ul>
          <ul className="language">
            <li>
              <a href="">KOR</a>
            </li>
            <span>|</span>
            <li className="ENG">
              <a className="ENG" href="">
                ENG
              </a>
            </li>
          </ul>
        </nav>
      </header>

      {/* 메인 이미지 및 타이틀 영역 */}
      <section>
        <img
          src="https://static.toss.im/assets/homepage/newtossim/new_main.png"
          alt="메인"
          className="main_image"
        />
        <section className="title-section">
          <h1>
            <br />
            <br />
            데이터와 혜택의 만남
            <br />
            스마트한 상권 분석의 시작
            <div className="title-container">
              <img
                className="apple"
                src="/assets/toss-symbol.svg"
                alt="토스 심볼"
              />
              <span className="title-text">Toss Beneface</span>
            </div>
          </h1>
        </section>
      </section>

      {/* 소개 문구 */}
      <p className="top-text">
        내가 속한 상권과 나의 매장을 비교 분석하여
        <br />
        이제껏 경험 못 했던 쉽고 편리한 시각화 및 솔루션,
        <br />
        개인의 할인 혜택 정리까지
        <br />
        toss Beneface와 함께라면 당신의 일상이 새로워질 거예요.
      </p>

      {/* phone 섹션 */}
      <section className="phone">
        <p className="phone_title">홈 · 상권분석</p>
        <p className="phone_detail1">
          매장 관리,
          <br />
          비교부터 전략생성까지
          <br />
          똑똑하게
        </p>
        <div className="iphone">
          <img
            className="consum"
            src="/assets/app-consume.svg"
            alt="소비"
          />
          
        </div>
        <div className="iphone">
          <img
            className="app_main"
            src="/assets/app-main.svg"
            alt="토스 메인"
          />
          
        </div>
        <p className="phone_detail2">
          Toss Place를 사용해 보세요.
          <br />
          경쟁 업채와의 비교분석은 기본
          <br />
          분석에 대한 전략 생성까지 한번에 제공합니다.
        </p>
      </section>

      {/* rem 섹션 */}
      <section className="rem">
        <section className="rem_title">
          <p className="rem_main_title">혜택</p>
          <p className="rem_sub_title">
            간편하고 빠르게
            <br />
            카드 혜택은 최대로,
            <br />
            이런 서비스 써보셨나요?
            <br />
          </p>
        </section>
        <section className="rem_cont1">
          <div className="rem_cont1_title">
            <svg
              className="rem_cont1_logo"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
            >
              <path fill="none" d="M0 0h24v24H0z" />
              <g
                fill="none"
                stroke="#007ff2"
                strokeWidth="1.8"
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeMiterlimit="10"
              >
                <path d="M16.25 8.92l-1.85 6.71c-.06.2-.34.2-.4 0l-1.8-6.56c-.06-.2-.34-.2-.4 0L10 15.63c-.06.2-.34.2-.4 0L7.75 8.92M15.3 12.35H18M8.7 12.35H6" />
              </g>
              <circle cx="12" cy="1.97" r="1.98" fill="#4d9cff" />
              <circle
                transform="rotate(-60 3.313 6.985)"
                cx="3.31"
                cy="6.98"
                fill="#74b6ff"
                r="1.98"
              />
              <circle
                transform="rotate(-30 3.312 17.015)"
                cx="3.31"
                cy="17.02"
                fill="#007ff2"
                r="1.98"
              />
              <circle cx="12" cy="22.03" r="1.98" fill="#4d9cff" />
              <circle
                transform="rotate(-60 20.687 17.016)"
                cx="20.69"
                cy="17.02"
                fill="#74b6ff"
                r="1.98"
              />
              <circle
                transform="rotate(-30 20.686 6.986)"
                cx="20.69"
                cy="6.98"
                fill="#007ff2"
                r="1.98"
              />
            </svg>
            <p className="rem_cont1_det1">평생 무료 할인 혜택</p>
          </div>
          <p className="rem_cont1_det2">
            토스 평생 무료혜택으로
            <br />
            모두의 혜택에 자유를
          </p>
          <p className="rem_cont1_det3">
            어디서 결제하든 은행 상관 없이,
            <br />
            이제 토스와 함께 최고의 할인 혜택과 실적관리를.
          </p>
          <img
            className="rem_cont1_pic"
            src="https://static.toss.im/illusts-content/img-bankfeed-check-ep4-cover.png"
            alt="송금 완료 화면"
          />
        </section>
        <section className="rem_cont2">
          <div className="rem_cont2_title">
            <svg
              className="rem_cont2_logo"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
            >
              <path
                d="M11.6.7L2.9 4.2c-.4.2-.7.6-.7 1v8.3c0 3.8 4.8 7.5 9.3 9.8.3.1.6.1.9 0 4.5-2.3 9.3-6 9.3-9.8V5.2c0-.4-.2-.8-.6-.9L12.4.7c-.3-.1-.6-.1-.8 0z"
                fill="#007ff7"
              />
              <path
                d="M10.9 15.8c-.3 0-.5-.1-.7-.3l-3.4-3.4c-.4-.4-.4-1 0-1.4.4-.4 1-.4 1.4 0l2.7 2.7 4.9-4.9c.4-.4 1-.4 1.4 0s.4 1 0 1.4l-5.6 5.6c-.2.2-.4.3-.7.3z"
                fill="#fff"
              />
            </svg>
            <p className="rem_cont2_det1">음성인식</p>
          </div>
          <p className="rem_cont2_det2">
            원하는 메뉴 찾기 어려우셨나요?
            <br />
            원하는 메뉴가 있다면 음성으로 찾기
          </p>
          <p className="rem_cont2_det3">
            원하는 메뉴가 없다면 토스가 알아서 비슷한 메뉴를 추천해드려요.
            <br />
            메뉴를 text가 아닌 음성으로 추천받을 수 있어요.
            <br />
          </p>
          <img
            className="rem_cont2_pic"
            src="https://img.etnews.com/photonews/1907/1200536_20190701171033_036_0002.jpg"
            alt="사기 조회"
          />
        </section>
      </section>

      {/* credit 섹션 */}
      <section className="credit">
        <div className="credit_title">
          <div className="credit_title_sub">얼굴인식</div>
          <div className="credit_title_main">
            현금, 카드뿐만이 아닌
            <br />
            휴대폰없이도
            <br />
            얼굴로 결제하세요.
            <br />
          </div>
        </div>
        <div className="credit_score">
          <img
            className="credit_score_pic"
            src="/assets/credit-score.svg"
            alt="신용점수"
          />
        </div>
      </section>

      {/* 이미지 섹션 */}
      <section className="img">
        <div className="first">
          <img
            className="img1"
            src="https://static.toss.im/assets/homepage/newtossim/section2_4_big.jpg"
            alt="이미지1"
          />
          <p className="img1_title">꼭 필요했던 서비스</p>
        </div>
        <div className="second">
          <p className="img2_title">
            토스로
            <br />
            나에게 딱 맞게
          </p>
          <img
            className="img2"
            src="https://static.toss.im/assets/homepage/newtossim/section2_1_document.jpg"
            alt="이미지2"
          />
        </div>
      </section>

      {/* end 섹션 */}
      <section className="end">
        <img
          className="end_img"
          src="https://static.toss.im/assets/homepage/newtossim/section4_device.jpg"
          alt="사업 이미지"
        />
        <p className="end_title_main">사업도 토스와 함께</p>
        <p className="end_title_sub">
          사업을 시작하셨나요?
          <br />
          사업의 시작부터 관리까지
          <br />
          이제 토스와 함께 하세요.
        </p>
        <div className="end_pay">
          <p className="end_pay_title">토스결제</p>
          <p className="end_pay_det">
            합리적인 수수료,
            <br />
            간편한 결제 경험으로 비용은
            <br />
            절감하고 매출은 늘리세요.
          </p>
          <button className="end_pay_button">가맹점 문의하기</button>
        </div>
        <div className="end_sales">
          <p className="end_sales_title">내 매출 장부</p>
          <p className="end_sales_det">
            내 매출 장부 따로 관리할 필요 없어요.
            <br />
            총 매출, 총 입금, 총 지출을 보기 쉽게
            <br />
            알려드려요.
          </p>
          <button className="end_sales_button">자세히 알아보기</button>
        </div>
        <div className="end_tosspayment">
          <p className="end_tosspayment_title">토스페이먼트</p>
          <p className="end_tosspayment_det">
            시작하기 어려웠던 온라인 비즈니스,
            <br />
            온라인 결제 토스페이먼츠와
            <br />
            함께 해보세요.
          </p>
          <button className="end_tosspayment_button">홈페이지 바로가기</button>
        </div>
      </section>
      <div className="floating-bar" onClick={handleFloatingBarClick}>
        서비스 이용해보기
      </div>
    </div>
  );
};

export default MainPage;
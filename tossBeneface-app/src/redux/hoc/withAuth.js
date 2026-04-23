// src/hoc/withAuth.js
import React, { useEffect, useState } from "react";
import { useSelector, useDispatch } from "react-redux";
import { refreshAccessToken } from "../auth/authSlice";
import LoginPage from "../../pages/LoginPage";

const withAuth = (WrappedComponent) => {

  return (props) => {
    const accessToken = useSelector((state) => state.auth.accessToken);
    const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);
    const dispatch = useDispatch();
    const [showLoginModal, setShowLoginModal] = useState(false);

    useEffect(() => {
      console.log("Checking authentication status...");
      console.log("isAuthenticated:", isAuthenticated);
      console.log("accessToken:", accessToken);

        // 🔥 인증 상태가 유효하지 않을 때만 처리
        if (!isAuthenticated) {
            setShowLoginModal(true); // 로그아웃 상태라면 로그인 모달 표시
            return;
        }
         // 🔥 accessToken이 없을 때만 refreshAccessToken 호출
        if (!accessToken) {
            dispatch(refreshAccessToken())
            .unwrap()
            .catch(() => {
            // 인증 실패 시 로그인 모달 표시
            setShowLoginModal(true);
          });
        }
    }, [accessToken, isAuthenticated, dispatch]);

    // 모달 닫기 핸들러
    const closeModal = () => {
      setShowLoginModal(false);
    };

    // 로그인 성공 후 콜백
    const onLoginSuccess = () => {
      setShowLoginModal(false);
      dispatch(refreshAccessToken()); // ✅ 로그인 직후 토큰 갱신 실행
    };

    return (
      <>
        {/* 로그인 모달 */}
        {showLoginModal && <LoginPage isModal={true} onClose={closeModal} onLoginSuccess={onLoginSuccess} />}

        {/* 액세스 토큰이 없으면 모달을 띄우기 때문에, 모달이 닫히기 전에는 원래 컴포넌트 렌더링 방지 */}
        {isAuthenticated && accessToken ? <WrappedComponent {...props} /> : null}
      </>
    );
  };
};

export default withAuth;

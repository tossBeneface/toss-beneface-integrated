import { clearAccessToken } from "../redux/auth/authSlice";
import { persistor } from "../redux/store";
import { logout } from "../api/logoutApi";
import { handleAuthError } from "../utils/errorHandler";

/**
 * 로그아웃 처리 함수
 * @param {Function} dispatch Redux dispatch 함수
 * @param {Function} navigate React Router의 navigate 함수
 */
const handleLogout = async (dispatch, navigate) => {
  try {
    // Redux 상태 초기화
    dispatch(clearAccessToken());

    // Persist 상태 초기화
    await persistor.purge();

    // 로그아웃 API 호출
    await logout(navigate);

    // 로그아웃 완료 메시지
    alert("로그아웃 되었습니다.");

    // 로그인 페이지로 이동
    navigate("/login");
  } catch (error) {
    // 에러 처리
    handleAuthError(error, navigate, "로그아웃 요청 실패:");
  }
};

export default handleLogout;

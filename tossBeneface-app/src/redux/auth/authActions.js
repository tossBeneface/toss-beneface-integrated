import { clearAccessToken } from "../auth/authSlice";
import { logout } from "../../api/logoutApi"; // 로그아웃 API 호출
import { persistor } from "../store"; // Redux Persist의 persistor 가져오기

export const handleLogout = () => async (dispatch) => {
    try {
        // 서버에 로그아웃 요청
        await logout();

        // Redux 상태 초기화
        dispatch(clearAccessToken());

        // Redux Persist 저장소 초기화
        await persistor.purge(); // Persist 저장소를 초기화하여 상태 삭제

        console.log("로그아웃 완료 및 Persist 상태 초기화");
    } catch (error) {
        console.error("로그아웃 처리 중 오류 발생:", error);
    }
};

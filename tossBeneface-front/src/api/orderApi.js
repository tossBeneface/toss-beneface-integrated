import apiClient from "./apiClient";
import { handleAuthError } from "../utils/errorHandler";

export const order = async (requestDto, navigate) => {
    try {
        console.log("📌 order 요청 데이터:", requestDto); // ✅ 추가

        // 🔹 API 요청 (apiClient가 자동으로 토큰 포함)
        const response = await apiClient.post("/orders", requestDto);

        console.log("✅ order 응답:", response.data);
        return response.data;
    } catch (error) {
        console.error("🚨 order 오류:", error.response?.data || error.message); // ✅ 추가
        handleAuthError(error, navigate, "주문 API 호출 중 오류 발생:");
        throw error;
    }
};

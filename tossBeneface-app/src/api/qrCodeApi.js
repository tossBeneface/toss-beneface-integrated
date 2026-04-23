import apiClient from "./apiClient";
import { handleAuthError } from "../utils/errorHandler";

export const qrCodeGenerate = async (navigate) => {
    try {
        const response = await apiClient.get("/qr/generate", { responseType: 'blob' });
        console.log("✅ qrCodeGenerate 응답:", response.data);
        return response.data;
    } catch (error) {
        console.error("🚨 qrCodeGenerate 오류:", error.response?.data || error.message);
        handleAuthError(error, navigate, "qrCodeGenerate 호출 중 오류 발생:"); // navigate 전달
        throw error;
    }
};

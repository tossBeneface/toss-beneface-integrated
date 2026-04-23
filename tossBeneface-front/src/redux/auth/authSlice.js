// src/redux/authSlice.js
import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios"; // 추가

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

// 🔥 refreshAccessToken를 authSlice.js 내부에서 정의
export const refreshAccessToken = createAsyncThunk(
    "auth/refreshAccessToken",
    async (_, { rejectWithValue }) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/access-token/issue`, {}, { withCredentials: true });
            return response.data.accessToken;
        } catch (error) {
            return rejectWithValue(error.response?.data || "토큰 갱신 실패");
        }
    }
);


const authSlice = createSlice({
    name: "auth",
    initialState: {
        accessToken: null,
        isAuthenticated: false, // 인증 여부 추가
        error: null, // refreshAccessToken 관련 에러 관리
    },
    reducers: {
        setAccessToken: (state, action) => {
            state.accessToken = action.payload;
            state.isAuthenticated = true; // 토큰 설정 시 인증 상태 true
        },
        clearAccessToken: (state) => {
            state.accessToken = null;
            state.isAuthenticated = false; // 토큰 제거 시 인증 상태 false
            state.error = null; // 에러 상태 초기화
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(refreshAccessToken.fulfilled, (state, action) => {
                state.accessToken = action.payload; // 새로 발급된 토큰 설정
                state.isAuthenticated = true; // 인증 상태 true로 설정
                state.error = null; // 에러 초기화
            })
            .addCase(refreshAccessToken.rejected, (state, action) => {
                state.error = action.payload; // 에러 메시지 저장
                state.isAuthenticated = false; // 인증 상태 false
            })
             // 🔥 새로고침 후 accessToken 복원
             .addDefaultCase((state, action) => {
                if (action.type === "persist/REHYDRATE" && action.payload?.auth) {
                    state.accessToken = action.payload.auth.accessToken || null;
                    state.isAuthenticated = !!action.payload.auth.accessToken;
                }
            });
    },
});

export const { setAccessToken, clearAccessToken } = authSlice.actions;
export default authSlice.reducer;

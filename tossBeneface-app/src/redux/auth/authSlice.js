import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios"; // 추가

const API_BASE_URL = "http://localhost:8080";

// 🔥 액세스 토큰 갱신 (리프레시 토큰은 HTTP-Only 쿠키에서 자동 처리됨)
export const refreshAccessToken = createAsyncThunk(
    "auth/refreshAccessToken",
    async (_, { rejectWithValue }) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/api/access-token/issue`, {}, {
                 withCredentials: true, headers: {
                    "Content-Type": "application/json",
                  },
                });
            return response.data.accessToken;
        } catch (error) {
            return rejectWithValue(error.response?.data || "토큰 갱신 실패");
        }
    }
);

// 🔥 로그인 처리
export const login = createAsyncThunk(
    "auth/login",
    async ({ email, password }, { rejectWithValue }) => {
      try {
        console.log("Login request payload:", { email, password });
        const response = await axios.post(`${API_BASE_URL}/api/login`, { email, password }, { withCredentials: true });
        return response.data.accessToken; // 서버에서 받은 액세스 토큰 반환
      } catch (error) {
        return rejectWithValue(error.response?.data || "로그인 실패");
      }
    }
  );

const authSlice = createSlice({
    name: "auth",
    initialState: {
        accessToken: null, // 🚨 액세스 토큰은 persist에서 제외
        isAuthenticated: false,  // 🔥 인증 상태만 persist로 저장
        error: null, // refreshAccessToken 관련 에러 상태
    },
    reducers: {
        setAccessToken: (state, action) => {
            state.accessToken = action.payload;
            state.isAuthenticated = true; // 토큰 설정 시 인증 상태 true
        },
        clearAccessToken: (state) => {
            state.accessToken = null; // 🚨 accessToken 초기화
            state.isAuthenticated = false; // 토큰 제거 시 인증 상태 false
            state.error = null; // 에러 상태 초기화
        },
    },
    extraReducers: (builder) => {
        builder
             // 🔥 로그인 성공
            .addCase(login.fulfilled, (state, action) => {
                console.log("Login Success:", action.payload);
                state.accessToken = action.payload; // 액세스 토큰 메모리에만 저장
                state.isAuthenticated = true; // 인증 상태 업데이트
                state.error = null; // 로그인 성공 시 에러 초기화
            })
            .addCase(login.rejected, (state, action) => {
                state.accessToken = null; // 로그인 실패 시 토큰 제거
                state.isAuthenticated = false; // 인증 상태 false
                state.error = action.payload || "로그인 실패"; // 에러 메시지 저장
            })
             // 🔥 토큰 갱신 성공
            .addCase(refreshAccessToken.fulfilled, (state, action) => {
                state.accessToken = action.payload; // 새로 발급된 토큰 설정
                state.isAuthenticated = true; // 인증 상태 true로 설정
                state.error = null; // 에러 초기화
            })
            .addCase(refreshAccessToken.rejected, (state, action) => {
                state.accessToken = null; // 액세스 토큰 제거
                state.isAuthenticated = false; // 인증 상태 false
                state.error = action.payload || "토큰 갱신 실패"; // 에러 메시지 저장
            
            })
             // 🚨 persist 저장 시 액세스 토큰 제외 (새로고침 시 인증 상태 유지) - 새로고침 후 accessToken 복원
             .addDefaultCase((state, action) => {
                if (action.type === "persist/REHYDRATE" && action.payload?.auth) {
                    state.isAuthenticated = !!action.payload.auth.accessToken;
                    state.accessToken = null; // persist에서 복원하지 않음 (보안 강화)
                }
            });
    },
});

export const { setAccessToken, clearAccessToken } = authSlice.actions;
export default authSlice.reducer;
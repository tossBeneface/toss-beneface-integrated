// src/redux/store.js
import { configureStore } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from "redux-persist";
import storage from "redux-persist/lib/storage"; // localStorage 사용 : 인증 상태만 저장
import authReducer from "./auth/authSlice";

const persistConfig = {
    key: "auth",
    storage,
    whitelist: ["isAuthenticated"], // isAuthenticated만 저장
};

const persistedReducer = persistReducer(persistConfig, authReducer);

const store = configureStore({
    reducer: {
        auth: persistedReducer,
    },
    devTools: process.env.NODE_ENV !== "production", // 개발 모드에서 DevTools 활성화
});

export const persistor = persistStore(store);
export default store;

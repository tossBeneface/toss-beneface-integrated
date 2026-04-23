import { createBrowserRouter } from "react-router-dom";
import LazyLoad from "../components/LazyLoad"; // 공통 LazyLoad 컴포넌트 import
import { lazy } from 'react';
import withAuth from "../redux/hoc/withAuth";

// 페이지 컴포넌트 Lazy 로드
const Home = lazy(() => import("../pages/MainPage"));
const Join = lazy(() => import("../pages/JoinPage"));
const PasswordSetupPage = lazy(() => import("../pages/PasswordSetupPage")); 
const Login = lazy(() => import("../pages/LoginPage"));
const Profile = lazy(() => import("../pages/ProfilePage"));
const FaceRecognition = lazy(() => import("../pages/FaceRecognition"));
const CardRegisterPage = lazy(() => import("../pages/CardRegisterPage"));
const CardScanPage = lazy(() => import("../pages/CardScanPage"));
const CardInputPage = lazy(() => import("../pages/CardInputPage"));
const CardPreviewPage = lazy(() => import("../pages/CardPreviewPage"));
const TermsDetailPage = lazy(() => import("../pages/TermsDetailPage"));
const QRCodeGenerator = lazy(() => import("../pages/QrCode"));

const root = createBrowserRouter([
     // 1. 인증이 필요 없는 페이지
    { path: "join", element: LazyLoad(Join) },
    { path : "/terms/:id", element: LazyLoad(TermsDetailPage) },
    { path: "password-setup", element: LazyLoad(PasswordSetupPage) },
    { path: "/login", element: LazyLoad(Login) },

    // 2. 인증이 필요한 페이지
    { path: "/", element: LazyLoad(withAuth(Home)) },
    { path: "home", element: LazyLoad(withAuth(Home)) },
    { path: "profile", element: LazyLoad(withAuth(Profile)) },
    { path: "/qrcode", element: LazyLoad(withAuth(QRCodeGenerator)) },
    // { path: "logout", element: LazyLoad(withAuth(Logout)) },
    { path: "FaceRecognition", element: LazyLoad(withAuth(FaceRecognition)) }, // 얼굴 등록
    { path: "card-register", element: LazyLoad(withAuth(CardRegisterPage)) }, // 카드 등록
    { path: "card-scan", element: LazyLoad(withAuth(CardScanPage)) }, // 카드 스캔
    { path: "card-input", element: LazyLoad(withAuth(CardInputPage)) }, // 카드 입력
    { path: "card-preview", element: LazyLoad(withAuth(CardPreviewPage)) } // 카드 확인
]);

export default root;

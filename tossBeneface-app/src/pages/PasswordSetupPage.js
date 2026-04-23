import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useDispatch } from "react-redux";
import { join } from "../api/authApi";
import { setAccessToken } from "../redux/auth/authSlice";
import AlertModal from "../components/AlertModal";

const PasswordSetupPage = () => {
    const [password, setPassword] = useState("");
    const [isLetterMode, setIsLetterMode] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const navigate = useNavigate();
    const dispatch = useDispatch();

    // JoinPage에서 전달된 데이터 가져오기
    const location = useLocation();
    const userData = location.state || {};

    const handleNumberInput = (num) => {
        if (password.length < 4) {
            setPassword((prev) => prev + num);
            if (password.length + 1 === 4) {
                setIsLetterMode(true);
            }
        }
    };

    const handleLetterInput = (letter) => {
        if (isLetterMode && password.length < 5) {
            const newPassword = password + letter;
            setPassword(newPassword);
            if (newPassword.length === 5) {
                // 회원가입 완료 로직
                handleRegistration(newPassword);
            }
        }
    };

    const handleDelete = () => {
        setPassword((prev) => prev.slice(0, -1));
        if (password.length <= 4) {
            setIsLetterMode(false);
        }
    };

    const handleRegistration = async (finalPassword) => {
        try {
            // 이메일 조합
            const email = userData.useCustomDomain
                ? `${userData.emailId}@${userData.customEmailDomain}`
                : `${userData.emailId}@${userData.emailDomain}`;

            // 최종 데이터 구성
            const completeData = {
                email,
                password: finalPassword,
                memberName: userData.memberName,
                phoneNumber: userData.phoneNumber,
                gender: userData.gender.toUpperCase(),
                role: "USER",
            };

            // API 호출
            const response = await join(completeData);

            // 서버로부터 받은 accessToken 저장
            dispatch(setAccessToken(response.accessToken));

            // 회원가입 성공 후 모달 표시
            setShowSuccessModal(true);
        } catch (error) {
            console.error(error);
            alert("회원가입에 실패했습니다. 다시 시도해주세요.");
        }
    };

    const handleCloseModal = () => {
        setShowSuccessModal(false);
        navigate("/"); // 모달 닫은 후 리다이렉트
    };

    return (
        <div
            className="flex flex-col items-center justify-center bg-gray-900 text-white text-center 
                       w-full max-w-[430px] min-h-screen mx-auto p-4"
        >
            <h1 className="text-lg font-semibold mb-4">토스에서 사용할 비밀번호를 입력해주세요</h1>
            <p className="text-sm text-gray-300 mb-6">숫자 4자리 + 영문자 1자리</p>

            <div className="flex items-center justify-center space-x-2 mb-4">
                {/* 숫자 입력란 */}
                {Array(4)
                    .fill("")
                    .map((_, index) => (
                        <div
                            key={index}
                            className={`w-8 h-8 rounded-full border-2 flex items-center justify-center text-lg font-bold ${
                                password[index] ? "bg-white text-black" : "border-gray-500"
                            }`}
                        >
                            {password[index] && showPassword ? password[index] : ""}
                        </div>
                    ))}

                {/* + 기호 */}
                <span className="text-xl font-bold text-gray-300">+</span>

                {/* 영문 입력란 */}
                <div
                    className={`w-8 h-8 rounded-full border-2 flex items-center justify-center text-lg font-bold ${
                        password[4] ? "bg-white text-black" : "border-gray-500"
                    }`}
                >
                    {password[4] && showPassword ? password[4] : ""}
                </div>
            </div>

            <button
                className="text-sm text-blue-400 mb-6"
                onClick={() => setShowPassword(!showPassword)}
            >
                {showPassword ? "입력값 숨기기" : "입력값 보기"}
            </button>

            {/* 키패드 */}
            <div
                className="grid justify-items-center"
                style={{
                    gap: isLetterMode ? "8px" : "12px",
                    gridTemplateColumns: isLetterMode ? "repeat(10, 1fr)" : "repeat(3, 1fr)",
                }}
            >
                {!isLetterMode
                    ? Array(10)
                          .fill(0)
                          .map((_, index) => (
                              <button
                                  key={index}
                                  onClick={() => handleNumberInput(index)}
                                  className="w-20 h-20 text-3xl text-white bg-gray-800 hover:bg-gray-700 focus:outline-none"
                              >
                                  {index}
                              </button>
                          ))
                    : (() => {
                          const rows = [
                              ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"],
                              ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
                              ["Z", "X", "C", "V", "B", "N", "M"],
                          ];
                          return rows.flat().map((letter) => (
                              <button
                                  key={letter}
                                  onClick={() => handleLetterInput(letter)}
                                  className="w-8 h-8 text-lg bg-gray-800 rounded hover:bg-gray-700 focus:outline-none"
                              >
                                  {letter}
                              </button>
                          ));
                      })()}
            </div>

            <button
                className="mt-6 w-20 h-12 rounded-full bg-red-600 text-white font-bold"
                onClick={handleDelete}
            >
                삭제
            </button>

            {/* 회원가입 성공 알림 모달 */}
            {showSuccessModal && (
                <AlertModal message="회원가입이 성공적으로 완료되었습니다!" onClose={handleCloseModal} />
            )}
        </div>
    );
};

export default PasswordSetupPage;
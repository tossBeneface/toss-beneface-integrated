import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { login } from "../redux/auth/authSlice";
import { useNavigate } from "react-router-dom";

const LoginPage = ({ isModal = false, onClose, onLoginSuccess }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false); // 비밀번호 표시 상태
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      console.log("Submitting login:", { email, password });
      await dispatch(login({ email, password })).unwrap();
      setError("");
      if (onLoginSuccess) onLoginSuccess();
    } catch (err) {
      setError("로그인 실패. 다시 시도해주세요.");
    }
  };

  const navigateToJoin = () => {
    navigate("/join");
  };

  return (
    <div
      className={`flex justify-center items-center h-screen ${
        isModal ? "bg-black bg-opacity-50 fixed inset-0" : "bg-gray-50"
      }`}
    >
      <div className="w-96 bg-white p-8 shadow-lg rounded-lg text-center">
        <div className="flex justify-start items-center mb-6 pl-9">
          <img
            src="/Toss_Symbol_Primary.png"
            alt="Toss Logo"
            className="h-10 mr-2"
          />
          <span className="text-2xl font-bold text-gray-800">
            toss <span className="font-light">Beneface</span>
          </span>
        </div>
        {error && (
          <p className="text-red-500 text-center text-sm mb-4">{error}</p>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <input
              type="email"
              placeholder="이메일"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          <div className="mb-4 relative">
            <input
              type={showPassword ? "text" : "password"}
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 bottom-2 text-xs text-gray-500 focus:outline-none hover:underline"
            >
              {showPassword ? "숨기기" : "보기"}
            </button>
          </div>
          <button
            type="submit"
            className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 transition"
          >
            로그인
          </button>
        </form>
        <button
          onClick={navigateToJoin}
          className="mt-4 w-full bg-gray-200 text-blue-500 py-2 rounded-md hover:bg-gray-300 transition"
        >
          회원가입
        </button>
        {isModal && (
          <button
            onClick={onClose}
            className="mt-4 w-full bg-gray-100 text-gray-500 py-2 rounded-md hover:bg-gray-200 transition"
          >
            닫기
          </button>
        )}
      </div>
    </div>
  );
};

export default LoginPage;

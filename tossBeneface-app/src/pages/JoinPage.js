import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import TermsModal from "../components/menus/TermsModal";

const JoinPage = () => {
    const [agreedToTerms, setAgreedToTerms] = useState(false); // 약관 동의 여부
    const [formData, setFormData] = useState({
        phoneNumber: "",
        emailId: "",
        emailDomain: "naver.com",
        customEmailDomain: "",
        useCustomDomain: false,
        gender: "",
        memberName: "",
    });

    const navigate = useNavigate();

    const handleAgree = () => {
        setAgreedToTerms(true); // 약관 동의 완료
      };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handleEmailDomainChange = (e) => {
        const value = e.target.value;
        if (value === "custom") {
            setFormData((prev) => ({ ...prev, useCustomDomain: true, customEmailDomain: "" }));
        } else {
            setFormData((prev) => ({ ...prev, useCustomDomain: false, emailDomain: value }));
        }
    };

    const validateEmail = (email) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    const handleGenderChange = (e) => {
        const value = e.target.value;
        const mappedValue = value === "남성" ? "MALE" : value === "여성" ? "FEMALE" : "";
        setFormData((prev) => ({ ...prev, gender: mappedValue }));
    };    

    const handleSubmit = (e) => {
        e.preventDefault();
        const email = formData.useCustomDomain
            ? `${formData.emailId}@${formData.customEmailDomain}`
            : `${formData.emailId}@${formData.emailDomain}`;

        // 이메일 유효성 검사
        if (!validateEmail(email)) {
            alert("올바른 이메일 형식을 입력해주세요.");
            return;
        }

        // 추가 필드 유효성 검사
        if (!formData.gender || !formData.memberName) {
            alert("모든 필드를 올바르게 입력해주세요.");
            return;
        }

        const completeData = { 
            ...formData, 
            email,
            gender: formData.gender.trim().toUpperCase(),
        };

        // 불필요한 필드 제거
        delete completeData.useCustomDomain;
        delete completeData.customEmailDomain;

        // 비밀번호 설정 페이지로 데이터 전달
        navigate("/password-setup", { state: completeData });

    };

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 px-4" style={{ maxWidth: "430px", margin: "0 auto" }}>
            {/* 약관 동의 모달 */}
            {!agreedToTerms && (
                <TermsModal 
                    onAgree={handleAgree} 
                    onClose={() => alert("약관에 동의해야 가입을 진행할 수 있습니다.")} 
                />
            )}
            {/* 회원가입 폼 */}
            <div className="flex-1 flex flex-col justify-center w-full bg-white p-6 rounded-lg shadow-md">
                <h1 className="text-xl font-semibold text-left text-gray-800 mb-8 leading-relaxed">
                    입력한 정보가 맞다면<br />아래 확인 버튼을 눌러주세요.
                </h1>
                <form onSubmit={handleSubmit} className="space-y-8">
                    <div>
                        <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700 mb-2">
                            휴대폰 번호
                        </label>
                        <input
                            type="text"
                            id="phoneNumber"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleChange}
                            className="block w-full h-12 rounded-md border-gray-300 shadow-sm focus:outline-none focus:bg-transparent focus:shadow-none appearance-none"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                            이메일
                        </label>
                        <div className="flex items-center space-x-2">
                            <input
                                type="text"
                                id="emailId"
                                name="emailId"
                                value={formData.emailId}
                                onChange={handleChange}
                                placeholder="이메일 입력"
                                className="w-1/3 h-12 rounded-md border-gray-300 shadow-sm focus:outline-none focus:bg-transparent focus:shadow-none appearance-none"
                                required
                            />
                            <span>@</span>
                            <input
                                type="text"
                                id="customEmailDomain"
                                name="customEmailDomain"
                                value={formData.useCustomDomain ? formData.customEmailDomain : formData.emailDomain}
                                onChange={handleChange}
                                className="w-1/3 h-12 rounded-md border-gray-300 shadow-sm focus:outline-none focus:bg-transparent focus:shadow-none appearance-none"
                                readOnly={!formData.useCustomDomain}
                                required
                            />
                            <select
                                id="emailDomain"
                                name="emailDomain"
                                value={formData.useCustomDomain ? "custom" : formData.emailDomain}
                                onChange={handleEmailDomainChange}
                                className="w-1/4 h-12 text-sm rounded-md border-gray-300 shadow-sm focus:outline-none focus:bg-transparent focus:shadow-none appearance-none"
                            >
                                <option value="naver.com">naver.com</option>
                                <option value="nate.com">nate.com</option>
                                <option value="hanmail.net">hanmail.net</option>
                                <option value="gmail.com">gmail.com</option>
                                <option value="hotmail.com">hotmail.com</option>
                                <option value="yahoo.com">yahoo.com</option>
                                <option value="custom">직접 입력</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <label htmlFor="gender" className="block text-sm font-medium text-gray-700 mb-2">
                            성별
                        </label>
                        <select
                            id="gender"
                            name="gender"
                            value={formData.gender === "MALE" ? "남성" : formData.gender === "FEMALE" ? "여성" : ""}
                            onChange={handleGenderChange}
                            className="block w-full h-12 rounded-md border-gray-300 shadow-sm focus:outline-none focus:bg-transparent focus:shadow-none appearance-none"
                            required
                        >
                            <option value="">선택</option>
                            <option value="남성">남성</option>
                            <option value="여성">여성</option>
                        </select>
                    </div>
                    <div>
                        <label htmlFor="memberName" className="block text-sm font-medium text-gray-700 mb-2">
                            이름
                        </label>
                        <input
                            type="text"
                            id="memberName"
                            name="memberName"
                            value={formData.memberName}
                            onChange={handleChange}
                            className="block w-full h-12 rounded-md border-gray-300 shadow-sm focus:outline-none focus:bg-transparent focus:shadow-none appearance-none"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="block w-full py-3 text-white bg-tossBlue hover:bg-tossBlueHover rounded-md focus:outline-none"
                    >
                        확인
                    </button>
                </form>
            </div>
        </div>
    );
};

export default JoinPage;

import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { qrCodeGenerate } from "../api/qrCodeApi";
import "./QRCode.css";
import MenuModal from "../components/menus/MenuModal";
import handleLogout from "../utils/handleLogout"; 
import { useDispatch } from "react-redux";

const QRCodeGenerator = () => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [qrCodeUrl, setQrCodeUrl] = useState('');
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false); // 메뉴 모달 상태

    useEffect(() => {
        fetchQRCode();
    }, []);

    // QR 코드 갱신 함수
    const fetchQRCode = async () => {
        setLoading(true);
        try {
            const response = await qrCodeGenerate();
            const qrCodeImage = URL.createObjectURL(response);
            setQrCodeUrl(qrCodeImage);
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="qr-main-container">
            {/* iPhone 상단 노치 스타일 */}
            {/* <div className="phone-status-bar">9:41</div>2 */}

            {/* 헤더 영역 */}
            <header className="header-area">
                <button className="back-button" onClick={() => navigate(-1)}>
                    {"<"}
                </button>
                <h3 className="header-title">본인인증</h3>
                <div className="header-right-icon">{""}</div> {/* 공백 유지 */}
            </header>

            {/* QR 코드 본문 영역 */}
            <div className="qr-code-content">
                <div className="qr-code-wrapper">
                    {loading ? (
                        <div className="loader">Loading...</div>
                    ) : (
                        <img className="qr-code" src={qrCodeUrl} alt="QR Code" />
                    )}
                </div>
                <button className="refresh-button" onClick={fetchQRCode}>
                    QR 생성
                </button>
            </div>

            {/* 메뉴 모달 */}
            <MenuModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onProfileClick={() => {
                    navigate("/profile");
                    setIsModalOpen(false);
                }}
                onLogoutClick={async () => {
                    await handleLogout(dispatch, navigate);
                    setIsModalOpen(false);
                }}
            />
        </div>
    );
};

export default QRCodeGenerator;

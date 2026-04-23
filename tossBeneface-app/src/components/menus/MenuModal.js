import React from "react";

const MenuModal = ({ isOpen, onClose, onProfileClick, onLogoutClick }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white rounded-lg p-6 w-80">
        <h2 className="text-xl font-bold mb-4 text-center">메뉴</h2>
        <button
          className="w-full bg-blue-500 text-white py-2 px-4 rounded-lg mb-3 hover:bg-blue-600"
          onClick={onProfileClick}
        >
          회원정보조회
        </button>
        <button
          className="w-full bg-red-500 text-white py-2 px-4 rounded-lg mb-3 hover:bg-red-600"
          onClick={onLogoutClick}
        >
          로그아웃
        </button>
        <button
          className="w-full bg-gray-300 text-gray-800 py-2 px-4 rounded-lg hover:bg-gray-400"
          onClick={onClose}
        >
          닫기
        </button>
      </div>
    </div>
  );
};

export default MenuModal;

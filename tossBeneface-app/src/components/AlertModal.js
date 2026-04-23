import React from "react";

const AlertModal = ({ message, onClose }) => {
    return (
        <div className="fixed inset-0 flex items-center justify-center bg-gray-800 bg-opacity-50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg max-w-sm w-full">
                <h2 className="text-lg font-semibold text-gray-800 mb-4">{message}</h2>
                <div className="flex justify-center">
                    <button onClick={onClose} className="py-2 px-6 text-white bg-blue-500 hover:bg-blue-600 rounded-md focus:outline-none">
                        확인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AlertModal;

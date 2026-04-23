import React from "react";

const LoadingScreen = () => {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "100vh",
        backgroundColor: "#ffffff", // 흰색 배경
      }}
    >
      <img
        src="/Toss_Symbol_Primary.png"
        alt="Loading"
        style={{
          width: "100px", // 이미지 크기 -> 150px 테스트
          height: "100px",
          objectFit: "contain"
        }}
      />
    </div>
  );
};

export default LoadingScreen;

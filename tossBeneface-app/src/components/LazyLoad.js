import React, { Suspense } from "react";
import LoadingScreen from "./LoadingScreen"; // 파일 경로에 맞게 수정

// LazyLoad 컴포넌트 생성
const LazyLoad = (Component) => {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Component />
    </Suspense>
  );
};

export default LazyLoad;

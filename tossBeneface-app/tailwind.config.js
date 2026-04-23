/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,js,jsx,ts,tsx}"], // 공백이나 잘못된 파일 확장자 제거
  theme: {
    extend: {
      colors: {
        tossBlue: "#0064FF",
        tossGray: "#202632",
        tossLightGray: "#CACCCD",
        customBlue: "#D9EFFC",
        tossBlueHover: "#0056E0",
        tossGrayHover: "#181C24",
        tossLightGrayHover : "#C0C1C5",
        customBlueHover: "#B9D8EA",
      },
      ringWidth: {
        DEFAULT: "0px", // 기본 포커스 링 제거
      },
      ringColor: {
        DEFAULT: "transparent", // 기본 포커스 색 제거
      },
    },
  },
  plugins: [],
};
import React, { useState } from 'react';
import '../MarketAnalysisPage.css';
import Sidebar from './Sidebar';
import useMarketAnalysisData from '../hooks/useMarketAnalysisData';
import GenderSalesChart from '../components/charts/GenderSalesChart';
import AgeSalesChart from '../components/charts/AgeSalesChart';
import DayOfWeekChart from '../components/charts/DayOfWeekChart';
import TimeslotChart from '../components/charts/TimeslotChart';
import MonthlySalesChart from '../components/charts/MonthlySalesChart';

const containerStyle = {
  display: "flex",
  flexDirection: "row",
  alignItems: "flex-start",
  width: "100%",
  fontFamily: "Pretendard, sans-serif",
  backgroundColor: "#F9FAFB",
  minHeight: "100vh",
};

const mainContentStyle = {
  flex: 1,
  padding: "40px 60px",
};

const sectionStyle = {
  background: "white",
  borderRadius: "16px",
  padding: "32px",
  boxShadow: "0px 6px 16px rgba(0, 0, 0, 0.08)",
  transition: "all 0.3s ease-in-out",
  marginBottom: "32px",
  width: "100%",
};

const categoryTitleStyle = {
  fontSize: "18px",
  fontWeight: "600",
  color: "#333333",
  marginBottom: "20px",
  paddingLeft: "16px",
  borderLeft: "4px solid #2563eb",
};

export default function MarketAnalysisPageBeta() {
  const [isSidebarVisible, setIsSidebarVisible] = useState(false);
  const { merchantData, areaData, analysisResult } = useMarketAnalysisData();
  const placeName = sessionStorage.getItem("place_name");

  const isReady = merchantData && areaData;

  return (
    <div style={containerStyle}>
      {isSidebarVisible && <Sidebar />}

      <div style={mainContentStyle}>
        <div style={{ display: "flex", alignItems: "center", marginBottom: "30px" }}>
          <div style={{ cursor: "pointer", fontSize: "24px", marginRight: "16px" }}
            onClick={() => setIsSidebarVisible(prev => !prev)}>☰</div>
          <h1 style={{ fontSize: "36px", fontWeight: "800", color: "#000" }}>
            가맹점 vs 상권 데이터 시각화
          </h1>
        </div>

        {isReady && (
          <>
            <section style={sectionStyle}>
              <h2 style={categoryTitleStyle}>분기별 매출</h2>
              <MonthlySalesChart merchantData={merchantData} areaData={areaData} />
            </section>

            <section style={sectionStyle}>
              <h2 style={categoryTitleStyle}>성별 매출</h2>
              <GenderSalesChart merchantData={merchantData} areaData={areaData} />
            </section>

            <section style={sectionStyle}>
              <h2 style={categoryTitleStyle}>연령대별 매출</h2>
              <AgeSalesChart merchantData={merchantData} areaData={areaData} />
            </section>

            <section style={sectionStyle}>
              <h2 style={categoryTitleStyle}>요일별 매출</h2>
              <DayOfWeekChart merchantData={merchantData} areaData={areaData} />
            </section>

            <section style={sectionStyle}>
              <h2 style={categoryTitleStyle}>시간대별 매출</h2>
              <TimeslotChart merchantData={merchantData} areaData={areaData} />
            </section>
          </>
        )}

        {analysisResult && (
          <section className="section">
            <h2 className="section-header">
              <strong style={{ color: 'blue' }}>{placeName}</strong>에 대한 분석 결과
            </h2>
            <pre style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
              {analysisResult}
            </pre>
          </section>
        )}
      </div>
    </div>
  );
}

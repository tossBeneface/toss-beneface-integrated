import { useState, useEffect } from 'react';

function parseJson(str) {
  return JSON.parse(str.replace(/'/g, '"'));
}

export default function useMarketAnalysisData() {
  const [merchantData, setMerchantData] = useState(null);
  const [areaData, setAreaData] = useState(null);
  const [predictedData, setPredictedData] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);

  useEffect(() => {
    const analysisResultStr = sessionStorage.getItem("analysis_result");
    if (!analysisResultStr) {
      console.error("세션에 저장된 분석 결과가 없습니다.");
      return;
    }

    let data;
    try {
      data = JSON.parse(analysisResultStr);
      setAnalysisResult(data.analysis_result);
    } catch (e) {
      console.error("JSON 파싱 에러:", e);
      return;
    }

    // 가맹점 데이터 파싱
    const storeLines = data.store_data.split('\n');
    const merchantDayOfWeek = parseJson(storeLines[0]);
    const merchantTimeslot = parseJson(storeLines[1])[0];
    const merchantAge     = parseJson(storeLines[2])[0];
    const merchantGender  = parseJson(storeLines[3])[0];
    const merchantMonthly = parseJson(storeLines[4]);

    // 상권 데이터 파싱
    const districtLines = data.district_data.split('\n');
    const areaDayOfWeekObj = parseJson(districtLines[0])[0] || {};
    const areaDayOfWeek = [
      { day_of_week: "Monday",    total_sales: areaDayOfWeekObj.monday_sales    || 0 },
      { day_of_week: "Tuesday",   total_sales: areaDayOfWeekObj.tuesday_sales   || 0 },
      { day_of_week: "Wednesday", total_sales: areaDayOfWeekObj.wednesday_sales || 0 },
      { day_of_week: "Thursday",  total_sales: areaDayOfWeekObj.thursday_sales  || 0 },
      { day_of_week: "Friday",    total_sales: areaDayOfWeekObj.friday_sales    || 0 },
      { day_of_week: "Saturday",  total_sales: areaDayOfWeekObj.saturday_sales  || 0 },
      { day_of_week: "Sunday",    total_sales: areaDayOfWeekObj.sunday_sales    || 0 },
    ];
    const areaTimeslot = parseJson(districtLines[1])[0];
    const areaAge      = parseJson(districtLines[2])[0];
    const areaGender   = parseJson(districtLines[3])[0];
    const areaMonthly  = parseJson(districtLines[4]);

    // 예측 데이터 파싱
    const predictedLines  = data.predicted_data.split('\n');
    const predictedParsed = predictedLines.map(line => parseJson(line));

    setMerchantData({ dayOfWeek: merchantDayOfWeek, timeslot: merchantTimeslot, age: merchantAge, gender: merchantGender, monthly: merchantMonthly });
    setAreaData({ dayOfWeek: areaDayOfWeek, timeslot: areaTimeslot, age: areaAge, gender: areaGender, monthly: areaMonthly });
    setPredictedData(predictedParsed);
  }, []);

  return { merchantData, areaData, predictedData, analysisResult };
}

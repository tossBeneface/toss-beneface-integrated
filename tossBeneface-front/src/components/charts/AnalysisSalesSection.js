import React, { useState } from 'react';
import Select from 'react-select';
import { Bar } from 'react-chartjs-2';
import useAnalysisFilters from '../../hooks/useAnalysisFilters';
import parseNumber from '../../utils/parseNumber';

const VISUALIZATION_OPTIONS = [
  { value: '요일별 매출 금액',   label: '요일별 매출 금액' },
  { value: '시간대별 매출 금액', label: '시간대별 매출 금액' },
  { value: '성별 매출 금액',     label: '성별 매출 금액' },
  { value: '연령대별 매출 금액', label: '연령대별 매출 금액' },
];

const barChartOptions = { responsive: true, scales: { y: { beginAtZero: true } } };

function buildAvgDataset(filteredData, columns, labels, label, color) {
  const values = columns.map(col => {
    const sum = filteredData.reduce((acc, cur) => acc + parseNumber(cur[col]), 0);
    return filteredData.length > 0 ? sum / filteredData.length : 0;
  });
  return { labels, datasets: [{ label, data: values, backgroundColor: color }] };
}

function getChartData(filteredData, analysisSelection, filterTitle) {
  if (!filteredData.length) return { labels: [], datasets: [] };

  if (analysisSelection === '요일별 매출 금액') {
    return buildAvgDataset(
      filteredData,
      ['월요일_매출_금액','화요일_매출_금액','수요일_매출_금액','목요일_매출_금액','금요일_매출_금액','토요일_매출_금액','일요일_매출_금액'],
      ['월','화','수','목','금','토','일'],
      `[요일별] 평균 매출 금액 (${filterTitle})`, 'skyblue'
    );
  }
  if (analysisSelection === '시간대별 매출 금액') {
    return buildAvgDataset(
      filteredData,
      ['시간대_00~06_매출_금액','시간대_06~11_매출_금액','시간대_11~14_매출_금액','시간대_14~17_매출_금액','시간대_17~21_매출_금액','시간대_21~24_매출_금액'],
      ['00~06','06~11','11~14','14~17','17~21','21~24'],
      `[시간대별] 평균 매출 금액 (${filterTitle})`, 'lightcoral'
    );
  }
  if (analysisSelection === '성별 매출 금액') {
    return buildAvgDataset(
      filteredData,
      ['남성_매출_금액','여성_매출_금액'],
      ['남성','여성'],
      `[성별] 평균 매출 금액 (${filterTitle})`, ['lightcoral','lightgreen']
    );
  }
  if (analysisSelection === '연령대별 매출 금액') {
    return buildAvgDataset(
      filteredData,
      ['연령대_10_매출_금액','연령대_20_매출_금액','연령대_30_매출_금액','연령대_40_매출_금액','연령대_50_매출_금액','연령대_60_이상_매출_금액'],
      ['10대','20대','30대','40대','50대','60대+'],
      `[연령대별] 평균 매출 금액 (${filterTitle})`, 'lightseagreen'
    );
  }
  return { labels: [], datasets: [] };
}

export default function AnalysisSalesSection({ data }) {
  const [analysisSelection, setAnalysisSelection] = useState('요일별 매출 금액');
  const {
    selectedQuarter, setSelectedQuarter,
    selectedDistrictCode, setSelectedDistrictCode,
    selectedDongCode, setSelectedDongCode,
    selectedDistrict, setSelectedDistrict,
    selectedIndustry, setSelectedIndustry,
    quarterOptions, districtCodeOptions, dongCodeOptions, districtOptions, industryOptions,
    filteredData, isFilterComplete,
  } = useAnalysisFilters(data);

  const filterTitle = `${selectedQuarter} | ${selectedDistrictCode} | ${selectedDongCode} | ${selectedDistrict} | ${selectedIndustry}`;

  return (
    <div>
      <h2>📊 매출 분석</h2>
      <Select placeholder="기준 년-분기를 선택하세요" options={quarterOptions} onChange={opt => setSelectedQuarter(opt.value)} />
      <Select placeholder="자치구를 선택하세요" options={districtCodeOptions} onChange={opt => setSelectedDistrictCode(opt.value)} isDisabled={!selectedQuarter} />
      <Select placeholder="행정동을 선택하세요" options={dongCodeOptions} onChange={opt => setSelectedDongCode(opt.value)} isDisabled={!selectedDistrictCode} />
      <Select placeholder="상권을 선택하세요" options={districtOptions} onChange={opt => setSelectedDistrict(opt.value)} isDisabled={!selectedDongCode} />
      <Select placeholder="서비스 업종을 선택하세요" options={industryOptions} onChange={opt => setSelectedIndustry(opt.value)} isDisabled={!selectedDistrict} />
      <Select placeholder="시각화 항목을 선택하세요" options={VISUALIZATION_OPTIONS} onChange={opt => setAnalysisSelection(opt.value)} />
      {isFilterComplete && (
        <Bar data={getChartData(filteredData, analysisSelection, filterTitle)} options={barChartOptions} />
      )}
    </div>
  );
}

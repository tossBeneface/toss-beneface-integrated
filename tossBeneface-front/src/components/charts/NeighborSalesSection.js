import React, { useState } from 'react';
import Select from 'react-select';
import { Line } from 'react-chartjs-2';
import parseNumber from '../../utils/parseNumber';

const VISUALIZATION_OPTIONS = [
  { value: '요일별 매출 금액',   label: '요일별 매출 금액' },
  { value: '시간대별 매출 금액', label: '시간대별 매출 금액' },
  { value: '성별 매출 금액',     label: '성별 매출 금액' },
  { value: '연령대별 매출 금액', label: '연령대별 매출 금액' },
];

const lineChartOptions = {
  responsive: true,
  scales: { y: { beginAtZero: true } },
  elements: { point: { radius: 5 } },
};

function buildNeighborDataset(matchedData, dummyRow, columns, labels, realColor, dummyColor) {
  const realValues  = columns.map(col => {
    const sum = matchedData.reduce((acc, cur) => acc + parseNumber(cur[col]), 0);
    return matchedData.length > 0 ? sum / matchedData.length : 0;
  });
  const dummyValues = columns.map(col => parseNumber(dummyRow[col]));
  return {
    labels,
    datasets: [
      { label: '실제 데이터 (평균)', data: realValues,  borderColor: realColor,  backgroundColor: `${realColor}1A` },
      { label: '더미 데이터',        data: dummyValues, borderColor: dummyColor, backgroundColor: `${dummyColor}1A`, borderDash: [5, 5] },
    ],
  };
}

function getChartData(matchedData, dummyRow, selection) {
  if (!dummyRow) return { labels: [], datasets: [] };

  if (selection === '요일별 매출 금액') {
    return buildNeighborDataset(matchedData, dummyRow,
      ['월요일_매출_금액','화요일_매출_금액','수요일_매출_금액','목요일_매출_금액','금요일_매출_금액','토요일_매출_금액','일요일_매출_금액'],
      ['월','화','수','목','금','토','일'], 'blue', 'orange');
  }
  if (selection === '시간대별 매출 금액') {
    return buildNeighborDataset(matchedData, dummyRow,
      ['시간대_00~06_매출_금액','시간대_06~11_매출_금액','시간대_11~14_매출_금액','시간대_14~17_매출_금액','시간대_17~21_매출_금액','시간대_21~24_매출_금액'],
      ['00~06','06~11','11~14','14~17','17~21','21~24'], 'green', 'red');
  }
  if (selection === '성별 매출 금액') {
    return buildNeighborDataset(matchedData, dummyRow,
      ['남성_매출_금액','여성_매출_금액'], ['남성','여성'], 'blue', 'orange');
  }
  if (selection === '연령대별 매출 금액') {
    return buildNeighborDataset(matchedData, dummyRow,
      ['연령대_10_매출_금액','연령대_20_매출_금액','연령대_30_매출_금액','연령대_40_매출_금액','연령대_50_매출_금액','연령대_60_이상_매출_금액'],
      ['10대','20대','30대','40대','50대','60대+'], 'green', 'red');
  }
  return { labels: [], datasets: [] };
}

export default function NeighborSalesSection({ data, dummyData }) {
  const [neighborSelection, setNeighborSelection] = useState('요일별 매출 금액');
  const dummyRow = dummyData[0];

  const dummyQuarterOptions = Array.from(new Set(dummyData.map(d => d.기준_년분기_코드)))
    .map(v => ({ value: v, label: v }));

  const matchedData = dummyRow
    ? data.filter(i =>
        i.자치구_코드_명       === dummyRow['자치구_코드_명'] &&
        i.행정동_코드_명       === dummyRow['행정동_코드_명'] &&
        i.상권_코드_명         === dummyRow['상권_코드_명'] &&
        i.서비스_업종_코드_명  === dummyRow['서비스_업종_코드_명']
      )
    : [];

  return (
    <div>
      <h2>🏠 내 가게 주변상권 분석</h2>
      <Select placeholder="기준 년-분기를 선택하세요" options={dummyQuarterOptions} onChange={() => {}} />

      {dummyRow && (
        <>
          <p><strong>자치구명</strong>: {dummyRow['자치구_코드_명']}</p>
          <p><strong>행정동명</strong>: {dummyRow['행정동_코드_명']}</p>
          <p><strong>상권코드명</strong>: {dummyRow['상권_코드_명']}</p>
          <p><strong>서비스업종</strong>: {dummyRow['서비스_업종_코드_명']}</p>
        </>
      )}

      <Select
        placeholder="시각화 항목을 선택하세요"
        options={VISUALIZATION_OPTIONS}
        defaultValue={VISUALIZATION_OPTIONS[0]}
        onChange={opt => setNeighborSelection(opt.value)}
      />

      {dummyRow && (
        <Line data={getChartData(matchedData, dummyRow, neighborSelection)} options={lineChartOptions} />
      )}
    </div>
  );
}

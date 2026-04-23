import React, { useState } from 'react';
import Select from 'react-select';
import useCsvData from '../hooks/useCsvData';
import AnalysisSalesSection from '../components/charts/AnalysisSalesSection';
import NeighborSalesSection from '../components/charts/NeighborSalesSection';

const PAGE_OPTIONS = [
  { value: '매출 분석',            label: '매출 분석' },
  { value: '내 가게 주변상권 분석', label: '내 가게 주변상권 분석' },
];

const MarketAnalysisPage = () => {
  const [page, setPage] = useState('매출 분석');
  const { data, dummyData } = useCsvData();

  return (
    <div style={{ padding: '20px' }}>
      <h1>Streamlit 시각화 ㅡ React 시각화 데모</h1>

      <div style={{ marginBottom: 20 }}>
        <Select
          options={PAGE_OPTIONS}
          defaultValue={PAGE_OPTIONS[0]}
          onChange={opt => setPage(opt.value)}
        />
      </div>

      {page === '매출 분석' && <AnalysisSalesSection data={data} />}
      {page === '내 가게 주변상권 분석' && <NeighborSalesSection data={data} dummyData={dummyData} />}
    </div>
  );
};

export default MarketAnalysisPage;

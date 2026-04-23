import { useState } from 'react';

const toOptions = (set) => Array.from(set).map(v => ({ value: v, label: v }));

export default function useAnalysisFilters(data) {
  const [selectedQuarter, setSelectedQuarter]           = useState(null);
  const [selectedDistrictCode, setSelectedDistrictCode] = useState(null);
  const [selectedDongCode, setSelectedDongCode]         = useState(null);
  const [selectedDistrict, setSelectedDistrict]         = useState(null);
  const [selectedIndustry, setSelectedIndustry]         = useState(null);

  const quarterOptions       = toOptions(new Set(data.map(i => i.기준_년분기_코드)));
  const districtCodeOptions  = toOptions(new Set(data.map(i => i.자치구_코드_명)));

  const dongCodeOptions = selectedDistrictCode
    ? toOptions(new Set(data.filter(i => i.자치구_코드_명 === selectedDistrictCode).map(i => i.행정동_코드_명)))
    : [];

  const districtOptions = selectedDongCode
    ? toOptions(new Set(data.filter(i => i.행정동_코드_명 === selectedDongCode).map(i => i.상권_코드_명)))
    : [];

  const industryOptions = selectedDistrict
    ? toOptions(new Set(data.filter(i => i.상권_코드_명 === selectedDistrict).map(i => i.서비스_업종_코드_명)))
    : [];

  const filteredData = data.filter(i =>
    i.기준_년분기_코드 === selectedQuarter &&
    i.자치구_코드_명   === selectedDistrictCode &&
    i.행정동_코드_명   === selectedDongCode &&
    i.상권_코드_명     === selectedDistrict &&
    i.서비스_업종_코드_명 === selectedIndustry
  );

  const isFilterComplete = selectedQuarter && selectedDistrictCode && selectedDongCode && selectedDistrict && selectedIndustry;

  return {
    selectedQuarter, setSelectedQuarter,
    selectedDistrictCode, setSelectedDistrictCode,
    selectedDongCode, setSelectedDongCode,
    selectedDistrict, setSelectedDistrict,
    selectedIndustry, setSelectedIndustry,
    quarterOptions, districtCodeOptions, dongCodeOptions, districtOptions, industryOptions,
    filteredData, isFilterComplete,
  };
}

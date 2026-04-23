import { useState, useEffect } from 'react';
import Papa from 'papaparse';
import axios from 'axios';

export default function useCsvData() {
  const [data, setData] = useState([]);
  const [dummyData, setDummyData] = useState([]);

  useEffect(() => {
    axios.get('/Alldata_final3.csv', { responseType: 'blob' })
      .then(response => Papa.parse(response.data, {
        header: true,
        encoding: 'UTF-8',
        complete: results => setData(results.data),
        error: err => console.error('[PapaParse Error: main data]', err),
      }))
      .catch(error => console.error('Error loading /Alldata_final3.csv:', error));

    axios.get('/더미데이터_예측값.csv', { responseType: 'blob' })
      .then(response => Papa.parse(response.data, {
        header: true,
        encoding: 'UTF-8',
        complete: results => setDummyData(results.data),
        error: err => console.error('[PapaParse Error: dummy data]', err),
      }))
      .catch(error => console.error('Error loading /더미데이터_예측값.csv:', error));
  }, []);

  return { data, dummyData };
}

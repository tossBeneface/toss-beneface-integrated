import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { sectionStyle } from './chartStyles';

export default function MonthlySalesChart({ merchantData, areaData }) {
  const mData = merchantData.monthly.map(item => ({ month: item.month, merchant: item.total_sales }));
  const aData = areaData.monthly.map(item => ({
    month: item.month || item.quarter_year_code,
    area: Number(item.total_sales),
  }));

  const mergedObj = {};
  mData.forEach(d => { mergedObj[d.month] = { month: d.month, merchant: d.merchant, area: 0 }; });
  aData.forEach(d => {
    if (mergedObj[d.month]) {
      mergedObj[d.month].area = d.area;
    } else {
      mergedObj[d.month] = { month: d.month, merchant: 0, area: d.area };
    }
  });

  const chartData = Object.values(mergedObj).sort((a, b) => String(a.month).localeCompare(String(b.month)));

  return (
    <div style={sectionStyle}>
      <ResponsiveContainer width="100%" height={400}>
        <LineChart data={chartData} margin={{ top: 20, right: 30, left: 40, bottom: 20 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" />
          <YAxis tickMargin={10} />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="merchant" stroke="#ff7300" name="가맹점" />
          <Line type="monotone" dataKey="area" stroke="#387908" name="상권" strokeDasharray="3 3" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

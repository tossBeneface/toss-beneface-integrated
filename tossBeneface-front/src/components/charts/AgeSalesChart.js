import React from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import { COLORS, sectionStyle, titleStyle } from './chartStyles';

export default function AgeSalesChart({ merchantData, areaData }) {
  const merchantPieData = Object.entries(merchantData.age).map(([key, val]) => ({
    name: key.replace("sales_", ""),
    value: val,
  }));
  const areaPieData = Object.entries(areaData.age).map(([key, val]) => ({
    name: key.replace("sales_", ""),
    value: val,
  }));

  return (
    <div style={{ display: "flex", flexDirection: "row", gap: "60px", alignItems: "center", width: "100%" }}>
      <div style={sectionStyle}>
        <h3 style={titleStyle}>가맹점 연령대별 매출</h3>
        <ResponsiveContainer width="100%" height={400}>
          <PieChart>
            <Pie data={merchantPieData} dataKey="value" nameKey="name" outerRadius={120}
              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(2)}%`} labelLine={false}>
              {merchantPieData.map((_, index) => (
                <Cell key={`mAgeCell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(value) => value.toLocaleString()} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div style={sectionStyle}>
        <h3 style={titleStyle}>상권 연령대별 매출</h3>
        <ResponsiveContainer width="100%" height={400}>
          <PieChart>
            <Pie data={areaPieData} dataKey="value" nameKey="name" outerRadius={120}
              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(2)}%`} labelLine={false}>
              {areaPieData.map((_, index) => (
                <Cell key={`aAgeCell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(value) => value.toLocaleString()} />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

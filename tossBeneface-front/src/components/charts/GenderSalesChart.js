import React from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import { COLORS, sectionStyle, titleStyle } from './chartStyles';

export default function GenderSalesChart({ merchantData, areaData }) {
  const merchantPieData = [
    { name: "Male", value: merchantData.gender.male_sales },
    { name: "Female", value: merchantData.gender.female_sales },
  ];
  const areaPieData = [
    { name: "Male", value: areaData.gender.male_sales },
    { name: "Female", value: areaData.gender.female_sales },
  ];

  return (
    <div style={{ display: "flex", flexDirection: "row", gap: "60px", alignItems: "center", width: "100%" }}>
      <div style={sectionStyle}>
        <h3 style={titleStyle}>가맹점 성별 매출</h3>
        <ResponsiveContainer width="100%" height={350}>
          <PieChart>
            <Pie data={merchantPieData} dataKey="value" nameKey="name" outerRadius={120}
              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(2)}%`} labelLine={false}>
              {merchantPieData.map((_, index) => (
                <Cell key={`mCell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(value) => value.toLocaleString()} />
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div style={sectionStyle}>
        <h3 style={titleStyle}>상권 성별 매출</h3>
        <ResponsiveContainer width="100%" height={350}>
          <PieChart>
            <Pie data={areaPieData} dataKey="value" nameKey="name" outerRadius={120}
              label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(2)}%`} labelLine={false}>
              {areaPieData.map((_, index) => (
                <Cell key={`aCell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(value) => value.toLocaleString()} />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

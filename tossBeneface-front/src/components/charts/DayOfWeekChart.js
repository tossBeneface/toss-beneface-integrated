import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { sectionStyle } from './chartStyles';

const DAY_ORDER = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];

export default function DayOfWeekChart({ merchantData, areaData }) {
  const chartData = DAY_ORDER.map(day => {
    const mItem = merchantData.dayOfWeek.find(item => item.day_of_week === day) || { total_sales: 0 };
    const aItem = areaData.dayOfWeek.find(item => item.day_of_week === day) || { total_sales: 0 };
    return { day_of_week: day, merchant: mItem.total_sales, area: aItem.total_sales };
  });

  return (
    <div style={sectionStyle}>
      <ResponsiveContainer width="100%" height={400}>
        <BarChart data={chartData} margin={{ top: 20, right: 30, left: 40, bottom: 20 }}
          barCategoryGap="20%" barGap={5}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="day_of_week" />
          <YAxis tickMargin={10} />
          <Tooltip />
          <Legend />
          <Bar dataKey="merchant" fill="#82ca9d" name="가맹점" />
          <Bar dataKey="area" fill="#8884d8" name="상권" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

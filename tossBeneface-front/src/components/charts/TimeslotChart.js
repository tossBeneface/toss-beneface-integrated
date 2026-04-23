import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { sectionStyle } from './chartStyles';

const TIMESLOT_ORDER = ["sales_00_06", "sales_06_11", "sales_11_14", "sales_14_17", "sales_17_21", "sales_21_24"];

export default function TimeslotChart({ merchantData, areaData }) {
  const chartData = TIMESLOT_ORDER.map(slot => ({
    timeslot: slot,
    merchant: merchantData.timeslot[slot] || 0,
    area: areaData.timeslot[slot] || 0,
  }));

  return (
    <div style={sectionStyle}>
      <ResponsiveContainer width="100%" height={400}>
        <BarChart data={chartData} margin={{ top: 20, right: 30, left: 40, bottom: 20 }}
          barCategoryGap="20%" barGap={5}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="timeslot" />
          <YAxis tickMargin={10} />
          <Tooltip />
          <Legend />
          <Bar dataKey="merchant" fill="#ffa07a" name="가맹점" />
          <Bar dataKey="area" fill="#20b2aa" name="상권" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

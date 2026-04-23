import React from 'react';

export default function CircularProgress({ value }) {
  const radius = 80;
  const circumference = 2 * Math.PI * radius;
  const progress = Math.max(0, Math.min(100, value));
  const offset = circumference - (progress / 100) * circumference;

  return (
    <svg width={200} height={200}>
      <circle cx="100" cy="100" r={radius} fill="none" stroke="#ddd" strokeWidth="10" />
      <circle
        cx="100" cy="100" r={radius}
        fill="none" stroke="#007bff" strokeWidth="10"
        strokeDasharray={circumference} strokeDashoffset={offset} strokeLinecap="round"
      />
      <text x="100" y="110" textAnchor="middle" fill="#000" fontSize="28" fontWeight="bold">
        {progress}%
      </text>
    </svg>
  );
}

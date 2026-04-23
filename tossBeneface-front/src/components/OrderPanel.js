import React from 'react';

export default function OrderPanel({ selectedItems, totalAmount, onCancel }) {
  return (
    <div style={{
      width: '300px', borderLeft: '1px solid #ddd', padding: '20px',
      backgroundColor: '#fefefe', overflowY: 'auto',
    }}>
      <h2 style={{ borderBottom: '2px solid #007bff', paddingBottom: '10px', marginBottom: '20px' }}>
        주문내역
      </h2>

      {selectedItems.length === 0 ? (
        <p style={{ textAlign: 'center', color: '#777' }}>메뉴를 선택해주세요.</p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {selectedItems.map((order, index) => (
            <li key={index} style={{
              marginBottom: '15px', padding: '10px', border: '1px solid #ddd',
              borderRadius: '8px', backgroundColor: '#fff', boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
              display: 'flex', flexDirection: 'column',
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span style={{ fontWeight: 'bold' }}>{order.name}</span>
                <button
                  onClick={() => onCancel(order.name)}
                  style={{ background: 'none', border: 'none', color: '#ff4d4d', fontWeight: 'bold', cursor: 'pointer' }}
                >
                  취소
                </button>
              </div>
              <div style={{ fontSize: '14px', color: '#555' }}>
                {order.count}개 &nbsp;|&nbsp; {order.price * order.count}원
              </div>
            </li>
          ))}
        </ul>
      )}

      <hr style={{ margin: '20px 0' }} />
      <h2 style={{ textAlign: 'center', color: '#333' }}>총 금액</h2>
      <p style={{ fontSize: '28px', fontWeight: 'bold', textAlign: 'center', color: '#007bff' }}>
        {totalAmount}원
      </p>
    </div>
  );
}

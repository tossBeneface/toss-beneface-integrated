import React from 'react';

export default function MenuCard({ item, onClick }) {
  const isOutOfStock = item.stock === 0;

  return (
    <div
      onClick={() => { if (!isOutOfStock) onClick(item); }}
      style={{
        backgroundColor: '#fff',
        borderRadius: '8px',
        boxShadow: '0 1px 5px rgba(0,0,0,0.1)',
        overflow: 'hidden',
        textAlign: 'center',
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        opacity: isOutOfStock ? 0.5 : 1,
        pointerEvents: isOutOfStock ? 'none' : 'auto',
        cursor: 'pointer',
      }}
    >
      <img src={item.img} alt={item.name} style={{ width: '100%', height: '150px', objectFit: 'cover' }} />
      {item.badge && (
        <div style={{
          position: 'absolute', top: '10px', left: '10px',
          padding: '5px 10px', fontSize: '12px', fontWeight: 'bold', color: '#fff',
          borderRadius: '12px',
          backgroundColor: item.badge === 'popular' ? '#007bff' : '#ff4d4d',
        }}>
          {item.badge === 'popular' ? '인기' : '신규'}
        </div>
      )}
      <div style={{ padding: '10px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ fontSize: '14px', fontWeight: 'bold', marginBottom: '5px', wordBreak: 'break-word' }}>
          {item.name}
        </div>
        <div style={{ fontSize: '12px', color: '#555' }}>{item.price}</div>
        {isOutOfStock && <div style={{ color: 'red', fontSize: '12px', marginTop: '5px' }}>품절</div>}
      </div>
    </div>
  );
}

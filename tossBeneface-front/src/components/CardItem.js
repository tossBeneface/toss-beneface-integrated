import React from 'react';
import { FaStar } from 'react-icons/fa';

const baseStyle = {
  position: 'relative',
  backgroundColor: '#ffffff',
  borderRadius: '10px',
  overflow: 'visible',
  width: '300px',
  minWidth: '300px',
  display: 'flex',
  flexDirection: 'column',
  transition: 'transform 0.5s, box-shadow 0.2s, border 0.2s, filter 0.2s, opacity 0.2s',
  cursor: 'pointer',
  scrollSnapAlign: 'center',
  transformStyle: 'preserve-3d',
};

export default function CardItem({ card, onSelect, distance, relativeIndex, isAutoSelected, isClicked }) {
  const style = { ...baseStyle };

  if (isAutoSelected) {
    style.transformOrigin = 'top center';
    style.transform = 'scale(1.1)';
    style.boxShadow = '0 10px 20px rgba(0, 0, 0, 0.3)';
    style.zIndex = 1000;
  } else {
    const scaleFactor = Math.max(0.7, 1 - 0.1 * distance);
    style.transform = `scale(${scaleFactor}) rotateY(${relativeIndex * 5}deg)`;
    style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.1)';
  }
  style.border = isClicked ? '3px solid #0064FF' : '3px solid transparent';
  style.filter = 'none';
  style.opacity = (isAutoSelected || isClicked) ? 1 : 0.6;

  const benefitsText = Array.isArray(card.benefits)
    ? card.benefits.join('\n')
    : typeof card.benefits === 'string' ? card.benefits : '';

  return (
    <div style={style} onClick={() => onSelect(card)}>
      <div style={{ position: 'absolute', top: '10px', left: '10px', backgroundColor: '#0064FF', color: '#ffffff', padding: '8px 12px', borderRadius: '5px', fontSize: '18px', fontWeight: 'bold', zIndex: 1 }}>
        <FaStar style={{ marginRight: '4px' }} />
        {card.computedRank}
      </div>
      <div style={{ width: '100%', height: '400px', backgroundSize: 'cover', backgroundPosition: 'center', backgroundImage: `url(${card.image})` }} />
      <div style={{ padding: '15px', flexGrow: 1 }}>
        <h2 style={{ fontSize: '20px', marginBottom: '8px', color: '#202632' }}>{card.title}</h2>
        <p style={{ fontSize: '14px', lineHeight: 1.4, color: '#202632' }}>
          {benefitsText.split('\n').map((line, i) => <span key={i}>{line}<br /></span>)}
        </p>
      </div>
    </div>
  );
}

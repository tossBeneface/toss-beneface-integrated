import React, { useEffect, useState, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import useCardRecommendation from '../hooks/useCardRecommendation';
import CardItem from '../components/CardItem';

const CardRecommendation = () => {
  const { state } = useLocation();
  const memberId = state?.memberId;
  const sortedCards = useCardRecommendation(memberId);

  const copyCount = sortedCards.length;
  const extendedCards = [...sortedCards, ...sortedCards, ...sortedCards];

  const [autoSelectedCard, setAutoSelectedCard] = useState(null);
  const [clickedCard, setClickedCard] = useState(null);
  const scrollTimeoutRef = useRef(null);
  const cardListWrapperRef = useRef(null);

  useEffect(() => {
    if (cardListWrapperRef.current && copyCount > 0) {
      const wrapper = cardListWrapperRef.current;
      const desiredCardCenter = 20 + copyCount * 320 + 150;
      wrapper.scrollLeft = desiredCardCenter - wrapper.clientWidth / 2;
    }
  }, [copyCount]);

  useEffect(() => {
    if (sortedCards.length > 0 && !autoSelectedCard) {
      setAutoSelectedCard(sortedCards[0]);
    }
  }, [sortedCards, autoSelectedCard]);

  const handleSelect = (card) => {
    setClickedCard(card);
    setAutoSelectedCard(card);
    if (cardListWrapperRef.current) {
      const wrapper = cardListWrapperRef.current;
      const originalIndex = sortedCards.findIndex(c => c.id === card.id);
      const desiredCardCenter = 20 + (copyCount + originalIndex) * 320 + 150;
      wrapper.scrollLeft = desiredCardCenter - wrapper.clientWidth / 2;
    }
  };

  const handleScroll = () => {
    if (scrollTimeoutRef.current) clearTimeout(scrollTimeoutRef.current);
    scrollTimeoutRef.current = setTimeout(() => {
      if (!cardListWrapperRef.current) return;
      const wrapper = cardListWrapperRef.current;
      let scrollLeft = wrapper.scrollLeft;
      const totalScrollWidth = copyCount * 320;
      if (scrollLeft < totalScrollWidth * 0.5) {
        scrollLeft += totalScrollWidth;
        wrapper.scrollLeft = scrollLeft;
      } else if (scrollLeft > totalScrollWidth * 1.5) {
        scrollLeft -= totalScrollWidth;
        wrapper.scrollLeft = scrollLeft;
      }
      const approximateIndex = Math.floor((scrollLeft + wrapper.clientWidth / 2 - 170) / 320);
      setAutoSelectedCard(sortedCards[approximateIndex % copyCount]);
    }, 200);
  };

  if (sortedCards.length === 0) {
    return <div style={{ textAlign: 'center', padding: '50px' }}>Loading...</div>;
  }

  return (
    <div style={{ overflow: 'visible', padding: '20px' }}>
      <style>{`.card-list-wrapper::-webkit-scrollbar { display: none; }`}</style>
      <div style={{ maxWidth: '1080px', margin: '0 auto', padding: '20px', backgroundColor: '#ffffff', color: '#202632', fontFamily: "'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif", display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
        <div style={{ textAlign: 'center', marginBottom: '30px' }}>
          {clickedCard ? (
            <button
              style={{ padding: '10px 20px', fontSize: '18px', backgroundColor: '#0064FF', color: '#fff', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
              onClick={() => console.log('선택한 카드로 결제 진행:', clickedCard)}
            >
              선택한 카드로 결제하기
            </button>
          ) : (
            <p style={{ fontSize: '20px', fontWeight: 'bold', color: '#202632' }}>
              보유중인 카드들 중 현 매장에서 쓸 수 있는 순위를 매겼어요!
            </p>
          )}
        </div>
        <div
          style={{ width: '940px', overflowX: 'auto', overflowY: 'visible', scrollBehavior: 'smooth', margin: '0 auto', padding: '0 20px', display: 'flex', justifyContent: 'flex-start', msOverflowStyle: 'none', scrollbarWidth: 'none', WebkitOverflowScrolling: 'touch', scrollSnapType: 'x mandatory', perspective: '1000px' }}
          className="card-list-wrapper"
          onScroll={handleScroll}
          ref={cardListWrapperRef}
        >
          <div style={{ display: 'inline-flex', flexWrap: 'nowrap', gap: '20px', paddingBottom: '20px', justifyContent: 'center' }}>
            {extendedCards.map((card, index) => {
              const autoIndex = autoSelectedCard ? sortedCards.findIndex(c => c.id === autoSelectedCard.id) : 0;
              const relativeIndex = (index % copyCount) - autoIndex;
              return (
                <CardItem
                  key={`${index}-${card.id}`}
                  card={card}
                  onSelect={handleSelect}
                  distance={Math.abs(relativeIndex)}
                  relativeIndex={relativeIndex}
                  isAutoSelected={autoSelectedCard && card.id === autoSelectedCard.id}
                  isClicked={clickedCard && card.id === clickedCard.id}
                />
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CardRecommendation;

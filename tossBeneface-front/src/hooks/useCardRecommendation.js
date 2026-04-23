import { useState, useEffect } from 'react';

const callPredictAPI = async (modelData, index, setPredictions) => {
  try {
    const response = await fetch(
      'https://benefacefastapi20-frgtcya5bnefbdfs.koreacentral-01.azurewebsites.net/fastapi/predict',
      { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(modelData) }
    );
    if (!response.ok) throw new Error('예측 API 호출에 실패했습니다.');
    const data = await response.json();
    setPredictions(prev => {
      const next = [...prev];
      next[index] = data;
      return next;
    });
  } catch (error) {
    console.error('예측 API 호출 중 에러:', error);
  }
};

export default function useCardRecommendation(memberId) {
  const [recommendedCards, setRecommendedCards] = useState([]);
  const [predictions, setPredictions] = useState([]);

  useEffect(() => {
    if (!memberId) return;
    fetch(`${process.env.REACT_APP_API_BASE_URL}/user-data-test/financial-data?memberId=${memberId}`)
      .then(res => {
        if (!res.ok) throw new Error('재무 데이터 조회에 실패했습니다.');
        return res.json();
      })
      .then(financialData => {
        const benefitPromises = financialData.map(async (item, index) => {
          const params = new URLSearchParams({ carcompany: item.card_company, card_name: item.card_name });
          const res = await fetch(`${process.env.REACT_APP_API_BASE_URL}/card-benefits/get_details?${params.toString()}`);
          if (!res.ok) throw new Error('카드 혜택 상세 조회에 실패했습니다.');
          const benefitDataArray = await res.json();
          const benefitData = benefitDataArray[0];
          const model_data = {
            전월실적: item.last_per, 결제금액: item.pay_amount,
            혜택받은횟수: item.monthly_split, 이번달실적: item.now_per,
            혜택받은금액: item.Accrue_benefit, Benefit: benefitData.Benefit,
            limit_once: benefitData.limit_once, limit_month: benefitData.limit_month,
            min_pay: benefitData.min_pay, min_per: benefitData.min_per, monthly: benefitData.monthly,
          };
          callPredictAPI(model_data, index, setPredictions);
          return { ...item, ...benefitData, model_data };
        });
        Promise.all(benefitPromises)
          .then(setRecommendedCards)
          .catch(err => console.error('카드 혜택 조회 중 에러:', err));
      })
      .catch(err => console.error('재무 데이터 조회 중 에러:', err));
  }, [memberId]);

  const dynamicCards = recommendedCards.map((card, index) => ({
    id: index,
    title: card.card_name,
    benefits: typeof card.Benefit === 'string' ? card.Benefit : Array.isArray(card.Benefit) ? card.Benefit.join('\n') : '',
    image: card.card_image || `https://via.placeholder.com/300x400?text=${encodeURIComponent(card.card_name)}`,
    prediction: predictions[index] ? predictions[index].prediction[0] : null,
    computedScore: predictions[index] ? 6 - predictions[index].prediction[0] : 6,
  }));

  const sortedCards = [...dynamicCards].sort((a, b) => a.computedScore - b.computedScore);
  sortedCards.forEach((card, idx) => { card.computedRank = idx + 1; });

  return sortedCards;
}

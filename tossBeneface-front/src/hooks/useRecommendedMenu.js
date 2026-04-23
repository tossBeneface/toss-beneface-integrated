import { useState, useEffect } from 'react';

export default function useRecommendedMenu() {
  const [recommendedMenu, setRecommendedMenu] = useState([]);

  useEffect(() => {
    const matchedBrand = sessionStorage.getItem('matchedBrand');
    fetch(`${process.env.REACT_APP_API_BASE_URL}/products/getmenu?brand=${encodeURIComponent(matchedBrand)}`)
      .then(res => {
        if (!res.ok) throw new Error('네트워크 응답이 정상적이지 않습니다.');
        return res.json();
      })
      .then(data => setRecommendedMenu(data.menus || []))
      .catch(err => console.error('메뉴 데이터를 가져오는 중 오류 발생:', err));
  }, []);

  const menuCards = recommendedMenu.map(item => ({
    img: item.img,
    name: item.menu,
    price: item.price ? `${item.price}원` : '',
    stock: item.stock ?? 0,
    badge: '',
  }));

  return menuCards;
}

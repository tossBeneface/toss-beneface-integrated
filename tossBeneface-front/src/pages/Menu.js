import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { STATIC_MENU_ITEMS, MENU_TABS } from '../constants/menuData';
import useRecommendedMenu from '../hooks/useRecommendedMenu';
import useOrderCart from '../hooks/useOrderCart';
import MenuCard from '../components/MenuCard';
import OrderPanel from '../components/OrderPanel';

const tabButtonStyle = {
  flex: 1, padding: '10px', fontSize: '14px', color: '#333',
  border: 'none', background: 'none', cursor: 'pointer', borderBottom: '2px solid transparent',
};

const StarbucksMenuPage = () => {
  const [currentTab, setCurrentTab] = useState('추천메뉴');
  const [isToggleOn, setIsToggleOn] = useState(false);
  const [orderMenuClicked, setOrderMenuClicked] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const recommendedMenuCards = useRecommendedMenu();
  const { selectedItems, addItem, removeItem, totalAmount } = useOrderCart();

  const displayItems = currentTab === '추천메뉴' ? recommendedMenuCards : (STATIC_MENU_ITEMS[currentTab] || []);

  // location.state로 전달된 메뉴를 자동 주문 추가
  useEffect(() => {
    if (!location.state?.menu || orderMenuClicked) return;
    const incomingMenu = location.state.menu.toLowerCase();

    let itemFound = recommendedMenuCards.find(i => i.name.toLowerCase() === incomingMenu) || null;
    if (!itemFound) {
      for (const arr of Object.values(STATIC_MENU_ITEMS)) {
        itemFound = arr.find(i => i.name.toLowerCase() === incomingMenu) || null;
        if (itemFound) break;
      }
    }
    if (itemFound) {
      addItem(itemFound, 1);
      setOrderMenuClicked(true);
    }
  }, [location.state, recommendedMenuCards, orderMenuClicked, addItem]);

  const toggleMenu = () => {
    setIsToggleOn(prev => !prev);
    navigate('/OwnerPage');
  };

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'Arial, sans-serif' }}>
      {/* 왼쪽: 메뉴 패널 */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 20px', backgroundColor: '#fff', boxShadow: '0 1px 5px rgba(0,0,0,0.1)', alignItems: 'center' }}>
          <div onClick={toggleMenu} style={{ display: 'flex', alignItems: 'center', width: '50px', height: '25px', backgroundColor: isToggleOn ? '#4caf50' : '#ccc', borderRadius: '15px', position: 'relative', cursor: 'pointer', transition: 'background-color 0.3s' }}>
            <div style={{ width: '20px', height: '20px', backgroundColor: '#fff', borderRadius: '50%', position: 'absolute', top: '2.5px', left: isToggleOn ? '25px' : '2.5px', transition: 'left 0.3s', boxShadow: '0 2px 4px rgba(0,0,0,0.2)' }} />
          </div>
          <div style={{ fontSize: '16px', fontWeight: 'bold' }}>{currentTab}</div>
          <div style={{ fontSize: '20px', cursor: 'pointer' }}>⟳</div>
        </div>

        {/* 배지 영역 */}
        <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', backgroundColor: '#fff', borderBottom: '1px solid #ddd', padding: '10px 0' }}>
          <span style={{ fontSize: '14px', color: '#007bff', fontWeight: 'bold' }}>인기</span>
          <span style={{ fontSize: '14px', color: '#ff4d4d', fontWeight: 'bold' }}>신규</span>
        </div>

        {/* 탭 내비게이션 */}
        <div style={{ display: 'flex', justifyContent: 'space-around', backgroundColor: '#fff', borderBottom: '1px solid #ddd' }}>
          {MENU_TABS.map(tab => (
            <button key={tab} style={{ ...tabButtonStyle, borderBottom: currentTab === tab ? '2px solid #007bff' : '2px solid transparent' }} onClick={() => setCurrentTab(tab)}>
              {tab}
            </button>
          ))}
        </div>

        {/* 메뉴 카드 그리드 */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))', gap: '15px' }}>
            {displayItems.map((item, index) => (
              <MenuCard key={index} item={item} onClick={addItem} />
            ))}
          </div>
        </div>

        {/* Footer */}
        <div style={{ padding: '15px', backgroundColor: '#fff', borderTop: '1px solid #ddd', textAlign: 'center', display: 'flex', justifyContent: 'center', gap: '10px' }}>
          <button onClick={() => navigate('/faceRecognition')} style={{ padding: '15px 30px', fontSize: '16px', fontWeight: 'bold', color: '#fff', backgroundColor: '#007bff', border: 'none', borderRadius: '8px', cursor: 'pointer' }}>
            결제하기
          </button>
          <button onClick={() => navigate('/voiceorder')} style={{ padding: '15px 30px', fontSize: '16px', fontWeight: 'bold', color: '#fff', backgroundColor: '#28a745', border: 'none', borderRadius: '8px', cursor: 'pointer' }}>
            음성결제
          </button>
        </div>
      </div>

      {/* 오른쪽: 주문 내역 패널 */}
      <OrderPanel selectedItems={selectedItems} totalAmount={totalAmount} onCancel={removeItem} />
    </div>
  );
};

export default StarbucksMenuPage;

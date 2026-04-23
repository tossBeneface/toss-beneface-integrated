import { useState } from 'react';
import parsePrice from '../utils/parsePrice';

export default function useOrderCart() {
  const [selectedItems, setSelectedItems] = useState([]);

  const addItem = (item, quantity = null) => {
    let qty = quantity;
    if (qty === null) {
      const input = window.prompt(`"${item.name}" 몇 개를 주문하시겠습니까?`, '1');
      if (input === null) return;
      qty = parseInt(input, 10);
      if (isNaN(qty) || qty <= 0) {
        alert('유효한 주문 수량을 입력해주세요.');
        return;
      }
    }

    setSelectedItems(prev => {
      const existing = prev.find(o => o.name === item.name);
      if (existing) {
        return prev.map(o => o.name === item.name ? { ...o, count: o.count + qty } : o);
      }
      return [...prev, { name: item.name, price: parsePrice(item.price), count: qty }];
    });
  };

  const removeItem = (name) => {
    setSelectedItems(prev => prev.filter(o => o.name !== name));
  };

  const totalAmount = selectedItems.reduce((sum, o) => sum + o.price * o.count, 0);

  return { selectedItems, addItem, removeItem, totalAmount };
}

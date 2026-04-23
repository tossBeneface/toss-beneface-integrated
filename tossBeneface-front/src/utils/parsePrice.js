const parsePrice = (priceString) =>
  Number(priceString.replace(/,/g, '').replace(/원/g, '').trim());

export default parsePrice;

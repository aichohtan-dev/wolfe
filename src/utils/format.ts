export const money = (n: number) => `₹${n.toLocaleString('en-IN')}`;
export const inr = (paise: number) => `₹${(paise / 100).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

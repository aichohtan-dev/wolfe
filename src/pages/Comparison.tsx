import React from 'react';
import { Product, products } from '../data';
import { money } from '../utils/format';

interface ComparisonProps {
  items: typeof products;
  onAdd: (id: string) => void;
  wishes: string[];
  onWish: (id: string) => void;
  onCompare: (id: string) => void;
  compared: string[];
}

export default function Comparison({ items, onAdd, wishes, onWish, onCompare, compared }: ComparisonProps) {
  return (
    <main className="container-w section">
      <p className="eyebrow">Product comparison</p>
      <h1>Compare the details.</h1>
      <p>
        {items.length < 2
          ? 'Select at least two products from the shop to compare.'
          : `Comparing ${items.length} selected pieces.`}
      </p>
      <div className="product-grid">
        {items.map((p) => (
          <article className="admin-panel" key={p.id}>
            <img src={p.image} alt={p.name} style={{ width: '100%', aspectRatio: '1', objectFit: 'cover' }} />
            <h2>{p.name}</h2>
            <p>{money(p.price)}</p>
            <p>
              <strong>Finish</strong> · {p.finish}
            </p>
            <p>
              <strong>Material</strong> · {p.material}
            </p>
            <p>
              <strong>Colour</strong> · {p.color}
            </p>
            <p>
              <strong>Style</strong> · {p.style}
            </p>
            <button className="btn btn-orange" onClick={() => onAdd(p.id)}>
              Add to bag
            </button>
            <button className="btn btn-light" onClick={() => onWish(p.id)}>
              {wishes.includes(p.id) ? 'Saved' : 'Save'}
            </button>
            <button className="btn btn-light" onClick={() => onCompare(p.id)}>
              Remove comparison
            </button>
          </article>
        ))}
      </div>
    </main>
  );
}

import React, { useState, useEffect } from 'react';
import { api } from '../api';
import { useSeo } from '../hooks/useSeo';
import { money } from '../utils/format';

interface BundlesProps {
  onAddBundle: (b: any) => void;
}

export default function Bundles({ onAddBundle }: BundlesProps) {
  useSeo('Wolfe Bundles | Considered Combinations', 'Curated architectural hardware bundles with automated savings.');
  const [items, setItems] = useState<any[]>([]);

  useEffect(() => {
    api.bundles.list().then(setItems).catch(() => setItems([]));
  }, []);

  return (
    <main className="container-w section">
      <p className="eyebrow">Wolfe bundles</p>
      <h1>Considered combinations.</h1>
      <p className="detail-description">Curated pieces together, with bundle savings calculated server-side at checkout.</p>
      <div className="product-grid">
        {items.map((b: any) => (
          <article className="product-card" key={b.id}>
            <div className="product-card-image">
              <img src={b.items?.[0]?.imageUrl || '/wolfe-logo.png'} alt={b.name} loading="lazy" />
            </div>
            <div className="product-card-copy">
              <p className="eyebrow">Bundle</p>
              <h3>{b.name}</h3>
              <p>{b.description}</p>
              <div className="detail-price">
                {money(Number(b.total))}{' '}
                <small style={{ textDecoration: 'line-through', opacity: 0.55 }}>{money(Number(b.subtotal))}</small>
              </div>
              <p className="checkout-login-note">Save {money(Number(b.discount))}</p>
              <button className="btn btn-orange" onClick={() => onAddBundle(b)}>
                Add bundle to bag
              </button>
            </div>
          </article>
        ))}
      </div>
      {!items.length && <p className="empty-state">No active bundles yet.</p>}
    </main>
  );
}

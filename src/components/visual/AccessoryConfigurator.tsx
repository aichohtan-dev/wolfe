import React, { useState, useEffect } from 'react';
import { api, type Customer } from '../../api';

export interface AccessoryConfiguratorProps {
  slug: string;
  baseImage: string;
  user: Customer | null;
  productId: number;
  onAddConfigured: (id: string, token: string) => void;
}

export function AccessoryConfigurator({
  slug,
  baseImage,
  user,
  productId,
  onAddConfigured,
}: AccessoryConfiguratorProps) {
  const [items, setItems] = useState<any[]>([]);
  const [selected, setSelected] = useState<any | null>(null);
  const [saved, setSaved] = useState('');
  const [room, setRoom] = useState('light');

  useEffect(() => {
    api.accessories(slug).then(setItems).catch(() => setItems([]));
  }, [slug]);

  if (!items.length) return null;

  const save = async () => {
    try {
      const c = await api.experience.saveConfiguration({
        customerId: user?.id || null,
        productId,
        accessoryId: selected?.id || null,
        configJson: JSON.stringify({
          accessoryId: selected?.id || null,
          room,
          accessorySku: selected?.sku || null,
          accessoryName: selected?.name || null,
        }),
      });
      setSaved(`${location.origin}/config/${c.shareToken}`);
      return c;
    } catch {
      setSaved('Could not save configuration');
      return null;
    }
  };

  return (
    <section className="configurator">
      <div className="configurator-head">
        <div>
          <p className="eyebrow">Wolfe visual configurator</p>
          <h2>Build your look.</h2>
          <p>Try handles, knobs and accessories on the piece, then save or share the configuration.</p>
        </div>
        {selected && (
          <button className="btn btn-light" onClick={() => setSelected(null)}>
            Clear
          </button>
        )}
      </div>
      <div className={`configurator-stage room-${room}`}>
        <img src={baseImage} alt="Product configurator" />
        <div className="configurator-overlay" aria-live="polite">
          {selected && (
            <img
              src={selected.overlayUrl}
              alt={selected.name}
              style={{
                left: `${selected.x}%`,
                top: `${selected.y}%`,
                transform: `translate(-50%,-50%) scale(${selected.scale})`,
              }}
            />
          )}
        </div>
      </div>
      <div className="configurator-toolbar">
        <label>
          Room{' '}
          <select value={room} onChange={(e) => setRoom(e.target.value)} className="select-field">
            <option value="light">Light</option>
            <option value="warm">Warm</option>
            <option value="dark">Dark</option>
          </select>
        </label>
        <button
          className="btn btn-orange"
          onClick={async () => {
            const c = await save();
            if (c) onAddConfigured(slug, c.shareToken);
          }}
          disabled={!selected}
        >
          Add configured to bag
        </button>
      </div>
      {saved && <p className="success-message">{saved}</p>}
      <div className="configurator-options">
        {items.map((a) => (
          <button
            type="button"
            key={a.id}
            className={selected?.id === a.id ? 'selected' : ''}
            onClick={() => setSelected(a)}
          >
            <span className="swatch">
              <img src={a.overlayUrl} alt="" />
            </span>
            <strong>{a.name}</strong>
            <small>
              {a.type}
              {a.price != null ? ` · ₹${Number(a.price).toLocaleString('en-IN')}` : ''}
            </small>
          </button>
        ))}
      </div>
    </section>
  );
}

export default AccessoryConfigurator;

import React, { useState, useEffect } from 'react';
import { api, type Customer } from '../../api';

export interface AddressesPageProps {
  user: Customer | null;
}

export function AddressesPage({ user }: AddressesPageProps) {
  const [items, setItems] = useState<any[]>([]);
  const [form, setForm] = useState<any>({
    label: 'Home',
    recipientName: user?.name || '',
    phone: user?.phone || '',
    address: '',
    city: '',
    state: 'Rajasthan',
    pincode: '',
    isDefault: false
  });
  const [editing, setEditing] = useState<number | null>(null);
  const [msg, setMsg] = useState('');

  const load = () => user && api.addresses.get(user.id).then(setItems).catch(() => setItems([]));

  useEffect(() => {
    load();
  }, [user]);

  if (!user) {
    return (
      <main className="container-w narrow-page">
        <p className="empty-state">Sign in to manage addresses.</p>
      </main>
    );
  }

  const save = async (e: any) => {
    e.preventDefault();
    try {
      if (editing) {
        await api.addresses.update(user.id, editing, form);
      } else {
        await api.addresses.create(user.id, form);
      }
      setEditing(null);
      setForm({
        label: 'Home',
        recipientName: user.name,
        phone: user.phone || '',
        address: '',
        city: '',
        state: 'Rajasthan',
        pincode: '',
        isDefault: false
      });
      await load();
    } catch (err: any) {
      setMsg(err.message || 'Unable to save address');
    }
  };

  const edit = (a: any) => {
    setEditing(a.id);
    setForm({ ...a, isDefault: a.default });
    setMsg('');
  };

  return (
    <main className="container-w section">
      <p className="eyebrow">Customer account</p>
      <h1>Saved addresses</h1>
      <div className="account-grid">
        {items.map(a => (
          <div className="account-card" key={a.id}>
            <strong>{a.label}{a.default ? ' · Default' : ''}</strong>
            <p>
              {a.recipientName}<br />
              {a.address}<br />
              {a.city}, {a.state} — {a.pincode}<br />
              {a.phone}
            </p>
            <button className="text-link" onClick={() => edit(a)}>Edit</button>
            <button
              className="text-link"
              onClick={async () => {
                await api.addresses.remove(user.id, a.id);
                load();
              }}
            >
              Remove
            </button>
          </div>
        ))}
      </div>
      <form onSubmit={save} className="checkout-form" style={{ maxWidth: 720, marginTop: 32 }}>
        <h2>{editing ? 'Edit address' : 'Add address'}</h2>
        {[
          ['label', 'Label'],
          ['recipientName', 'Recipient name'],
          ['phone', 'Phone'],
          ['address', 'Address'],
          ['city', 'City'],
          ['state', 'State'],
          ['pincode', 'Pincode']
        ].map(([k, l]) => (
          <input
            key={k}
            required
            value={form[k] || ''}
            onChange={e => setForm({ ...form, [k]: e.target.value })}
            placeholder={l}
            className="field"
          />
        ))}
        <label className="admin-check">
          <input
            type="checkbox"
            checked={!!form.isDefault}
            onChange={e => setForm({ ...form, isDefault: e.target.checked })}
          />{' '}
          Make default
        </label>
        {msg && <p className="error-message">{msg}</p>}
        <button className="btn btn-orange">{editing ? 'Update address' : 'Save address'}</button>
      </form>
    </main>
  );
}

export default AddressesPage;

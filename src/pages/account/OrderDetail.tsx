import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api, type Customer } from '../../api';
import { money } from '../../utils/format';

export interface OrderDetailProps {
  user: Customer | null;
}

export function OrderDetail({ user }: OrderDetailProps) {
  const { id } = useParams();
  const [data, setData] = useState<any>(null);
  const [error, setError] = useState('');
  const [reason, setReason] = useState('');
  const [returnSent, setReturnSent] = useState(false);
  const [returnQty, setReturnQty] = useState<Record<number, number>>({});
  const [history, setHistory] = useState<any[]>([]);

  useEffect(() => {
    if (user && id) {
      api.orderDetail(id).then(setData).catch(e => setError(e.message));
      api.orderHistory(id).then(setHistory).catch(() => setHistory([]));
    }
  }, [user, id]);

  if (!user) {
    return (
      <main className="container-w narrow-page">
        <p className="empty-state">Sign in to view this order.</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="container-w narrow-page">
        <p className="error-message">{error}</p>
      </main>
    );
  }

  if (!data) {
    return (
      <main className="container-w section">
        <p>Loading order…</p>
      </main>
    );
  }

  const o = data.order;
  const orderItems = data.items || [];
  return (
    <main className="container-w section">
      <p className="eyebrow">Order detail</p>
      <h1>{o.id}</h1>
      <p>{new Date(o.createdAt).toLocaleString('en-IN')} · <strong>{o.status}</strong></p>
      <div className="order-card">
        <div>
          <h2>Items</h2>
          {data.items.map((i: any) => {
            let cfgLabel = '';
            if (i.configurationJson) {
              try {
                const c = JSON.parse(i.configurationJson);
                if (c.accessoryName) cfgLabel = ` · ${c.accessoryName}`;
                if (c.room) cfgLabel += ` · ${c.room} room`;
              } catch { }
            }
            return (
              <div className="summary-line" key={i.id}>
                <span>
                  {i.productName} × {i.quantity}
                  {i.configurationToken ? ` · configured${cfgLabel}` : ''}
                </span>
                <span>{money(Number(i.lineNetAmount ?? (i.unitPrice * i.quantity)) / 100)}</span>
              </div>
            );
          })}
          <div className="summary-total">
            <strong>Total</strong>
            <strong>{money(o.total / 100)}</strong>
          </div>
        </div>
        <div>
          <h2>Delivery</h2>
          <p>
            {o.customerName}<br />
            {o.phone}<br />
            {o.address}<br />
            {o.city} — {o.pincode}
          </p>
        </div>
      </div>
      <div className="order-card" style={{ marginTop: 20 }}>
        <div>
          <h2>Order history</h2>
          {history.length ? (
            history.map((h: any) => (
              <div className="summary-line" key={h.id}>
                <span>
                  <strong>{h.status}</strong>
                  {h.note && <small> · {h.note}</small>}
                </span>
                <span>{new Date(h.createdAt).toLocaleString('en-IN')}</span>
              </div>
            ))
          ) : (
            <p className="empty-state">History will appear as the order progresses.</p>
          )}
        </div>
      </div>
      {['CONFIRMED', 'PROCESSING'].includes(o.status) && (
        <button
          className="btn btn-light"
          onClick={async () => {
            await api.cancelOrder(o.id);
            setData(await api.orderDetail(o.id));
          }}
        >
          Cancel order
        </button>
      )}
      {o.status === 'DELIVERED' && !returnSent && (
        <form
          className="checkout-form"
          style={{ maxWidth: 720, marginTop: 32 }}
          onSubmit={async (e) => {
            e.preventDefault();
            const selected = orderItems.map((i: any) => ({ orderItemId: i.id, quantity: returnQty[i.id] ?? i.quantity })).filter((x: any) => x.quantity > 0);
            await api.returns.create(user.id, o.id, reason, selected);
            setReturnSent(true);
          }}
        >
          <h2>Request a return</h2>
          {orderItems.map((i: any) => <label key={i.id} style={{display:'block',marginBottom:8}}>{i.productName} (max {i.quantity})<input className="field" type="number" min={0} max={i.quantity} value={returnQty[i.id] ?? i.quantity} onChange={e => setReturnQty(v => ({...v, [i.id]: Math.max(0, Math.min(i.quantity, Number(e.target.value) || 0))}))} /></label>)}
          <textarea
            required
            minLength={10}
            maxLength={1000}
            value={reason}
            onChange={e => setReason(e.target.value)}
            placeholder="Tell us why you want to return this order"
            className="field admin-textarea"
          />
          <button className="btn btn-orange">Submit return request</button>
        </form>
      )}
      {returnSent && (
        <p className="checkout-login-note">Return request submitted. Our team will review it.</p>
      )}
    </main>
  );
}

export default OrderDetail;

import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api, type Customer } from '../../api';
import { money } from '../../utils/format';
import { useSeo } from '../../hooks/useSeo';

export interface OrdersProps {
  user: Customer | null;
}

export function Orders({ user }: OrdersProps) {
  useSeo('Your Orders | Wolfe — The Jewel of Villa', 'View and track your previous orders.');
  const [orders, setOrders] = useState<any[]>([]);
  const [loading, setLoading] = useState(!!user);

  useEffect(() => {
    if (!user) {
      setOrders([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    api.customerOrders(user.id)
      .then(setOrders)
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, [user]);

  return (
    <main className="container-w section">
      <p className="eyebrow">Customer account</p>
      <h1>Orders</h1>
      {!user ? (
        <p className="empty-state">Sign in to view your orders.</p>
      ) : loading ? (
        <p className="empty-state">Loading orders…</p>
      ) : orders.length ? (
        <div className="orders-list">
          {orders.map(o => (
            <Link to={`/orders/${o.id}`} className="order-card" key={o.id}>
              <div>
                <strong>{o.id}</strong>
                <p>{new Date(o.createdAt).toLocaleDateString('en-IN')}</p>
              </div>
              <span>{o.status}</span>
              <strong>{money(o.total / 100)}</strong>
            </Link>
          ))}
        </div>
      ) : (
        <p className="empty-state">No orders yet.</p>
      )}
    </main>
  );
}

export default Orders;

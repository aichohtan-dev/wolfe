import React, { useState, useEffect } from 'react';
import { api, type Customer } from '../../api';

export interface NotificationsPageProps {
  user: Customer | null;
}

export function NotificationsPage({ user }: NotificationsPageProps) {
  const [data, setData] = useState<any>({ items: [], unread: 0 });

  useEffect(() => {
    if (user) {
      api.notifications.get(user.id).then(setData).catch(() => setData({ items: [], unread: 0 }));
    }
  }, [user]);

  if (!user) {
    return (
      <main className="container-w narrow-page">
        <p className="empty-state">Sign in to view notifications.</p>
      </main>
    );
  }

  return (
    <main className="container-w section">
      <div className="admin-head">
        <div>
          <p className="eyebrow">Customer account</p>
          <h1>Notifications</h1>
          <p>{data.unread || 0} unread</p>
        </div>
        {data.items?.some((n: any) => !n.readAt) && (
          <button
            className="btn btn-light"
            onClick={async () => {
              await api.notifications.readAll(user.id);
              setData(await api.notifications.get(user.id));
            }}
          >
            Mark all read
          </button>
        )}
      </div>
      <div className="orders-list">
        {data.items?.length ? (
          data.items.map((n: any) => (
            <div
              className="order-card"
              key={n.id}
              style={{ opacity: n.readAt?.length ? 0.65 : 1 }}
              onClick={async () => {
                if (!n.readAt) {
                  await api.notifications.read(user.id, n.id);
                  setData(await api.notifications.get(user.id));
                }
              }}
            >
              <div>
                <strong>{n.title}</strong>
                <p>{n.message}</p>
                <small>{new Date(n.createdAt).toLocaleString('en-IN')}</small>
              </div>
            </div>
          ))
        ) : (
          <p className="empty-state">No notifications yet.</p>
        )}
      </div>
    </main>
  );
}

export default NotificationsPage;

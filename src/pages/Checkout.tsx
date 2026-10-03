import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { products } from '../data';
import { api, type Customer } from '../api';
import { money } from '../utils/format';
import { useConfigurationDetails } from '../hooks/useConfigurationDetails';
import { cartUnitPrice } from '../utils/cartPricing';
import type { CartItem } from '../types/cart';

export interface CheckoutProps {
  cart: CartItem[];
  user: Customer | null;
  onPlaced: (o: any) => void;
}

export default function Checkout({ cart, user, onPlaced }: CheckoutProps) {
  const navigate = useNavigate();
  const configs = useConfigurationDetails(cart);
  const [form, setForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: '',
    city: '',
    pincode: '',
    coupon: '',
  });
  const [shipping, setShipping] = useState<any>(null);
  const [bundleQuotes, setBundleQuotes] = useState<Record<string, any>>({});
  const [discount, setDiscount] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const rows = cart
    .map((i) => ({ item: i, p: products.find((p) => p.id === i.id) }))
    .filter((x) => x.p);

  const grossSubtotal = rows.reduce(
    (s, x) => s + cartUnitPrice(x.p, x.item, configs) * x.item.qty,
    0
  );

  const bundleDiscount = Object.entries(bundleQuotes).reduce(
    (sum, [slug, q]: [string, any]) => {
      const bundleLines = cart.filter(
        (i) => i.bundleSlug === slug && i.bundleId
      );
      const bundleQty = bundleLines.length
        ? Math.min(...bundleLines.map((i) => i.bundleUnits || 1))
        : 0;
      return sum + Number(q.discount || 0) * bundleQty;
    },
    0
  );

  const subtotal = Math.max(0, grossSubtotal - bundleDiscount);

  useEffect(() => {
    let live = true;
    const slugs = [
      ...new Set(cart.map((i) => i.bundleSlug).filter(Boolean) as string[]),
    ];
    if (slugs.length > 0) {
      Promise.all(slugs.map((slug) => api.bundles.get(slug!)))
        .then((values) => {
          if (live)
            setBundleQuotes(Object.fromEntries(values.map((v) => [v.slug, v])));
        })
        .catch(() => {
          if (live) setBundleQuotes({});
        });
    }
    return () => {
      live = false;
    };
  }, [cart]);

  useEffect(() => {
    let live = true;
    api
      .shippingQuote(subtotal, 'STANDARD')
      .then((x) => {
        if (live) setShipping(x);
      })
      .catch(() => {
        if (live) setShipping(null);
      });
    return () => {
      live = false;
    };
  }, [subtotal]);

  useEffect(() => {
    let live = true;
    const code = form.coupon.trim();
    if (!code) {
      setDiscount(0);
      return;
    }
    api
      .couponQuote(subtotal, code)
      .then((x: any) => {
        if (live) setDiscount(Number(x?.discount || 0) / 100);
      })
      .catch(() => {
        if (live) setDiscount(0);
      });
    return () => {
      live = false;
    };
  }, [subtotal, form.coupon]);

  const total = Math.max(
    0,
    subtotal - discount + (shipping ? Number(shipping.shippingFee || 0) / 100 : 0)
  );

  const submit = async (e: any) => {
    e.preventDefault();
    if (!cart.length || !user) {
      setError('Please sign in before placing an order.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const idempotencyKey = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
      const order = await api.order({
        customerId: user.id,
        customerName: form.name,
        customerEmail: form.email,
        phone: form.phone,
        address: form.address,
        city: form.city,
        pincode: form.pincode,
        paymentMethod: 'COD',
        shippingMethod: 'STANDARD',
        couponCode: form.coupon || undefined,
        items: cart.map((i) => ({
          slug: i.id,
          quantity: i.qty,
          configurationToken: i.configurationToken,
          bundleId: i.bundleId,
          variantId: i.variantId,
          variantSku: i.variantSku,
        })),
      }, idempotencyKey);
      try { await api.cart.clear(user.id); } catch { /* order is authoritative; local cart is cleared below */ }
      onPlaced(order);
      navigate(`/orders/${order.id}`);
    } catch (err: any) {
      setError(err.message || 'Could not place order');
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="container-w section">
      <p className="eyebrow">Checkout</p>
      <h1>Complete your order.</h1>
      <div className="checkout-grid">
        <form className="checkout-form" onSubmit={submit}>
          <input
            className="field"
            required
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="Full name"
          />
          <input
            className="field"
            required
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            placeholder="Email"
          />
          <input
            className="field"
            required
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
            placeholder="Phone"
          />
          <textarea
            className="field textarea"
            required
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
            placeholder="Delivery address"
          />
          <div className="checkout-two">
            <input
              className="field"
              required
              value={form.city}
              onChange={(e) => setForm({ ...form, city: e.target.value })}
              placeholder="City"
            />
            <input
              className="field"
              required
              value={form.pincode}
              onChange={(e) => setForm({ ...form, pincode: e.target.value })}
              placeholder="Pincode"
            />
          </div>
          <input
            className="field"
            value={form.coupon}
            onChange={(e) =>
              setForm({ ...form, coupon: e.target.value.toUpperCase() })
            }
            placeholder="Coupon code (optional)"
          />
          {error && <p className="error-message">{error}</p>}
          <button
            className="btn btn-orange full"
            disabled={busy || !cart.length || !user}
          >
            {busy ? 'Placing order…' : 'Place COD order'}
          </button>
          <p className="checkout-login-note">
            Online payment is intentionally pending. Cash on Delivery (COD) is active.
          </p>
        </form>
        <aside className="order-summary">
          <h2>Order summary</h2>
          {rows.map(({ item, p }) => {
            const unit = cartUnitPrice(p, item, configs);
            const cfg = item.configurationToken
              ? configs[item.configurationToken]
              : null;
            const varLabel = item.variantTitle || item.variantColor || item.variantSize;
            return (
              <div
                className="summary-line"
                key={`${item.id}-${item.variantId || 'base'}-${item.configurationToken || 'base'}`}
              >
                <span>
                  {p!.name} {varLabel ? `(${varLabel})` : ''} × {item.qty}
                  {cfg ? ' · configured' : ''}
                </span>
                <span>{money(unit * item.qty)}</span>
              </div>
            );
          })}
          <div className="summary-line">
            <span>Subtotal</span>
            <span>{money(subtotal)}</span>
          </div>
          {discount > 0 && (
            <div className="summary-line">
              <span>Discount</span>
              <span>−{money(discount)}</span>
            </div>
          )}
          <div className="summary-line">
            <span>Shipping</span>
            <span>
              {shipping
                ? money(Number(shipping.shippingFee || 0) / 100)
                : '—'}
            </span>
          </div>
          <div className="summary-total">
            <strong>Total</strong>
            <strong>{money(total)}</strong>
          </div>
        </aside>
      </div>
    </main>
  );
}

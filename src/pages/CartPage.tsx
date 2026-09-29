import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Trash2 } from 'lucide-react';
import { products } from '../data';
import { api } from '../api';
import { money } from '../utils/format';
import { useSeo } from '../hooks/useSeo';
import { useConfigurationDetails } from '../hooks/useConfigurationDetails';
import { cartUnitPrice } from '../utils/cartPricing';
import type { CartItem } from '../types/cart';

export function CartPage({ cart, onQty, onRemove }: {
    cart: CartItem[];
    onQty: (id: string, token: string | undefined, n: number, bundleId?: number, variantId?: number) => void;
    onRemove: (id: string, token: string | undefined, bundleId?: number, variantId?: number) => void;
}) {
    useSeo('Shopping Bag | Wolfe — Architectural Hardware & Surfaces', 'Review your selected architectural hardware and surface pieces and continue to checkout.');
    const configs = useConfigurationDetails(cart);
    const [bundleQuotes, setBundleQuotes] = useState<Record<string, any>>({});

    useEffect(() => {
        let live = true;
        const slugs = [...new Set(cart.map(i => i.bundleSlug).filter(Boolean) as string[])];
        if (slugs.length > 0) {
            Promise.all(slugs.map(slug => api.bundles.get(slug!)))
                .then(values => {
                    if (live) setBundleQuotes(Object.fromEntries(values.map(v => [v.slug, v])));
                })
                .catch(() => {
                    if (live) setBundleQuotes({});
                });
        }
        return () => {
            live = false;
        };
    }, [cart]);

    const rows = cart.map(i => ({ item: i, p: products.find(p => p.id === i.id) })).filter(x => x.p);
    const grossSubtotal = rows.reduce((s, x) => s + cartUnitPrice(x.p, x.item, configs) * x.item.qty, 0);
    const bundleDiscount = Object.entries(bundleQuotes).reduce((sum, [slug, q]: [string, any]) => {
        const bundleLines = cart.filter(i => i.bundleSlug === slug && i.bundleId);
        const bundleQty = bundleLines.length ? Math.min(...bundleLines.map(i => i.qty)) : 0;
        return sum + Number(q.discount || 0) * bundleQty;
    }, 0);
    const subtotal = Math.max(0, grossSubtotal - bundleDiscount);

    return (
        <main className="container-w section">
            <p className="eyebrow">Your shopping bag</p>
            <div className="admin-head">
                <div>
                    <h1>Shopping Bag</h1>
                    <p>{rows.length ? `${cart.reduce((s, x) => s + x.qty, 0)} pieces in your bag.` : 'Your bag is currently empty.'}</p>
                </div>
            </div>
            {rows.length ? (
                <div className="checkout-grid" style={{ marginTop: 32 }}>
                    <div>
                        {rows.map(({ item, p }) => {
                            const unit = cartUnitPrice(p, item, configs);
                            const cfg = item.configurationToken ? configs[item.configurationToken] : null;
                            let label = item.bundleId ? `Bundle #${item.bundleId}` : 'custom detail';
                            try {
                                if (cfg?.configJson) label = JSON.parse(cfg.configJson).accessoryName || label;
                            } catch { }

                            const variantDesc = [
                                item.variantTitle,
                                item.variantColor && `Color: ${item.variantColor}`,
                                item.variantSize && `Size: ${item.variantSize}`,
                                item.variantFinish && `Finish: ${item.variantFinish}`,
                            ].filter(Boolean).join(' · ');

                            const itemImage = item.variantImage || p!.image || '/catalog/brass-01.jpg';

                            return (
                                <div className="cart-row" key={`${item.id}-${item.variantId || 'base'}-${item.configurationToken || 'base'}`}>
                                    <img src={itemImage} alt={p!.name} />
                                    <div className="cart-row-copy">
                                        <strong>{p!.name}</strong>
                                        {variantDesc ? (
                                            <small style={{ color: 'var(--color-gold, #c5a880)' }}>{variantDesc}</small>
                                        ) : (
                                            <small>{cfg ? `Configured · ${label}` : item.bundleId ? `Bundle piece · ${item.bundleSlug}` : 'Standard piece'}</small>
                                        )}
                                        {item.variantSku && <small style={{ color: 'var(--color-muted)' }}>SKU: {item.variantSku}</small>}
                                        <span>{money(unit)} × {item.qty} = {money(unit * item.qty)}</span>
                                        <div>
                                            <button onClick={() => onQty(item.id, item.configurationToken, Math.max(0, item.qty - 1), item.bundleId, item.variantId)}>−</button>
                                            <span>{item.qty}</span>
                                            <button onClick={() => onQty(item.id, item.configurationToken, item.qty + 1, item.bundleId, item.variantId)}>+</button>
                                            <button onClick={() => onRemove(item.id, item.configurationToken, item.bundleId, item.variantId)} aria-label="Remove"><Trash2 size={15} /></button>
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                    <aside className="order-summary">
                        <h2>Order summary</h2>
                        <div className="summary-line">
                            <span>Subtotal</span>
                            <span>{money(grossSubtotal)}</span>
                        </div>
                        {bundleDiscount > 0 && (
                            <div className="summary-line">
                                <span>Bundle savings</span>
                                <span>−{money(bundleDiscount)}</span>
                            </div>
                        )}
                        <div className="summary-total">
                            <strong>Total</strong>
                            <strong>{money(subtotal)}</strong>
                        </div>
                        <Link to="/checkout" className="btn btn-orange full" style={{ marginTop: 20 }}>
                            Continue to checkout
                        </Link>
                    </aside>
                </div>
            ) : (
                <div style={{ textAlign: 'center', padding: '60px 0' }}>
                    <p className="empty-state">Your bag is empty.</p>
                    <Link to="/shop" className="btn btn-orange">
                        Explore the collection <ArrowRight size={16} />
                    </Link>
                </div>
            )}
        </main>
    );
}

export default CartPage;

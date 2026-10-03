import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { X, Trash2 } from 'lucide-react';
import { products } from '../../data';
import { api } from '../../api';
import { money } from '../../utils/format';
import { useConfigurationDetails } from '../../hooks/useConfigurationDetails';
import { cartUnitPrice } from '../../utils/cartPricing';
import type { CartItem } from '../../types/cart';

export function CartDrawer({ items, onClose, onQty, onRemove }: {
    items: CartItem[];
    onClose: () => void;
    onQty: (id: string, token: string | undefined, n: number, bundleId?: number, variantId?: number) => void;
    onRemove: (id: string, token: string | undefined, bundleId?: number, variantId?: number) => void;
}) {
    const configs = useConfigurationDetails(items);
    const [bundleQuotes, setBundleQuotes] = useState<Record<string, any>>({});

    useEffect(() => {
        let live = true;
        const slugs = [...new Set(items.map(i => i.bundleSlug).filter(Boolean) as string[])];
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
    }, [items]);

    const rows = items.map(i => ({ item: i, p: products.find(p => p.id === i.id) })).filter(x => x.p);
    const grossSubtotal = rows.reduce((s, x) => s + cartUnitPrice(x.p, x.item, configs) * x.item.qty, 0);
    const bundleDiscount = Object.entries(bundleQuotes).reduce((sum, [slug, q]: [string, any]) => {
        const bundleLines = items.filter(i => i.bundleSlug === slug && i.bundleId);
        const bundleQty = bundleLines.length ? Math.min(...bundleLines.map(i => i.bundleUnits || 1)) : 0;
        return sum + Number(q.discount || 0) * bundleQty;
    }, 0);
    const subtotal = Math.max(0, grossSubtotal - bundleDiscount);

    return (
        <div className="cart-drawer-backdrop" onClick={onClose}>
            <aside className="cart-drawer" onClick={e => e.stopPropagation()}>
                <div className="admin-head">
                    <div>
                        <p className="eyebrow">Your bag</p>
                        <h2>Selected pieces.</h2>
                    </div>
                    <button className="icon-btn" onClick={onClose} aria-label="Close"><X /></button>
                </div>
                {rows.length ? (
                    rows.map(({ item, p }) => {
                        const unit = cartUnitPrice(p, item, configs);
                        const cfg = item.configurationToken ? configs[item.configurationToken] : null;
                        let label = item.bundleId ? `Bundle #${item.bundleId}` : 'custom detail';
                        try {
                            if (cfg?.configJson) {
                                label = JSON.parse(cfg.configJson).accessoryName || label;
                            }
                        } catch { }

                        const bundleUnits = item.bundleId ? (item.bundleUnits || 1) : 0;
                        const variantDesc = [
                            item.variantTitle,
                            item.variantColor && `Color: ${item.variantColor}`,
                            item.variantSize && `Size: ${item.variantSize}`,
                            item.variantFinish && `Finish: ${item.variantFinish}`,
                        ].filter(Boolean).join(' · ');

                        const itemImage = item.variantImage || p!.image || '/catalog/brass-01.jpg';

                        return (
                            <div className="cart-row" key={`${item.id}-${item.variantId || 'base'}-${item.configurationToken || 'base'}`}>
                                <img src={itemImage} alt="" />
                                <div className="cart-row-copy">
                                    <strong>{p!.name}</strong>
                                    {variantDesc ? (
                                        <small style={{ color: 'var(--color-gold, #c5a880)' }}>{variantDesc}</small>
                                    ) : (
                                        <small>{cfg ? `Configured · ${label}` : 'Standard piece'}</small>
                                    )}
                                    {item.variantSku && <small style={{ color: 'var(--color-muted)' }}>SKU: {item.variantSku}</small>}
                                    <span>{item.bundleId ? `Bundle × ${bundleUnits}` : `${money(unit)} × ${item.qty}`}</span>
                                    <div>
                                        <button onClick={() => onQty(item.id, item.configurationToken, item.bundleId ? Math.max(0, bundleUnits - 1) : Math.max(0, item.qty - 1), item.bundleId, item.variantId)}>−</button>
                                        <span>{item.bundleId ? bundleUnits : item.qty}</span>
                                        <button onClick={() => onQty(item.id, item.configurationToken, item.bundleId ? bundleUnits + 1 : item.qty + 1, item.bundleId, item.variantId)}>+</button>
                                        <button onClick={() => onRemove(item.id, item.configurationToken, item.bundleId, item.variantId)} aria-label="Remove"><Trash2 size={15} /></button>
                                    </div>
                                </div>
                            </div>
                        );
                    })
                ) : (
                    <p className="empty-state">Your bag is empty.</p>
                )}
                <div className="cart-drawer-total">
                    <span>Subtotal</span>
                    <strong>{money(subtotal)}</strong>
                </div>
                {bundleDiscount > 0 && (
                    <div className="summary-line">
                        <span>Bundle savings</span>
                        <span>−{money(bundleDiscount)}</span>
                    </div>
                )}
                {rows.length > 0 && (
                    <Link to="/checkout" className="btn btn-orange full" onClick={onClose}>
                        Continue to checkout
                    </Link>
                )}
            </aside>
        </div>
    );
}

export default CartDrawer;

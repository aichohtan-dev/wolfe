import { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { products } from './data';
let activeProductSlugs: Set<string> | null = null;
import { api, type Customer, type ProductVariant } from './api';
import PrivacyPolicy from './pages/legal/PrivacyPolicy';
import Terms from './pages/legal/Terms';
import { read, write } from './utils/storage';
import type { CartItem } from './types/cart';
import { CookieConsentBanner } from './components/layout/CookieConsentBanner';
import { Footer } from './components/layout/Footer';
import { CartDrawer } from './components/commerce/CartDrawer';
import CartPage from './pages/CartPage';
import NotFound from './pages/NotFound';
import SimpleContent from './pages/SimpleContent';
import Consultation from './pages/experience/Consultation';
import QuoteRequest from './pages/experience/QuoteRequest';
import CustomDesign from './pages/experience/CustomDesign';
import ConfigurationShare from './pages/experience/ConfigurationShare';
import Wishlist from './pages/Wishlist';
import ProfilePage from './pages/account/ProfilePage';
import AddressesPage from './pages/account/AddressesPage';
import NotificationsPage from './pages/account/NotificationsPage';
import Account from './pages/account/Account';
import Orders from './pages/account/Orders';
import OrderDetail from './pages/account/OrderDetail';
import Header from './components/layout/Header';
import Comparison from './pages/Comparison';
import Bundles from './pages/Bundles';
import Home from './pages/Home';
import Shop from './pages/Shop';
import ProductPage from './pages/ProductPage';
import Checkout from './pages/Checkout';
import Admin from './pages/admin/Admin';
import RetailerPortal from './pages/retailer/RetailerPortal';
import { ProductCard } from './components/commerce/ProductCard';

export function ProductGrid({ items, onAdd, onWish, wishes, onCompare = () => { }, compared = [] }: {
    items: typeof products;
    onAdd: (id: string, variant?: Partial<ProductVariant>) => void;
    onWish: (id: string) => void;
    wishes: string[];
    onCompare?: (id: string) => void;
    compared?: string[];
}) {
    const visible = activeProductSlugs ? items.filter(p => activeProductSlugs!.has(p.id)) : items;
    return (
        <div className="product-grid">
            {visible.map(p => (
                <ProductCard
                    key={p.id}
                    p={p}
                    onAdd={onAdd}
                    onWish={onWish}
                    wishes={wishes}
                    onCompare={onCompare}
                    compared={compared.includes(p.id)}
                />
            ))}
        </div>
    );
}

function App() {
    const [compareList, setCompareList] = useState<string[]>(() => read('wolfe_compare', []));
    const toggleCompare = (id: string) => {
        setCompareList(v => {
            const next = v.includes(id) ? v.filter(x => x !== id) : v.length >= 4 ? v : [...v, id];
            write('wolfe_compare', next);
            return next;
        });
    };
    const [user, setUser] = useState<Customer | null>(() => localStorage.getItem('wolfe_storage_consent') === 'accepted' ? read('wolfe_user', null) : null);
    const [, setCatalogVersion] = useState(0);

    useEffect(() => {
        api.me().then(u => { void syncAccount(u); }).catch(() => { localStorage.removeItem('wolfe_user'); });
    }, []);

    useEffect(() => {
        // Never expose bundled/demo catalog data as a production fallback.
        products.splice(0, products.length);
        activeProductSlugs = new Set();
        setCatalogVersion(v => v + 1);
        api.pagedProducts({ page: 0, pageSize: 24 }).then((paged: any) => {
            const server = paged.content || [];
            const mapped = server.filter((s: any) => s.active !== false).map(mapServerProduct);
            products.splice(0, products.length, ...mapped);
            activeProductSlugs = new Set(mapped.map((p: any) => p.id));
            setCatalogVersion(v => v + 1);
            void hydrateCartProducts(cart);
        }).catch(() => { });
    }, []);

    const [cart, setCart] = useState<CartItem[]>(() => read('wolfe_cart', []));

    const addBundle = async (b: any) => {
        const next = [...cart];
        for (const line of b.items || []) {
            const same = (x: CartItem) => x.id === line.slug && x.bundleId === Number(b.id) && !x.configurationToken;
            const found = next.find(same);
            if (found) {
                const baseQuantity = found.bundleBaseQuantity || Number(line.quantity || 1);
                const units = (found.bundleUnits || 1) + 1;
                found.bundleBaseQuantity = baseQuantity;
                found.bundleUnits = units;
                found.qty = baseQuantity * units;
            } else {
                const baseQuantity = Number(line.quantity || 1);
                next.push({ id: line.slug, qty: baseQuantity, bundleId: Number(b.id), bundleSlug: b.slug, bundleUnits: 1, bundleBaseQuantity: baseQuantity });
            }
        }
        persistCart(next);
        setDrawer(true);
        if (user) {
            for (const line of b.items || []) {
                const lineItem = next.find(x => x.id === line.slug && x.bundleId === Number(b.id) && !x.configurationToken);
                if (lineItem) void api.cart.put(user.id, lineItem.id, lineItem.qty, lineItem.variantId, lineItem.bundleId).catch(() => {});
            }
        }
    };

    const [wishes, setWishes] = useState<string[]>(() => read('wolfe_wishlist', []));
    const [drawer, setDrawer] = useState(false);
    const [storageConsent, setStorageConsent] = useState<string | null>(() => localStorage.getItem('wolfe_storage_consent'));
    const [consentOpen, setConsentOpen] = useState(false);

    const handleConsent = (choice: 'accepted' | 'essential_only') => {
        localStorage.setItem('wolfe_storage_consent', choice);
        setStorageConsent(choice);
        setConsentOpen(false);
    };

    const persistCart = (v: CartItem[]) => {
        setCart(v);
        write('wolfe_cart', v);
    };

    const mapServerProduct = (s: any) => ({
        id: s.slug,
        serverId: Number(s.id),
        name: s.name,
        category: s.category,
        subcategory: s.subcategory,
        brandName: s.brandName,
        price: Number(s.price),
        image: s.imageUrl || '/catalog/brass-01.jpg',
        description: s.description || '',
        finish: s.finish,
        material: s.material || 'Metal',
        color: s.color || 'Brass',
        style: s.style || 'Modern',
        dimensions: s.dimensions,
        attributesJson: s.attributesJson,
        featured: s.featured === true,
        media: String(s.mediaUrls || '').split(/\r?\n|,/).map((x: string) => x.trim()).filter(Boolean)
    });

    const hydrateCartProducts = async (items: CartItem[]) => {
        const missing = [...new Set(items.map(x => x.id).filter(slug => !products.some(p => p.id === slug)))];
        if (!missing.length) return;
        const results = await Promise.allSettled(missing.map(slug => api.product(slug)));
        const fetched = results.filter((r): r is PromiseFulfilledResult<any> => r.status === 'fulfilled').map(r => mapServerProduct(r.value));
        if (fetched.length) {
            products.splice(0, 0, ...fetched.filter(p => !products.some(x => x.id === p.id)));
            activeProductSlugs = new Set([...products.map(p => p.id)]);
            setCatalogVersion(v => v + 1);
        }
    };

    const syncAccount = async (u: Customer) => {
        setUser(u);
        if (localStorage.getItem('wolfe_storage_consent') === 'accepted') write('wolfe_user', u);
        try {
            const [serverCart, serverWishes] = await Promise.all([api.cart.get(u.id), api.wishlist.get(u.id).catch(() => [] as string[])]);
            const mapped = serverCart.map((x: any) => ({
                id: x.slug,
                qty: x.quantity,
                bundleId: x.bundleId ?? undefined,
                bundleSlug: x.bundleSlug ?? undefined,
                bundleUnits: x.bundleUnits ?? undefined,
                bundleBaseQuantity: x.bundleBaseQuantity ?? undefined,
                configurationToken: x.configurationToken ?? undefined,
                variantId: x.variantId ?? undefined,
                variantSku: x.variantSku ?? undefined,
                variantTitle: x.variantTitle ?? undefined,
                variantColor: x.variantColor ?? undefined,
                variantMaterial: x.variantMaterial ?? undefined,
                variantSize: x.variantSize ?? undefined,
                variantFinish: x.variantFinish ?? undefined,
                variantImage: x.variantImage ?? undefined,
                variantPrice: x.variantPrice ?? undefined
            })).filter((x: any) => !!x.id) as CartItem[];
            const key = (x: any) => `${x.id}|${x.variantId ?? ''}|${x.configurationToken ?? ''}|${x.bundleId ?? ''}`;
            const byKey = new Map(mapped.map((x: CartItem) => [key(x), x]));
            for (const local of cart) {
                const k = key(local);
                const server = byKey.get(k);
                if (!server) byKey.set(k, local);
                else if (local.qty > server.qty) {
                    server.qty = local.qty;
                    server.bundleUnits = local.bundleUnits;
                    server.bundleBaseQuantity = local.bundleBaseQuantity;
                }
            }
            const merged = [...byKey.values()];
            persistCart(merged);
            await hydrateCartProducts(merged);
            for (const item of merged) {
                void api.cart.put(u.id, item.id, item.qty, item.variantId, item.bundleId, item.configurationToken).catch(() => {});
            }
            const localWishes = read('wolfe_wishlist', []) as string[];
            const mergedWishes = [...new Set([...serverWishes, ...localWishes])];
            for (const slug of mergedWishes) {
                if (!serverWishes.includes(slug)) void api.wishlist.add(u.id, slug).catch(() => {});
            }
            setWishes(mergedWishes);
            write('wolfe_wishlist', mergedWishes);
        } catch { }
    };

    const add = async (id: string, variant?: Partial<ProductVariant>) => {
        const same = (x: CartItem) =>
            x.id === id &&
            !x.configurationToken &&
            !x.bundleId &&
            ((!variant && !x.variantId) || (variant && x.variantId === variant.id));

        let next: CartItem[];
        const existing = cart.find(same);

        if (existing) {
            next = cart.map(x => (same(x) ? { ...x, qty: x.qty + 1 } : x));
        } else {
            const newItem: CartItem = {
                id,
                qty: 1,
                variantId: variant?.id,
                variantSku: variant?.sku,
                variantTitle: variant?.title,
                variantColor: variant?.color,
                variantMaterial: variant?.material,
                variantSize: variant?.size,
                variantFinish: variant?.finish,
                variantImage: variant?.imageUrl,
                variantPrice: variant?.price
            };
            next = [...cart, newItem];
        }

        persistCart(next);
        setDrawer(true);

        if (user) {
            const item = next.find(same)!;
            try {
                await api.cart.put(user.id, id, item.qty, variant?.id, item.bundleId, item.configurationToken);
            } catch { }
        }
    };

    const addConfigured = async (id: string, configurationToken: string) => {
        const next = cart.some(x => x.id === id && x.configurationToken === configurationToken)
            ? cart.map(x => (x.id === id && x.configurationToken === configurationToken ? { ...x, qty: x.qty + 1 } : x))
            : [...cart, { id, qty: 1, configurationToken }];
        persistCart(next);
        setDrawer(true);
        if (user) {
            const item = next.find(x => x.id === id && x.configurationToken === configurationToken);
            if (item) void api.cart.put(user.id, id, item.qty, item.variantId, item.bundleId, configurationToken).catch(() => {});
        }
    };

    const qty = async (
        id: string,
        configurationToken: string | undefined,
        n: number,
        bundleId?: number,
        variantId?: number
    ) => {
        const same = (x: CartItem) =>
            x.id === id &&
            x.configurationToken === configurationToken &&
            x.bundleId === bundleId &&
            x.variantId === variantId;

        const next = bundleId
            ? n < 1
                ? cart.filter(x => x.bundleId !== bundleId)
                : cart.map(x => x.bundleId === bundleId
                    ? { ...x, bundleUnits: n, qty: (x.bundleBaseQuantity || x.qty) * n }
                    : x)
            : n < 1
            ? cart.filter(x => !same(x))
            : cart.map(x => (same(x) ? { ...x, qty: n } : x));

        persistCart(next);

        if (user) {
            try {
                if (bundleId) {
                    const bundleLines = next.filter(x => x.bundleId === bundleId);
                    if (n < 1) {
                        for (const line of cart.filter(x => x.bundleId === bundleId)) {
                            await api.cart.remove(user.id, line.id, line.variantId, line.bundleId, line.configurationToken);
                        }
                    } else {
                        for (const line of bundleLines) {
                            await api.cart.put(user.id, line.id, line.qty, line.variantId, line.bundleId, line.configurationToken);
                        }
                    }
                } else if (n < 1) {
                    await api.cart.remove(user.id, id, variantId, undefined, configurationToken);
                } else {
                    await api.cart.put(user.id, id, n, variantId, undefined, configurationToken);
                }
            } catch { }
        }
    };

    const remove = (
        id: string,
        configurationToken: string | undefined,
        bundleId?: number,
        variantId?: number
    ) => qty(id, configurationToken, 0, bundleId, variantId);

    const wish = async (id: string) => {
        const adding = !wishes.includes(id);
        const v = adding ? [...wishes, id] : wishes.filter(x => x !== id);
        setWishes(v);
        write('wolfe_wishlist', v);
        if (user) {
            try {
                if (adding) await api.wishlist.add(user.id, id);
                else await api.wishlist.remove(user.id, id);
            } catch { }
        }
    };

    const placed = (order: any) => {
        persistCart([]);
        return order;
    };

    const logout = async () => {
        try {
            await api.logout();
        } catch { }
        setUser(null);
        localStorage.removeItem('wolfe_user');
        setCart([]);
        setWishes([]);
        localStorage.removeItem('wolfe_cart');
        localStorage.removeItem('wolfe_wishlist');
    };

    return (
        <div>
            <Header cartCount={cart.reduce((s, x) => s + x.qty, 0)} wishCount={wishes.length} onCart={() => setDrawer(true)} />
            <Routes>
                <Route path="/" element={<Home onAdd={add} onWish={wish} wishes={wishes} onCompare={toggleCompare} compared={compareList} />} />
                <Route path="/bundles" element={<Bundles onAddBundle={addBundle} />} />
                <Route path="/shop" element={<Shop onAdd={add} onWish={wish} wishes={wishes} onCompare={toggleCompare} compared={compareList} />} />
                <Route path="/cart" element={<CartPage cart={cart} onQty={qty} onRemove={remove} />} />
                <Route path="/product/:id" element={<ProductPage onAdd={add} onAddConfigured={addConfigured} onWish={wish} wishes={wishes} user={user} />} />
                <Route path="/compare" element={<Comparison items={compareList.map(id => products.find(p => p.id === id)).filter(Boolean) as any} onAdd={add} onWish={wish} wishes={wishes} onCompare={toggleCompare} compared={compareList} />} />
                <Route path="/config/:token" element={<ConfigurationShare />} />
                <Route path="/wishlist" element={<Wishlist wishes={wishes} onWish={wish} onAdd={add} />} />
                <Route path="/checkout" element={<Checkout cart={cart} user={user} onPlaced={placed} />} />
                <Route path="/account" element={<Account user={user} onLogin={syncAccount} onLogout={logout} />} />
                <Route path="/orders" element={<Orders user={user} />} />
                <Route path="/notifications" element={<NotificationsPage user={user} />} />
                <Route path="/orders/:id" element={<OrderDetail user={user} />} />
                <Route path="/profile" element={<ProfilePage user={user} onSaved={u => { setUser(u); if (localStorage.getItem('wolfe_storage_consent') === 'accepted') write('wolfe_user', u); }} />} />
                <Route path="/addresses" element={<AddressesPage user={user} />} />
                <Route path="/consultation" element={<Consultation />} />
                <Route path="/quote" element={<QuoteRequest user={user} />} />
                <Route path="/custom-design" element={<CustomDesign user={user} />} />
                <Route path="/catalogue" element={<SimpleContent eyebrow="Wolfe catalogue" title="Explore the master collection." copy="Request the latest product catalogue for finishes, dimensions and project planning." />} />
                <Route path="/journal" element={<SimpleContent eyebrow="Journal" title="Ideas for considered spaces." copy="A growing editorial space for materials, rooms, finishes and the small details that make a home feel like yours." />} />
                <Route path="/privacy-policy" element={<PrivacyPolicy />} />
                <Route path="/terms" element={<Terms />} />
                <Route path="/retailer" element={<RetailerPortal />} />
                <Route path="/admin" element={<Admin />} />
                <Route path="*" element={<NotFound />} />
            </Routes>
            <Footer onOpenConsent={() => setConsentOpen(true)} />
            {drawer && <CartDrawer items={cart} onClose={() => setDrawer(false)} onQty={qty} onRemove={remove} />}
            {(!storageConsent || consentOpen) && <CookieConsentBanner onChoice={handleConsent} onClose={() => setConsentOpen(false)} showClose={!!storageConsent} />}
        </div>
    );
}

export default App;

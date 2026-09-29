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
    const [user, setUser] = useState<Customer | null>(() => read('wolfe_user', null));
    const [, setCatalogVersion] = useState(0);

    useEffect(() => {
        api.products().then((server: any[]) => {
            const mapped = server.filter(s => s.active !== false).map(s => ({
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
            }));
            products.splice(0, products.length, ...mapped);
            activeProductSlugs = new Set(mapped.map(p => p.id));
            setCatalogVersion(v => v + 1);
        }).catch(() => { });
    }, []);

    const [cart, setCart] = useState<CartItem[]>(() => read('wolfe_cart', []));

    const addBundle = async (b: any) => {
        const next = [...cart];
        for (const line of b.items || []) {
            const same = (x: CartItem) => x.id === line.slug && x.bundleId === Number(b.id) && !x.configurationToken;
            const found = next.find(same);
            if (found)
                found.qty += Number(line.quantity || 1);
            else
                next.push({ id: line.slug, qty: Number(line.quantity || 1), bundleId: Number(b.id), bundleSlug: b.slug });
        }
        persistCart(next);
        setDrawer(true);
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

    const syncAccount = async (u: Customer) => {
        setUser(u);
        write('wolfe_user', u);
        try {
            const [serverCart, serverWishes] = await Promise.all([api.cart.get(u.id), api.wishlist.get(u.id).catch(() => [])]);
            const mapped = serverCart.map((x: any) => {
                const p = products.find(p => p.serverId === x.productId);
                return p ? { id: p.id, qty: x.quantity } : null;
            }).filter(Boolean) as CartItem[];
            const localSpecial = cart.filter(x => !!x.configurationToken || x.bundleId || x.variantId);
            const merged = [...mapped, ...localSpecial];
            if (merged.length || serverCart.length === 0)
                persistCart(merged);
            setWishes(serverWishes);
            write('wolfe_wishlist', serverWishes);
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

        if (user && !variant) {
            const item = next.find(same)!;
            try {
                await api.cart.put(user.id, id, item.qty);
            } catch { }
        }
    };

    const addConfigured = async (id: string, configurationToken: string) => {
        const next = cart.some(x => x.id === id && x.configurationToken === configurationToken)
            ? cart.map(x => (x.id === id && x.configurationToken === configurationToken ? { ...x, qty: x.qty + 1 } : x))
            : [...cart, { id, qty: 1, configurationToken }];
        persistCart(next);
        setDrawer(true);
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
                : cart.map(x => (x.bundleId === bundleId ? { ...x, qty: n } : x))
            : n < 1
            ? cart.filter(x => !same(x))
            : cart.map(x => (same(x) ? { ...x, qty: n } : x));

        persistCart(next);

        if (user && !configurationToken && !bundleId && !variantId) {
            try {
                if (n < 1) await api.cart.remove(user.id, id);
                else await api.cart.put(user.id, id, n);
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
                <Route path="/profile" element={<ProfilePage user={user} onSaved={u => { setUser(u); write('wolfe_user', u); }} />} />
                <Route path="/addresses" element={<AddressesPage user={user} />} />
                <Route path="/consultation" element={<Consultation />} />
                <Route path="/quote" element={<QuoteRequest user={user} />} />
                <Route path="/custom-design" element={<CustomDesign user={user} />} />
                <Route path="/catalogue" element={<SimpleContent eyebrow="Wolfe catalogue" title="Explore the master collection." copy="Request the latest product catalogue for finishes, dimensions and project planning." />} />
                <Route path="/journal" element={<SimpleContent eyebrow="Journal" title="Ideas for considered spaces." copy="A growing editorial space for materials, rooms, finishes and the small details that make a home feel like yours." />} />
                <Route path="/privacy-policy" element={<PrivacyPolicy />} />
                <Route path="/terms" element={<Terms />} />
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

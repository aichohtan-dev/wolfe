import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { Routes, Route } from 'react-router-dom';
import { ArrowRight, Heart, Menu, Search, ShoppingBag, X, User, Minus, Plus, Trash2, Package, ShieldCheck, ChevronDown, SlidersHorizontal, Truck, RotateCcw, MessageCircle, Star } from 'lucide-react';
import { products } from './data';
let activeProductSlugs: Set<string> | null = null;
import { api, type Customer } from './api';
type CartItem = {
    id: string;
    qty: number;
    configurationToken?: string;
    bundleId?: number;
    bundleSlug?: string;
};
const money = (n: number) => `₹${n.toLocaleString('en-IN')}`;
const read = (k: string, f: any) => { try {
    return JSON.parse(localStorage.getItem(k) || JSON.stringify(f));
}
catch {
    return f;
} };
const write = (k: string, v: any) => localStorage.setItem(k, JSON.stringify(v));
const editorial = [
    { title: 'Handles', copy: 'Refined pulls for kitchens, wardrobes and furniture.', image: 'https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1200&q=85', category: 'Handles' },
    { title: 'Knobs', copy: 'Small details with a distinct point of view.', image: 'https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=1200&q=85', category: 'Knobs' },
    { title: 'Hooks', copy: 'Functional forms designed to live beautifully.', image: 'https://images.unsplash.com/photo-1617104678098-de229db51175?auto=format&fit=crop&w=1200&q=85', category: 'Hooks' },
];
function Header({ cartCount, wishCount, onCart }: {
    cartCount: number;
    wishCount: number;
    onCart: () => void;
}) {
    const [open, setOpen] = useState(false);
    const [searchOpen, setSearchOpen] = useState(false);
    const [search, setSearch] = useState('');
    const [suggestions, setSuggestions] = useState<any[]>([]);
    const [recentSearches, setRecentSearches] = useState<string[]>(() => read('wolfe_recent_searches', []));
    useEffect(() => { let live = true; const q = search.trim(); if (q.length < 2) {
        setSuggestions([]);
        return;
    } const t = setTimeout(() => api.productSuggestions(q).then(x => { if (live)
        setSuggestions(x); }).catch(() => { }), 180); return () => { live = false; clearTimeout(t); }; }, [search]);
    const submitSearch = (q: string) => { const term = q.trim(); if (!term)
        return; const next = [term, ...recentSearches.filter(x => x.toLowerCase() !== term.toLowerCase())].slice(0, 6); setRecentSearches(next); write('wolfe_recent_searches', next); setSearchOpen(false); setSearch(''); window.location.href = `/shop?q=${encodeURIComponent(term)}`; };
    const nav = (x: string) => x === 'New In' ? '/shop' : `/shop?category=${x}`;
    return <>
  <div className="wolfe-topbar">Complimentary delivery on orders above ₹2,500 <span>•</span> Easy returns <span>•</span> WhatsApp us for design help</div>
  <header className="site-header">
   <div className="container-w header-main">
    <button className="icon-btn mobile-menu" onClick={() => setOpen(!open)} aria-label="Menu">{open ? <X /> : <Menu />}</button>
    <Link to="/" className="brand"><img src="/wolfe-logo.png" alt="Wolfe — The Jewel of Villa"/></Link>
    <nav className={`${open ? 'mobile-nav-open' : 'mobile-nav-closed'} main-nav`}>
      {['New In', 'Handles', 'Knobs', 'Hooks'].map(x => <Link key={x} onClick={() => setOpen(false)} to={nav(x)}>{x}</Link>)}
      <details><summary>Explore <ChevronDown size={14}/></summary><div className="nav-menu"><Link to="/shop?category=Collections">Collections</Link><Link to="/consultation">Consultation</Link><Link to="/custom-design">Custom design</Link><Link to="/quote">Project quote</Link><Link to="/bundles">Bundles</Link><Link to="/catalogue">Catalogue</Link><Link to="/journal">Journal</Link></div></details>
    </nav>
    <div className="header-actions">
      <button type="button" className="header-search-button" onClick={() => setSearchOpen(true)} aria-label="Search"><Search size={19}/></button>
      <Link to="/wishlist" className="relative" aria-label="Wishlist"><Heart size={19}/>{wishCount > 0 && <Badge n={wishCount}/>}</Link>
      <Link to="/account" aria-label="Account"><User size={19}/></Link>
      <button onClick={onCart} className="relative" aria-label="Shopping bag"><ShoppingBag size={19}/>{cartCount > 0 && <Badge n={cartCount}/>}</button>
    </div>
   </div>
  </header>
  {searchOpen && <div className="search-panel"><div className="container-w search-panel-inner"><form onSubmit={e => { e.preventDefault(); submitSearch(search); }} className="search-form"><Search size={18}/><input autoFocus value={search} onChange={e => setSearch(e.target.value)} placeholder="Search handles, knobs, hooks, finishes…"/><button type="button" onClick={() => { setSearchOpen(false); setSearch(''); }} aria-label="Close search"><X size={18}/></button></form>{search.trim().length < 2 && recentSearches.length > 0 && <div className="search-section"><span className="search-label">Recent searches</span><div className="search-chips">{recentSearches.map(x => <button key={x} type="button" onClick={() => submitSearch(x)}>{x}</button>)}</div></div>}{suggestions.length > 0 && <div className="search-results"><span className="search-label">Suggestions</span>{suggestions.map(x => <Link key={x.id} to={`/product/${x.slug}`} onClick={() => submitSearch(x.name)} className="search-result"><img src={x.imageUrl || '/wolfe-logo.png'} alt=""/><span><strong>{x.name}</strong><small>{x.category} · {money(Number(x.price))}</small></span><ArrowRight size={15}/></Link>)}</div>}{search.trim().length >= 2 && !suggestions.length && <p className="search-empty">No matching products yet.</p>}</div></div>}
 </>;
}
function Badge({ n }: {
    n: number;
}) { return <span className="badge">{n}</span>; }
function ProductCard({ p, onAdd, onWish, wishes, onCompare, compared = false, featured = false }: {
    p: typeof products[number];
    onAdd: (id: string) => void;
    onWish: (id: string) => void;
    wishes: string[];
    onCompare: (id: string) => void;
    compared?: boolean;
    featured?: boolean;
}) {
    return <article className={`product-card ${featured ? 'product-featured' : ''}`}>
   <div className="product-media">
    {p.featured && <span className="product-badge">Featured</span>}
    <Link to={`/product/${p.id}`} className="product-image"><img src={p.image} alt={p.name}/></Link>
    <button onClick={() => onWish(p.id)} className="wish-btn" aria-label="Add to wishlist"><Heart size={18} fill={wishes.includes(p.id) ? 'currentColor' : 'none'}/></button>
    <button onClick={() => onAdd(p.id)} className="quick-add">Add to bag</button><button onClick={() => onCompare(p.id)} className="compare-add" aria-pressed={compared}>{compared ? 'Compared' : 'Compare'}</button>
   </div>
   <div className="product-info">
    <div><Link to={`/product/${p.id}`} className="product-name">{p.name}</Link><p className="product-meta">{p.finish}</p></div>
    <p className="product-price">{money(p.price)}</p>
   </div>
 </article>;
}
function ProductGrid({ items, onAdd, onWish, wishes, onCompare = () => { }, compared = [] }: {
    items: typeof products;
    onAdd: (id: string) => void;
    onWish: (id: string) => void;
    wishes: string[];
    onCompare?: (id: string) => void;
    compared?: string[];
}) { const visible = activeProductSlugs ? items.filter(p => activeProductSlugs!.has(p.id)) : items; return <div className="product-grid">{visible.map(p => <ProductCard key={p.id} p={p} onAdd={onAdd} onWish={onWish} wishes={wishes} onCompare={onCompare} compared={compared.includes(p.id)}/>)}</div>; }
function VisualHero() { const [items, setItems] = useState<any[]>([]); const [index, setIndex] = useState(0); useEffect(() => { api.visualContent('HERO').then(setItems).catch(() => setItems([])); }, []); if (!items.length)
    return null; const x = items[index % items.length]; return <section className="visual-hero"><div className="visual-hero-media">{x.mediaType === 'VIDEO' ? <video src={x.mediaUrl} poster={x.posterUrl || undefined} autoPlay muted loop playsInline controls={false}/> : <img src={x.mediaUrl} alt={x.title}/>}</div><div className="visual-hero-copy"><p className="eyebrow">Wolfe visual story</p><h2>{x.title}</h2>{x.subtitle && <p>{x.subtitle}</p>}{x.linkUrl && <a className="btn btn-orange" href={x.linkUrl}>Explore</a>}</div>{items.length > 1 && <div className="visual-hero-dots">{items.map((_: any, i: number) => <button type="button" key={i} aria-label={`Show visual ${i + 1}`} className={i === index ? 'active' : ''} onClick={() => setIndex(i)}/>)}</div>}</section>; }
function AccessoryConfigurator({ slug, baseImage, user, productId, onAddConfigured }: {
    slug: string;
    baseImage: string;
    user: Customer | null;
    productId: number;
    onAddConfigured: (id: string, token: string) => void;
}) { const [items, setItems] = useState<any[]>([]); const [selected, setSelected] = useState<any | null>(null); const [saved, setSaved] = useState(''); const [room, setRoom] = useState('light'); useEffect(() => { api.accessories(slug).then(setItems).catch(() => setItems([])); }, [slug]); if (!items.length)
    return null; const save = async () => { try {
    const c = await api.experience.saveConfiguration({ customerId: user?.id || null, productId, accessoryId: selected?.id || null, configJson: JSON.stringify({ accessoryId: selected?.id || null, room, accessorySku: selected?.sku || null, accessoryName: selected?.name || null }) });
    setSaved(`${location.origin}/config/${c.shareToken}`);
    return c;
}
catch {
    setSaved('Could not save configuration');
    return null;
} }; return <section className="configurator"><div className="configurator-head"><div><p className="eyebrow">Wolfe visual configurator</p><h2>Build your look.</h2><p>Try handles, knobs and accessories on the piece, then save or share the configuration.</p></div>{selected && <button className="btn btn-light" onClick={() => setSelected(null)}>Clear</button>}</div><div className={`configurator-stage room-${room}`}><img src={baseImage} alt="Product configurator"/><div className="configurator-overlay" aria-live="polite">{selected && <img src={selected.overlayUrl} alt={selected.name} style={{ left: `${selected.x}%`, top: `${selected.y}%`, transform: `translate(-50%,-50%) scale(${selected.scale})` }}/>}</div></div><div className="configurator-toolbar"><label>Room <select value={room} onChange={e => setRoom(e.target.value)} className="select-field"><option value="light">Light</option><option value="warm">Warm</option><option value="dark">Dark</option></select></label><button className="btn btn-orange" onClick={async () => { const c = await save(); if (c)
    onAddConfigured(slug, c.shareToken); }} disabled={!selected}>Add configured to bag</button></div>{saved && <p className="success-message">{saved}</p>}<div className="configurator-options">{items.map(a => <button type="button" key={a.id} className={selected?.id === a.id ? 'selected' : ''} onClick={() => setSelected(a)}><span className="swatch"><img src={a.overlayUrl} alt=""/></span><strong>{a.name}</strong><small>{a.type}{a.price != null ? ` · ₹${Number(a.price).toLocaleString('en-IN')}` : ''}</small></button>)}</div></section>; }
function SpinViewer({ slug }: {
    slug: string;
}) { const [frames, setFrames] = useState<any[]>([]); const [index, setIndex] = useState(0); useEffect(() => { api.experience.spin(slug).then(setFrames).catch(() => setFrames([])); }, [slug]); if (frames.length < 2)
    return null; return <section className="spin-viewer"><div><p className="eyebrow">360° view</p><h2>See every angle.</h2><p>Rotate through the product frames.</p></div><img src={frames[index].imageUrl} alt="Product 360 degree view"/><input aria-label="Rotate product" type="range" min="0" max={frames.length - 1} value={index} onChange={e => setIndex(Number(e.target.value))}/><div className="spin-actions"><button className="btn btn-light" onClick={() => setIndex((index - 1 + frames.length) % frames.length)}>Previous</button><button className="btn btn-light" onClick={() => setIndex((index + 1) % frames.length)}>Next</button></div></section>; }
function Comparison({ items, onAdd, wishes, onWish, onCompare, compared }: {
    items: typeof products;
    onAdd: (id: string) => void;
    wishes: string[];
    onWish: (id: string) => void;
    onCompare: (id: string) => void;
    compared: string[];
}) { return <main className="container-w section"><p className="eyebrow">Product comparison</p><h1>Compare the details.</h1><p>{items.length < 2 ? 'Select at least two products from the shop to compare.' : `Comparing ${items.length} selected pieces.`}</p><div className="product-grid">{items.map(p => <article className="admin-panel" key={p.id}><img src={p.image} alt={p.name} style={{ width: '100%', aspectRatio: '1', objectFit: 'cover' }}/><h2>{p.name}</h2><p>{money(p.price)}</p><p><strong>Finish</strong> · {p.finish}</p><p><strong>Material</strong> · {p.material}</p><p><strong>Colour</strong> · {p.color}</p><p><strong>Style</strong> · {p.style}</p><button className="btn btn-orange" onClick={() => onAdd(p.id)}>Add to bag</button><button className="btn btn-light" onClick={() => onWish(p.id)}>{wishes.includes(p.id) ? 'Saved' : 'Save'}</button><button className="btn btn-light" onClick={() => onCompare(p.id)}>Remove comparison</button></article>)}</div></main>; }
function Home({ onAdd, onWish, wishes, onCompare, compared }: {
    onAdd: (id: string) => void;
    onWish: (id: string) => void;
    wishes: string[];
    onCompare: (id: string) => void;
    compared: string[];
}) {
    return <main>
 <section className="hero">
   <div className="hero-image"></div>
   <div className="hero-overlay"><p className="eyebrow">Architectural hardware for considered spaces</p><h1>Details make<br />the room.</h1><p>Premium handles, knobs and hooks designed to bring quiet character to kitchens, wardrobes and living spaces.</p><Link to="/shop" className="btn btn-orange">Explore the collection <ArrowRight size={16}/></Link></div>
 </section>
 <VisualHero />
 <section className="section container-w">
  <SectionHead eyebrow="Shop by category" title="The details, curated." link="View all" to="/shop"/>
  <div className="category-grid">{editorial.map(x => <Link to={`/shop?category=${x.category}`} className="category-card" key={x.title}><img src={x.image} alt=""/><div><span>{x.title}</span><p>{x.copy}</p><ArrowRight size={18}/></div></Link>)}</div>
 </section>
 <section className="section section-soft">
   <div className="container-w"><SectionHead eyebrow="Featured collection" title="Made to be noticed." link="Shop all" to="/shop"/><ProductGrid items={products.slice(0, 4)} onAdd={onAdd} onWish={onWish} wishes={wishes} onCompare={onCompare} compared={compared}/></div>
 </section>
 <section className="inspiration container-w">
   <div className="inspiration-copy"><p className="eyebrow">Inspiration starts here</p><h2>Moodboards for spaces with character.</h2><p>Explore considered combinations of finishes, forms and materials, and find the details that belong in your space.</p><Link to="/journal" className="text-link">Explore inspiration <ArrowRight size={16}/></Link></div>
   <div className="inspiration-image"><img src="https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1600&q=85" alt="Warm contemporary interior"/></div>
 </section>
 <section className="section container-w"><SectionHead eyebrow="Shop our favourites" title="Pieces worth living with."/><ProductGrid items={products.slice(2, 6)} onAdd={onAdd} onWish={onWish} wishes={wishes} onCompare={onCompare} compared={compared}/></section>
 <section className="service-banner"><div className="container-w service-inner"><div><p className="eyebrow">Need a second opinion?</p><h2>Let's choose the details together.</h2><p>Book a complimentary consultation for finish, sizing and quantity guidance.</p></div><Link to="/consultation" className="btn btn-light">Book a consultation <ArrowRight size={16}/></Link></div></section>
 <section className="catalogue container-w"><div><p className="eyebrow">Wolfe catalogue</p><h2>See the complete collection.</h2><p>Browse finishes, forms and specifications in one considered catalogue.</p></div><Link to="/catalogue" className="btn btn-orange">Request catalogue</Link></section>
 <section className="trust-section"><div className="container-w"><p className="eyebrow center">The Wolfe assurance</p><div className="trust-grid"><Trust icon={<ShieldCheck />} title="Secure transactions"/><Trust icon={<Truck />} title="Reliable dispatch"/><Trust icon={<RotateCcw />} title="Easy returns"/><Trust icon={<MessageCircle />} title="Design support"/></div></div></section>
 <section className="testimonials container-w"><SectionHead eyebrow="Hear from our community" title="Details that stay with you."/><div className="testimonial-grid">{['Beautiful quality and beautifully packed. The finish is exactly what we needed.', 'The collection makes it very easy to coordinate a whole kitchen without it feeling too uniform.', 'Helpful advice, fast communication and pieces that feel much more premium in person.'].map((x, i) => <div className="testimonial" key={i}><div className="stars">{[1, 2, 3, 4, 5].map(n => <Star key={n} size={14} fill="currentColor"/>)}</div><p>“{x}”</p><span>Verified customer</span></div>)}</div></section>
 <section className="quote-section"><div className="container-w quote-inner"><div><p className="eyebrow">Create your own quote</p><h2>A simpler way to plan your project.</h2><p>Add products to your bag, then request a project quote from our team.</p></div><Link to="/quote" className="btn btn-orange">Start a quote <ArrowRight size={16}/></Link></div></section>
 </main>;
}
function Trust({ icon, title }: {
    icon: React.ReactNode;
    title: string;
}) { return <div className="trust-item">{icon}<span>{title}</span></div>; }
function SectionHead({ eyebrow, title, link, to }: {
    eyebrow: string;
    title: string;
    link?: string;
    to?: string;
}) { return <div className="section-head"><div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div>{link && to && <Link to={to} className="text-link">{link} <ArrowRight size={15}/></Link>}</div>; }
function Shop({ onAdd, onWish, wishes, onCompare, compared }: {
    onAdd: (id: string) => void;
    onWish: (id: string) => void;
    wishes: string[];
    onCompare: (id: string) => void;
    compared: string[];
}) {
    const loc = useLocation();
    const params = new URLSearchParams(loc.search);
    const [q, setQ] = useState(params.get('q') || '');
    const [cat, setCat] = useState(params.get('category') || 'All');
    const [finish, setFinish] = useState('All');
    const [material, setMaterial] = useState('All');
    const [color, setColor] = useState('All');
    const [style, setStyle] = useState('All');
    const [sort, setSort] = useState('featured');
    const [filters, setFilters] = useState(false);
    const [options, setOptions] = useState<any>({ categories: [], materials: [], colors: [], styles: [], finishes: [] });
    const [items, setItems] = useState<typeof products>([]);
    useEffect(() => { api.productFilters().then(setOptions).catch(() => { }); }, []);
    useEffect(() => { let live = true; api.products(cat === 'All' ? undefined : q.trim() || undefined, undefined, finish === 'All' ? undefined : finish, false, material === 'All' ? undefined : material, color === 'All' ? undefined : color, style === 'All' ? undefined : style, sort).then(rows => { if (live)
        setItems(rows.map(s => ({ id: s.slug, serverId: Number(s.id), name: s.name, category: s.category, price: Number(s.price), image: s.imageUrl || '/wolfe-logo.png', description: s.description || '', finish: s.finish, material: s.material || 'Metal', color: s.color || 'Brass', style: s.style || 'Modern', featured: s.featured === true, media: String(s.mediaUrls || '').split(/\r?\n|,/).map((x: string) => x.trim()).filter(Boolean) }))); }).catch(() => { if (live)
        setItems([]); }); return () => { live = false; }; }, [q, cat, finish, material, color, style, sort]);
    const cats = ['All', ...(options.categories || [])], finishes = ['All', ...(options.finishes || [])], materials = ['All', ...(options.materials || [])], colors = ['All', ...(options.colors || [])], styles = ['All', ...(options.styles || [])];
    return <main><div className="collection-hero"><div className="container-w"><p className="eyebrow">Wolfe collections</p><h1>Hardware with character.</h1><p>Explore handles, knobs and hooks across considered finishes, materials and forms.</p></div></div><div className="container-w shop-layout"><aside className={`filters ${filters ? 'filters-open' : ''}`}><div className="filter-title"><strong>Filter by</strong><button onClick={() => setFilters(false)} className="mobile-only"><X size={18}/></button></div><FilterBlock title="Category"><div className="filter-list">{cats.map((c: string) => <button key={c} onClick={() => setCat(c)} className={cat === c ? 'active' : ''}>{c}</button>)}</div></FilterBlock><FilterBlock title="Finish"><div className="filter-list">{finishes.map((c: string) => <button key={c} onClick={() => setFinish(c)} className={finish === c ? 'active' : ''}>{c}</button>)}</div></FilterBlock><FilterBlock title="Material"><div className="filter-list">{materials.map((c: string) => <button key={c} onClick={() => setMaterial(c)} className={material === c ? 'active' : ''}>{c}</button>)}</div></FilterBlock><FilterBlock title="Colour"><div className="filter-list">{colors.map((c: string) => <button key={c} onClick={() => setColor(c)} className={color === c ? 'active' : ''}>{c}</button>)}</div></FilterBlock><FilterBlock title="Style"><div className="filter-list">{styles.map((c: string) => <button key={c} onClick={() => setStyle(c)} className={style === c ? 'active' : ''}>{c}</button>)}</div></FilterBlock></aside><div className="shop-results"><div className="shop-toolbar"><button className="filter-mobile-btn" onClick={() => setFilters(true)}><SlidersHorizontal size={16}/> Filters</button><span>{items.length} products</span><div className="shop-controls"><div className="shop-search-wrap"><Search size={16}/><input value={q} onChange={e => setQ(e.target.value)} onKeyDown={e => { if (e.key === 'Enter' && q.trim()) {
        const next = [q.trim(), ...read('wolfe_recent_searches', []).filter((x: string) => x.toLowerCase() !== q.trim().toLowerCase())].slice(0, 6);
        write('wolfe_recent_searches', next);
    } }} placeholder="Search products, materials, colours…"/></div><select value={sort} onChange={e => setSort(e.target.value)}><option value="featured">Featured</option><option value="price_asc">Price low to high</option><option value="price_desc">Price high to low</option><option value="name_asc">Name A–Z</option></select></div></div>{items.length ? <ProductGrid items={items} onAdd={onAdd} onWish={onWish} wishes={wishes} onCompare={onCompare} compared={compared}/> : <p className="empty-state">No products found.</p>}</div></div></main>;
}
function FilterBlock({ title, children }: {
    title: string;
    children: React.ReactNode;
}) { return <div className="filter-block"><h3>{title}</h3>{children}</div>; }
function ReviewPanel({ slug, user }: {
    slug: string;
    user: Customer | null;
}) {
    const [data, setData] = useState<any>({ average: 0, count: 0, reviews: [] });
    const [rating, setRating] = useState(5);
    const [review, setReview] = useState('');
    const [sent, setSent] = useState(false);
    const [error, setError] = useState('');
    useEffect(() => { api.reviews.get(slug).then(setData).catch(() => { }); }, [slug, sent]);
    const submit = async (e: any) => { e.preventDefault(); if (!user) {
        setError('Please sign in to leave a review.');
        return;
    } try {
        await api.reviews.create(slug, { rating, review });
        setSent(true);
        setReview('');
        setError('');
    }
    catch (err: any) {
        setError(err.message || 'Unable to submit review');
    } };
    return <section className="container-w section review-section"><SectionHead eyebrow="Customer reviews" title={`${data.average.toFixed(1)} / 5 · ${data.count} reviews`}/>{data.reviews.map((r: any) => <div className="testimonial" key={r.id}><div className="stars">{[1, 2, 3, 4, 5].map(n => <Star key={n} size={14} fill={n <= r.rating ? 'currentColor' : 'none'}/>)}</div><p>{r.review}</p></div>)}<form onSubmit={submit} className="review-form"><h3>Share your experience</h3><select value={rating} onChange={e => setRating(Number(e.target.value))} className="field"><option value={5}>5 stars</option><option value={4}>4 stars</option><option value={3}>3 stars</option><option value={2}>2 stars</option><option value={1}>1 star</option></select><textarea required minLength={10} maxLength={1000} value={review} onChange={e => setReview(e.target.value)} placeholder="Tell us about the product" className="field textarea"/><button className="btn btn-orange">Submit review</button>{sent && <p className="checkout-login-note">Review submitted for approval.</p>}{error && <p className="error-message">{error}</p>}</form></section>;
}
function QuoteRequest({ user }: {
    user: Customer | null;
}) { const [message, setMessage] = useState(''); const [sent, setSent] = useState(false); const submit = async (e: any) => { e.preventDefault(); if (!user)
    return; await api.quotes.create(user.id, { message }); setSent(true); setMessage(''); }; return <main className="container-w narrow-page"><p className="eyebrow">Project quote</p><h1>Tell us about your project.</h1>{!user ? <p>Please sign in to request a quote.</p> : sent ? <div className="success-box"><h2>Request received.</h2><p>Our team will contact you with the next steps.</p></div> : <form onSubmit={submit} className="checkout-form"><textarea required minLength={10} maxLength={1000} value={message} onChange={e => setMessage(e.target.value)} placeholder="Products, quantities, room, timeline or anything else we should know" className="field textarea"/><button className="btn btn-orange">Request quote</button></form>}</main>; }
function CustomDesign({ user }: {
    user: Customer | null;
}) { const [form, setForm] = useState({ projectName: '', requirements: '', referenceImageUrl: '' }); const [sent, setSent] = useState(false); const submit = async (e: any) => { e.preventDefault(); if (!user)
    return; await api.customDesign.create(user.id, form); setSent(true); }; return <main className="container-w narrow-page"><p className="eyebrow">Custom design</p><h1>Make it yours.</h1>{!user ? <p>Please sign in to submit a custom design request.</p> : sent ? <div className="success-box"><h2>Design request received.</h2><p>Our team will review your brief and reference image.</p></div> : <form onSubmit={submit} className="checkout-form"><input required maxLength={160} value={form.projectName} onChange={e => setForm({ ...form, projectName: e.target.value })} placeholder="Project name" className="field"/><textarea maxLength={2000} value={form.requirements} onChange={e => setForm({ ...form, requirements: e.target.value })} placeholder="Dimensions, finish, quantity and design requirements" className="field textarea"/><input type="url" maxLength={2000} value={form.referenceImageUrl} onChange={e => setForm({ ...form, referenceImageUrl: e.target.value })} placeholder="Reference image URL (optional)" className="field"/><button className="btn btn-orange">Submit design request</button></form>}</main>; }
function useSeo(title: string, description: string) { useEffect(() => { document.title = title; const meta = document.querySelector('meta[name=description]'); if (meta)
    meta.setAttribute('content', description); let canonical = document.querySelector('link[rel=canonical]') as HTMLLinkElement | null; if (!canonical) {
    canonical = document.createElement('link');
    canonical.rel = 'canonical';
    document.head.appendChild(canonical);
} canonical.href = window.location.origin + window.location.pathname; return () => { }; }, [title, description]); }
function ProductPage({ onAdd, onAddConfigured, onWish, wishes, user }: {
    onAdd: (id: string) => void;
    onAddConfigured: (id: string, token: string) => void;
    onWish: (id: string) => void;
    wishes: string[];
    user: Customer | null;
}) { const { id } = useParams(); const p = products.find(x => x.id === id); useSeo(p ? `${p.name} | Wolfe — The Jewel of Villa` : 'Product | Wolfe', p ? `${p.name} — premium ${p.category.toLowerCase()} from Wolfe. ${p.description}` : 'Explore premium architectural hardware from Wolfe.'); const [variants, setVariants] = useState<any[]>([]); const [related, setRelated] = useState<any[]>([]); const [recent, setRecent] = useState<any[]>([]); useEffect(() => { if (p) {
    api.productVariants(p.id).then(setVariants).catch(() => setVariants([]));
    api.relatedProducts(p.id).then(setRelated).catch(() => setRelated([]));
    if (user && p.serverId) {
        api.experience.touchRecent(user.id, p.serverId).catch(() => { });
        api.experience.recent(user.id).then(setRecent).catch(() => setRecent([]));
    }
    else {
        const ids = read('wolfe_recently_viewed', []).filter((x: string) => x !== p.id);
        const next = [p.id, ...ids].slice(0, 8);
        write('wolfe_recently_viewed', next);
        setRecent(next.map((x: string) => products.find(y => y.id === x)).filter(Boolean));
    }
} }, [p?.id, user?.id]); if (!p)
    return <main className="container-w section">Product not found.</main>; const mapped = (x: any) => ({ id: x.slug, serverId: Number(x.id), name: x.name, category: x.category, price: Number(x.price), image: x.imageUrl || '/wolfe-logo.png', description: x.description || '', finish: x.finish, material: x.material || 'Metal', color: x.color || 'Brass', style: x.style || 'Modern', featured: x.featured === true, media: [] }); const recentItems = user ? recent.map(mapped) : recent as any; return <main className="product-page container-w"><div className="product-detail-image"><img src={p.image} alt={p.name}/>{p.media?.length > 0 && <div className="product-media-gallery">{p.media.map((src: string, i: number) => <img key={src + i} src={src} alt={`${p.name} ${i + 1}`} loading="lazy"/>)}</div>}</div><div className="product-detail-copy"><p className="eyebrow">{p.category}</p><h1>{p.name}</h1><div className="detail-price">{money(p.price)}</div><p className="detail-description">{p.description}</p><div className="option-block"><span>Finish</span><div className="option-value">{p.finish}</div></div><div className="option-block"><span>Material / Colour / Style</span><div className="option-value">{p.material} · {p.color} · {p.style}</div></div>{variants.length > 0 && <div className="option-block"><span>{variants[0].optionName}</span><div className="option-row">{variants.map((v: any) => <button key={v.id} className="selected">{v.optionValue}{v.priceOverride ? ` · ${money(Number(v.priceOverride))}` : ''}</button>)}</div></div>}<div className="detail-actions"><button onClick={() => onAdd(p.id)} className="btn btn-orange">Add to bag</button><button onClick={() => onWish(p.id)} className="btn btn-light"><Heart fill={wishes.includes(p.id) ? 'currentColor' : 'none'}/> {wishes.includes(p.id) ? 'Saved' : 'Save'}</button></div></div><ReviewPanel slug={p.id} user={user}/><AccessoryConfigurator slug={p.id} baseImage={p.image} user={user} productId={p.serverId || 0} onAddConfigured={onAddConfigured}/><SpinViewer slug={p.id}/><section className="room-visualizer"><div><p className="eyebrow">Room visualizer</p><h2>Preview the detail in a room.</h2><p>Use the accessory configuration with a room tone now; the same product asset can later power 3D and AR.</p></div><div className="room-preview room-preview-demo"><img src={p.image} alt={`${p.name} room preview`}/></div></section>{related.length > 0 && <section className="section"><SectionHead eyebrow="You may also like" title="Related pieces."/><ProductGrid items={related.map(mapped) as any} onAdd={onAdd} onWish={onWish} wishes={wishes} onCompare={() => { }} compared={[]}/></section>}{recentItems.length > 1 && <section className="section"><SectionHead eyebrow="Recently viewed" title="Pieces you explored."/><ProductGrid items={recentItems.filter((x: any) => x.id !== p.id) as any} onAdd={onAdd} onWish={onWish} wishes={wishes} onCompare={() => { }} compared={[]}/></section>}</main>; }
function Wishlist({ wishes, onWish, onAdd }: {
    wishes: string[];
    onWish: (id: string) => void;
    onAdd: (id: string) => void;
}) { const user = read<Customer | null>('wolfe_user', null); const items = products.filter(p => wishes.includes(p.id)); const clear = async () => { if (user)
    await api.wishlist.clear(user.id).catch(() => { }); wishes.forEach(onWish); }; return <main className="container-w section"><p className="eyebrow">Saved pieces</p><div className="admin-head"><div><h1>Wishlist</h1><p>Keep your considered pieces together.</p></div>{items.length > 0 && <button className="btn btn-light" onClick={clear}>Clear wishlist</button>}</div><div className="wishlist-grid">{items.length ? <ProductGrid items={items} onAdd={onAdd} onWish={onWish} wishes={wishes}/> : <p className="empty-state">Your wishlist is empty.</p>}</div></main>; }
function Account({ user, onLogin, onLogout }: {
    user: Customer | null;
    onLogin: (u: Customer) => void;
    onLogout: () => void;
}) { const [email, setEmail] = useState(''); const [password, setPassword] = useState(''); const [busy, setBusy] = useState(false); const [error, setError] = useState(''); const login = async (e: any) => { e.preventDefault(); setBusy(true); setError(''); try {
    const result = await api.login(email, password);
    localStorage.setItem('wolfe_access_token', result.accessToken);
    localStorage.setItem('wolfe_refresh_token', result.refreshToken);
    onLogin(result.customer);
}
catch (err: any) {
    setError(err.message || 'Invalid credentials');
}
finally {
    setBusy(false);
} }; if (user)
    return <main className="container-w section"><p className="eyebrow">Customer account</p><h1>Hello, {user.name}</h1><div className="account-grid"><Link to="/orders" className="account-card"><Package /><h2>Orders</h2><p>Track your purchases.</p></Link><Link to="/profile" className="account-card"><User /><h2>Profile</h2><p>Update your details.</p></Link><Link to="/addresses" className="account-card"><Truck /><h2>Addresses</h2><p>Manage saved delivery addresses.</p></Link><Link to="/wishlist" className="account-card"><Heart /><h2>Wishlist</h2></Link><button onClick={onLogout} className="account-card"><ShieldCheck /><h2>Sign out</h2></button></div></main>; return <main className="container-w narrow-page"><p className="eyebrow">Customer account</p><h1>Sign in</h1><p>Continue to manage orders and saved pieces.</p><form onSubmit={login} className="login-form"><input required type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Email address" className="field"/><input required minLength={8} type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" className="field"/>{error && <p className="error-message">{error}</p>}<button disabled={busy} className="btn btn-orange full">{busy ? 'Signing in…' : 'Sign in'}</button></form></main>; }
function Orders({ user }: {
    user: Customer | null;
}) { const [orders, setOrders] = useState<any[]>([]); const [loading, setLoading] = useState(!!user); useEffect(() => { if (!user) {
    setOrders([]);
    setLoading(false);
    return;
} setLoading(true); fetch(`${(import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1').replace(/\/$/, '')}/orders/customer/${user.id}`).then(r => r.json()).then(setOrders).catch(() => setOrders([])).finally(() => setLoading(false)); }, [user]); return <main className="container-w section"><p className="eyebrow">Customer account</p><h1>Orders</h1>{!user ? <p className="empty-state">Sign in to view your orders.</p> : loading ? <p className="empty-state">Loading orders…</p> : orders.length ? <div className="orders-list">{orders.map(o => <Link to={`/orders/${o.id}`} className="order-card" key={o.id}><div><strong>{o.id}</strong><p>{new Date(o.createdAt).toLocaleDateString('en-IN')}</p></div><span>{o.status}</span><strong>{money(o.total / 100)}</strong></Link>)}</div> : <p className="empty-state">No orders yet.</p>}</main>; }
function Consultation() { const [sent, setSent] = useState(false); return <main className="consultation-page"><div className="consultation-image"></div><div className="consultation-copy"><p className="eyebrow">Design service</p><h1>Find the right details for your space.</h1><p>Not sure which pieces, finish or size will work? Book a complimentary consultation with the Wolfe team.</p>{sent ? <div className="success-box"><h2>Request received.</h2><p>We’ll follow up with guidance for your project.</p></div> : <form onSubmit={e => { e.preventDefault(); setSent(true); }} className="consultation-form"><input required placeholder="Name" className="field"/><input required type="email" placeholder="Email" className="field"/><input placeholder="Project / room" className="field"/><textarea required placeholder="Tell us what you're working on" className="field textarea"/><button className="btn btn-orange">Request consultation</button></form>}</div></main>; }
function SimpleContent({ title, eyebrow, copy }: {
    title: string;
    eyebrow: string;
    copy: string;
}) { return <main className="container-w narrow-page"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{copy}</p><Link to="/shop" className="btn btn-orange">Shop collection <ArrowRight size={16}/></Link></main>; }
function useConfigurationDetails(items: CartItem[]) {
    const [details, setDetails] = useState<Record<string, any>>({});
    const tokens = useMemo(() => Array.from(new Set(items.map(i => i.configurationToken).filter(Boolean) as string[])).sort().join('|'), [items]);
    useEffect(() => { let live = true; const list = tokens ? tokens.split('|') : []; if (!list.length) {
        setDetails({});
        return;
    } Promise.all(list.map(async (token) => { try {
        return [token, await api.experience.getConfiguration(token)] as const;
    }
    catch {
        return [token, null] as const;
    } })).then(entries => { if (live)
        setDetails(Object.fromEntries(entries.filter(([, v]) => v))); }); return () => { live = false; }; }, [tokens]);
    return details;
}
function cartUnitPrice(p: any, item: CartItem, configs: Record<string, any>) { if (item.configurationToken) {
    const cfg = configs[item.configurationToken];
    if (cfg && Number.isFinite(Number(cfg.basePrice)))
        return (Number(cfg.basePrice) + Number(cfg.addonPrice || 0)) / 100;
    return Number(p?.price || 0) + Number(cfg?.addonPrice || 0) / 100;
} return Number(p?.price || 0); }
function CartDrawer({ items, onClose, onQty, onRemove }: {
    items: CartItem[];
    onClose: () => void;
    onQty: (id: string, token: string | undefined, n: number, bundleId?: number) => void;
    onRemove: (id: string, token: string | undefined, bundleId?: number) => void;
}) {
    const configs = useConfigurationDetails(items);
    const [bundleQuotes, setBundleQuotes] = useState<Record<string, any>>({});
    useEffect(() => { let live = true; const slugs = [...new Set(items.map(i => i.bundleSlug).filter(Boolean) as string[])]; Promise.all(slugs.map(slug => api.bundles.get(slug!))).then(values => { if (live)
        setBundleQuotes(Object.fromEntries(values.map(v => [v.slug, v]))); }).catch(() => { if (live)
        setBundleQuotes({}); }); return () => { live = false; }; }, [items]);
    const rows = items.map(i => ({ item: i, p: products.find(p => p.id === i.id) })).filter(x => x.p);
    const grossSubtotal = rows.reduce((s, x) => s + cartUnitPrice(x.p, x.item, configs) * x.item.qty, 0);
    const bundleDiscount = Object.entries(bundleQuotes).reduce((sum, [slug, q]: [
        string,
        any
    ]) => { const bundleLines = items.filter(i => i.bundleSlug === slug && i.bundleId); const bundleQty = bundleLines.length ? Math.min(...bundleLines.map(i => i.qty)) : 0; return sum + Number(q.discount || 0) * bundleQty; }, 0);
    const subtotal = Math.max(0, grossSubtotal - bundleDiscount);
    return <div className="cart-drawer-backdrop" onClick={onClose}><aside className="cart-drawer" onClick={e => e.stopPropagation()}><div className="admin-head"><div><p className="eyebrow">Your bag</p><h2>Selected pieces.</h2></div><button className="icon-btn" onClick={onClose} aria-label="Close"><X /></button></div>{rows.length ? rows.map(({ item, p }) => { const unit = cartUnitPrice(p, item, configs); const cfg = item.configurationToken ? configs[item.configurationToken] : null; let label = item.bundleId ? `Bundle #${item.bundleId}` : 'custom detail'; try {
        if (cfg?.configJson)
            label = JSON.parse(cfg.configJson).accessoryName || label;
    }
    catch { } return <div className="cart-row" key={`${item.id}-${item.configurationToken || 'base'}`}><img src={p!.image} alt=""/><div className="cart-row-copy"><strong>{p!.name}</strong><small>{cfg ? `Configured · ${label}` : 'Standard piece'}</small><span>{money(unit)} × {item.qty}</span><div><button onClick={() => onQty(item.id, item.configurationToken, Math.max(0, item.qty - 1), item.bundleId)}>−</button><span>{item.qty}</span><button onClick={() => onQty(item.id, item.configurationToken, item.qty + 1, item.bundleId)}>+</button><button onClick={() => onRemove(item.id, item.configurationToken, item.bundleId)} aria-label="Remove"><Trash2 size={15}/></button></div></div></div>; }) : <p className="empty-state">Your bag is empty.</p>}<div className="cart-drawer-total"><span>Subtotal</span><strong>{money(subtotal)}</strong></div>{bundleDiscount > 0 && <div className="summary-line"><span>Bundle savings</span><span>−{money(bundleDiscount)}</span></div>}{rows.length > 0 && <Link to="/checkout" className="btn btn-orange full" onClick={onClose}>Continue to checkout</Link>}</aside></div>;
}
function Checkout({ cart, user, onPlaced }: {
    cart: CartItem[];
    user: Customer | null;
    onPlaced: (o: any) => void;
}) {
    const navigate = useNavigate();
    const configs = useConfigurationDetails(cart);
    const [form, setForm] = useState({ name: user?.name || '', email: user?.email || '', phone: user?.phone || '', address: '', city: '', pincode: '', coupon: '' });
    const [shipping, setShipping] = useState<any>(null);
    const [bundleQuotes, setBundleQuotes] = useState<Record<string, any>>({});
    const [discount, setDiscount] = useState(0);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const rows = cart.map(i => ({ item: i, p: products.find(p => p.id === i.id) })).filter(x => x.p);
    const grossSubtotal = rows.reduce((s, x) => s + cartUnitPrice(x.p, x.item, configs) * x.item.qty, 0);
    const bundleDiscount = Object.entries(bundleQuotes).reduce((sum, [slug, q]: [
        string,
        any
    ]) => { const bundleLines = cart.filter(i => i.bundleSlug === slug && i.bundleId); const bundleQty = bundleLines.length ? Math.min(...bundleLines.map(i => i.qty)) : 0; return sum + Number(q.discount || 0) * bundleQty; }, 0);
    const subtotal = Math.max(0, grossSubtotal - bundleDiscount);
    useEffect(() => { let live = true; const slugs = [...new Set(cart.map(i => i.bundleSlug).filter(Boolean) as string[])]; Promise.all(slugs.map(slug => api.bundles.get(slug!))).then(values => { if (live)
        setBundleQuotes(Object.fromEntries(values.map(v => [v.slug, v]))); }).catch(() => { if (live)
        setBundleQuotes({}); }); return () => { live = false; }; }, [cart]);
    useEffect(() => { let live = true; api.shippingQuote(subtotal, 'STANDARD').then(x => { if (live)
        setShipping(x); }).catch(() => { if (live)
        setShipping(null); }); return () => { live = false; }; }, [subtotal]);
    useEffect(() => { let live = true; const code = form.coupon.trim(); if (!code) {
        setDiscount(0);
        return;
    } api.couponQuote(subtotal, code).then(x => { if (live)
        setDiscount(Number(x.discount || 0) / 100); }).catch(() => { if (live)
        setDiscount(0); }); return () => { live = false; }; }, [subtotal, form.coupon]);
    const total = Math.max(0, subtotal - discount + (shipping ? Number(shipping.shippingFee || 0) / 100 : 0));
    const submit = async (e: any) => { e.preventDefault(); if (!cart.length || !user) {
        setError('Please sign in before placing an order.');
        return;
    } setBusy(true); setError(''); try {
        const order = await api.order({ customerId: user.id, customerName: form.name, customerEmail: form.email, phone: form.phone, address: form.address, city: form.city, pincode: form.pincode, paymentMethod: 'COD', shippingMethod: 'STANDARD', couponCode: form.coupon || undefined, items: cart.map(i => ({ slug: i.id, quantity: i.qty, configurationToken: i.configurationToken, bundleId: i.bundleId })) });
        onPlaced(order);
        navigate(`/orders/${order.id}`);
    }
    catch (err: any) {
        setError(err.message || 'Could not place order');
    }
    finally {
        setBusy(false);
    } };
    return <main className="container-w section"><p className="eyebrow">Checkout</p><h1>Complete your order.</h1><div className="checkout-grid"><form className="checkout-form" onSubmit={submit}><input className="field" required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Full name"/><input className="field" required type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} placeholder="Email"/><input className="field" required value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} placeholder="Phone"/><textarea className="field textarea" required value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} placeholder="Delivery address"/><div className="checkout-two"><input className="field" required value={form.city} onChange={e => setForm({ ...form, city: e.target.value })} placeholder="City"/><input className="field" required value={form.pincode} onChange={e => setForm({ ...form, pincode: e.target.value })} placeholder="Pincode"/></div><input className="field" value={form.coupon} onChange={e => setForm({ ...form, coupon: e.target.value.toUpperCase() })} placeholder="Coupon code (optional)"/>{error && <p className="error-message">{error}</p>}<button className="btn btn-orange full" disabled={busy || !cart.length || !user}>{busy ? 'Placing order…' : 'Place COD order'}</button><p className="checkout-login-note">Online payment is intentionally pending. Razorpay remains untouched.</p></form><aside className="order-summary"><h2>Order summary</h2>{rows.map(({ item, p }) => { const unit = cartUnitPrice(p, item, configs); const cfg = item.configurationToken ? configs[item.configurationToken] : null; return <div className="summary-line" key={`${item.id}-${item.configurationToken || 'base'}`}><span>{p!.name} × {item.qty}{cfg ? ' · configured' : ''}</span><span>{money(unit * item.qty)}</span></div>; })}<div className="summary-line"><span>Subtotal</span><span>{money(subtotal)}</span></div>{discount > 0 && <div className="summary-line"><span>Discount</span><span>−{money(discount)}</span></div>}<div className="summary-line"><span>Shipping</span><span>{shipping ? money(Number(shipping.shippingFee || 0) / 100) : '—'}</span></div><div className="summary-total"><strong>Total</strong><strong>{money(total)}</strong></div></aside></div></main>;
}
function Admin() {
    const [tab, setTab] = useState<'dashboard' | 'products' | 'catalog' | 'bundles' | 'media' | 'inventory' | 'orders' | 'customers' | 'reviews' | 'quotes' | 'custom' | 'visual' | 'experience' | 'commerce'>('dashboard');
    const [dashboard, setDashboard] = useState<any>(null), [adminProducts, setAdminProducts] = useState<any[]>([]), [categories, setCategories] = useState<any[]>([]), [collections, setCollections] = useState<any[]>([]), [stock, setStock] = useState<any[]>([]), [adminOrders, setAdminOrders] = useState<any[]>([]), [adminCustomers, setAdminCustomers] = useState<any[]>([]), [adminReviews, setAdminReviews] = useState<any[]>([]), [adminQuotes, setAdminQuotes] = useState<any[]>([]), [adminCustom, setAdminCustom] = useState<any[]>([]);
    const [loading, setLoading] = useState(true), [error, setError] = useState(''), [adminBundles, setAdminBundles] = useState<any[]>([]), [selected, setSelected] = useState<number[]>([]), [visualItems, setVisualItems] = useState<any[]>([]), [visualDraft, setVisualDraft] = useState<any>({ placement: 'HERO', title: '', subtitle: '', mediaType: 'VIDEO', mediaUrl: '', posterUrl: '', linkUrl: '', active: true, sortOrder: 0 }), [accessoryItems, setAccessoryItems] = useState<any[]>([]), [accessoryDraft, setAccessoryDraft] = useState<any>({ name: '', type: 'HANDLE', overlayUrl: '', sku: '', price: '', x: 50, y: 50, scale: 1 }), [editing, setEditing] = useState<any | null>(null), [variants, setVariants] = useState<any[]>([]), [media, setMedia] = useState<any[]>([]), [variantDraft, setVariantDraft] = useState<any>({ optionName: 'Size', optionValue: '', sku: '', priceOverride: '' }), [mediaDraft, setMediaDraft] = useState<any>({ type: 'IMAGE', url: '', altText: '', sortOrder: 0 }), [showForm, setShowForm] = useState(false), [saving, setSaving] = useState(false), [collectionDraft, setCollectionDraft] = useState<any>({ name: '', description: '', active: true, productIds: [] }), [categoryDraft, setCategoryDraft] = useState(''), [bundleDraft, setBundleDraft] = useState<any>({ slug: '', name: '', description: '', discountType: 'PERCENT', discountValue: 10, productIds: [] }), [couponItems, setCouponItems] = useState<any[]>([]), [returnItems, setReturnItems] = useState<any[]>([]), [stockAlerts, setStockAlerts] = useState<any[]>([]), [stockSubscriptions, setStockSubscriptions] = useState<any[]>([]), [cartRecovery, setCartRecovery] = useState<any[]>([]);
    const blank = { slug: '', name: '', price: '', category: 'Handles', finish: 'Brushed Brass', material: 'Metal', color: 'Brass', style: 'Modern', description: '', imageUrl: '', mediaUrls: '', active: true, featured: false, sortOrder: 10 };
    const [form, setForm] = useState<any>(blank);
    const load = async () => { setLoading(true); setError(''); try {
        const [d, p, i, o, c, co, cu, rv, qt, cd, vc, b, cps, rr, sa, ss, cr] = await Promise.all([api.admin.dashboard(), api.admin.products(), api.admin.inventory(), api.admin.orders(), api.admin.categories(), api.admin.collections(), api.admin.customers(), api.admin.reviews(), api.admin.quotes(), api.admin.customDesign(), api.admin.visualContent(), api.admin.bundles(), api.admin.coupons(), api.admin.returns(), api.admin.stockAlerts(), api.admin.stockSubscriptions(), api.admin.cartRecovery()]);
        setDashboard(d);
        setAdminProducts(p);
        setAdminBundles(b);
        setStock(i);
        setAdminOrders(o);
        setCategories(c);
        setCollections(co);
        setAdminCustomers(cu);
        setAdminReviews(rv);
        setAdminQuotes(qt);
        setAdminCustom(cd);
        setVisualItems(vc);
        setCouponItems(cps);
        setReturnItems(rr);
        setStockAlerts(sa);
        setStockSubscriptions(ss);
        setCartRecovery(cr);
        setSelected([]);
    }
    catch (e: any) {
        setError(e.message || 'Admin access required');
    }
    finally {
        setLoading(false);
    } };
    useEffect(() => { load(); }, []);
    const openCreate = () => { setEditing(null); setVariants([]); setMedia([]); setAccessoryItems([]); setForm({ ...blank, sortOrder: (adminProducts.length + 1) * 10 }); setShowForm(true); };
    const openEdit = async (p: any) => { setEditing(p); setForm({ slug: p.slug, name: p.name, price: p.price, category: p.category, finish: p.finish, material: p.material || 'Metal', color: p.color || 'Brass', style: p.style || 'Modern', description: p.description || '', imageUrl: p.imageUrl || '', mediaUrls: p.mediaUrls || '', active: p.active !== false, featured: p.featured === true, sortOrder: p.sortOrder ?? 0 }); setVariants(await api.admin.variants(p.id).catch(() => [])); setMedia(await api.admin.media(p.id).catch(() => [])); setAccessoryItems(await api.admin.accessories(p.id).catch(() => [])); setShowForm(true); };
    const saveProduct = async (e: any) => { e.preventDefault(); setSaving(true); try {
        const body = { ...form, price: Number(form.price) };
        if (editing)
            await api.admin.updateProduct(editing.id, body);
        else
            await api.admin.createProduct(body);
        setShowForm(false);
        await load();
    }
    catch (e: any) {
        setError(e.message || 'Could not save product');
    }
    finally {
        setSaving(false);
    } };
    const toggle = (id: number) => setSelected(v => v.includes(id) ? v.filter(x => x !== id) : [...v, id]);
    const bulk = async (body: any) => { if (!selected.length)
        return; try {
        await api.admin.bulkProducts({ ids: selected, ...body });
        await load();
    }
    catch (e: any) {
        setError(e.message || 'Bulk operation failed');
    } };
    const setQty = async (productId: number, quantity: number) => { try {
        await api.admin.updateStock(productId, quantity);
        await load();
    }
    catch (e: any) {
        setError(e.message || 'Could not update stock');
    } };
    const setStatus = async (id: string, status: string) => { try {
        await api.admin.updateOrderStatus(id, status);
        await load();
    }
    catch (e: any) {
        setError(e.message || 'Could not update order');
    } };
    const saveCategory = async () => { if (!categoryDraft.trim())
        return; try {
        await api.admin.createCategory({ name: categoryDraft.trim(), active: true });
        setCategoryDraft('');
        await load();
    }
    catch (e: any) {
        setError(e.message);
    } };
    const saveCollection = async () => { if (!collectionDraft.name.trim())
        return; try {
        await api.admin.createCollection({ ...collectionDraft, productIds: collectionDraft.productIds.map(Number) });
        setCollectionDraft({ name: '', description: '', active: true, productIds: [] });
        await load();
    }
    catch (e: any) {
        setError(e.message);
    } };
    const addMedia = async () => { if (!editing || !mediaDraft.url.trim())
        return; try {
        await api.admin.createMedia(editing.id, { ...mediaDraft, sortOrder: Number(mediaDraft.sortOrder) });
        setMedia(await api.admin.media(editing.id));
        setMediaDraft({ type: 'IMAGE', url: '', altText: '', sortOrder: media.length + 1 });
    }
    catch (e: any) {
        setError(e.message);
    } };
    return <main className="container-w section admin-page">
  <div className="admin-head"><div><p className="eyebrow">Wolfe operations</p><h1>Store management.</h1><p>Catalog, customers, orders, content and commerce controls.</p></div><button className="btn btn-orange" onClick={openCreate}>Add product</button></div>
  <div className="admin-tabs">{(['dashboard', 'products', 'catalog', 'bundles', 'media', 'inventory', 'orders', 'customers', 'reviews', 'quotes', 'custom', 'visual', 'experience', 'commerce'] as const).map(x => <button key={x} onClick={() => setTab(x)} className={tab === x ? 'active' : ''}>{x}</button>)}</div>
  {error && <div className="admin-note"><strong>{error}</strong></div>}
  {loading ? <div className="admin-note"><h2>Loading operations…</h2></div> : <>
   {tab === 'dashboard' && <><div className="admin-stats">{[['Products', dashboard?.products ?? 0], ['Active', dashboard?.activeProducts ?? 0], ['Customers', dashboard?.customers ?? 0], ['Orders', dashboard?.orders ?? 0], ['Revenue', money(Number(dashboard?.revenue ?? 0) / 100)], ['Low stock', dashboard?.lowStock ?? 0], ['Pending reviews', dashboard?.pendingReviews ?? 0], ['Open quotes', dashboard?.openQuotes ?? 0]].map(x => <div className="admin-stat" key={String(x[0])}><span>{x[0]}</span><strong>{x[1]}</strong></div>)}</div><div className="admin-overview-grid"><div className="admin-panel"><h2>Order pipeline</h2>{[['Confirmed', dashboard?.confirmed], ['Processing', dashboard?.processing], ['Shipped', dashboard?.shipped], ['Delivered', dashboard?.delivered]].map(x => <div className="admin-row" key={String(x[0])}><span>{x[0]}</span><strong>{x[1] ?? 0}</strong></div>)}</div><div className="admin-panel"><h2>Attention</h2><div className="admin-row"><span>Low-stock items</span><strong>{dashboard?.lowStock ?? 0}</strong></div><div className="admin-row"><span>Pending reviews</span><strong>{dashboard?.pendingReviews ?? 0}</strong></div><div className="admin-row"><span>Open custom designs</span><strong>{dashboard?.openCustomDesigns ?? 0}</strong></div><div className="admin-row"><span>Returns</span><strong>{dashboard?.returns ?? 0}</strong></div></div></div></>}
   {tab === 'products' && <><div className="admin-panel" style={{ marginBottom: 16 }}><strong>{selected.length} selected</strong> <button className="btn btn-light" onClick={() => bulk({ active: true })} disabled={!selected.length}>Publish</button> <button className="btn btn-light" onClick={() => bulk({ active: false })} disabled={!selected.length}>Hide</button> <button className="btn btn-light" onClick={() => bulk({ featured: true })} disabled={!selected.length}>Feature</button> <button className="btn btn-light" onClick={() => bulk({ featured: false })} disabled={!selected.length}>Unfeature</button></div><div className="admin-list">{adminProducts.map(p => <div key={p.id}><span><input type="checkbox" checked={selected.includes(p.id)} onChange={() => toggle(p.id)}/> {p.name}<small>{p.slug} · {p.category} · {p.finish} · order {p.sortOrder ?? 0} · {p.active === false ? 'Hidden' : 'Visible'}</small></span><span className="admin-actions"><strong>{money(Number(p.price))}</strong><button onClick={() => openEdit(p)}>Edit</button><button onClick={async () => { await api.admin.deleteProduct(p.id); await load(); }}>Hide</button></span></div>)}</div></>}
   {tab === 'catalog' && <div className="admin-overview-grid"><div className="admin-panel"><h2>Categories</h2><div className="coupon-row"><input className="field" value={categoryDraft} onChange={e => setCategoryDraft(e.target.value)} placeholder="New category"/><button className="btn" onClick={saveCategory}>Add</button></div>{categories.map(c => <div className="admin-row" key={c.id}><span>{c.name}<small>{c.active ? 'Active' : 'Hidden'}</small></span><button onClick={async () => { const name = window.prompt('Category name', c.name); if (name?.trim()) {
            await api.admin.updateCategory(c.id, { name: name.trim(), active: c.active });
            await load();
        } }}>Edit</button><button onClick={async () => { await api.admin.deleteCategory(c.id); await load(); }}>Hide</button></div>)}</div><div className="admin-panel"><h2>Collections</h2><div className="checkout-form"><input className="field" value={collectionDraft.name} onChange={e => setCollectionDraft({ ...collectionDraft, name: e.target.value })} placeholder="Collection name"/><textarea className="field admin-textarea" value={collectionDraft.description} onChange={e => setCollectionDraft({ ...collectionDraft, description: e.target.value })} placeholder="Description"/><select className="select-field" multiple value={collectionDraft.productIds.map(String)} onChange={e => setCollectionDraft({ ...collectionDraft, productIds: Array.from(e.target.selectedOptions).map(x => Number(x.value)) })}>{adminProducts.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select><button type="button" className="btn btn-orange" onClick={saveCollection}>Create collection</button></div>{collections.map(c => <div className="admin-row" key={c.id}><span>{c.name}<small>{c.productCount} products · {c.active ? 'Active' : 'Hidden'}</small></span><button onClick={async () => { const name = window.prompt('Collection name', c.name); if (name?.trim()) {
            const description = window.prompt('Description', c.description || '') ?? c.description || '';
            await api.admin.updateCollection(c.id, { name: name.trim(), description, active: c.active, productIds: c.productIds || [] });
            await load();
        } }}>Edit</button><button onClick={async () => { await api.admin.deleteCollection(c.id); await load(); }}>Delete</button></div>)}</div></div>}
   {tab === 'bundles' && <div className="admin-overview-grid"><div className="admin-panel"><h2>Product bundles</h2><p className="admin-help">Create curated sets. Bundle savings are recalculated server-side at checkout.</p><input className="field" value={bundleDraft.slug} onChange={e => setBundleDraft({ ...bundleDraft, slug: e.target.value })} placeholder="Slug"/><input className="field" value={bundleDraft.name} onChange={e => setBundleDraft({ ...bundleDraft, name: e.target.value })} placeholder="Bundle name"/><textarea className="field admin-textarea" value={bundleDraft.description} onChange={e => setBundleDraft({ ...bundleDraft, description: e.target.value })} placeholder="Description"/><select className="select-field" value={bundleDraft.discountType} onChange={e => setBundleDraft({ ...bundleDraft, discountType: e.target.value })}><option>PERCENT</option><option>FIXED</option></select><input className="field" type="number" min="0" step="0.01" value={bundleDraft.discountValue} onChange={e => setBundleDraft({ ...bundleDraft, discountValue: Number(e.target.value) })} placeholder="Discount"/><select className="select-field" multiple value={bundleDraft.productIds.map(String)} onChange={e => setBundleDraft({ ...bundleDraft, productIds: Array.from(e.target.selectedOptions).map(x => Number(x.value)) })}>{adminProducts.filter(p => p.active !== false).map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select><button className="btn btn-orange" onClick={async () => { if (!bundleDraft.slug || !bundleDraft.name || bundleDraft.productIds.length < 2)
            return; try {
            if (bundleDraft.id)
                await api.admin.updateBundle(bundleDraft.id, { ...bundleDraft, active: true });
            else
                await api.admin.createBundle({ ...bundleDraft, active: true });
            setBundleDraft({ slug: '', name: '', description: '', discountType: 'PERCENT', discountValue: 10, productIds: [] });
            await load();
        }
        catch (e: any) {
            setError(e.message);
        } }}>{bundleDraft.id ? 'Save bundle' : 'Create bundle'}</button></div><div className="admin-panel"><h2>Active bundles</h2>{adminBundles.map(b => <div className="admin-row" key={b.id}><span><strong>{b.name}</strong><small>{b.slug} · {b.discountType} {b.discountValue} · {b.productIds?.length || 0} products</small></span><button onClick={() => setBundleDraft({ id: b.id, slug: b.slug, name: b.name, description: b.description || '', discountType: b.discountType, discountValue: Number(b.discountValue), productIds: b.productIds || [] })}>Edit</button><button onClick={async () => { await api.admin.deleteBundle(b.id); await load(); }}>Hide</button></div>)}{!adminBundles.length && <p className="empty-state">No bundles yet.</p>}</div></div>}
   {tab === 'media' && <div className="admin-list">{adminProducts.map(p => <div key={p.id}><span>{p.name}<small>{p.imageUrl || 'No primary image'} · {String(p.mediaUrls || '').split(/\n|,/).filter(Boolean).length} legacy media</small></span><button onClick={() => openEdit(p)}>Manage media</button></div>)}</div>}
   {tab === 'inventory' && <div className="admin-list">{stock.map(i => <div key={i.productId}><span>{i.name}<small>{i.slug} · Reserved {i.reserved} · Available {i.available}{i.available <= 5 ? ' · LOW STOCK' : ''}</small></span><span className="stock-control"><input className="admin-stock-input" type="number" min="0" value={i.quantity} onChange={e => setStock(prev => prev.map(x => x.productId === i.productId ? { ...x, quantity: Number(e.target.value), available: Number(e.target.value) - x.reserved } : x))} onBlur={e => setQty(i.productId, Number(e.target.value))}/><button onClick={() => setQty(i.productId, i.quantity + 1)}>+</button></span></div>)}</div>}
   {tab === 'orders' && <div className="admin-list">{adminOrders.map(o => <div key={o.id}><span>{o.id}<small>{new Date(o.createdAt).toLocaleString('en-IN')} · {o.customerName} · {o.customerEmail}</small></span><span className="admin-order-actions"><strong>{money(Number(o.total) / 100)}</strong><select value={o.status} onChange={e => setStatus(o.id, e.target.value)}><option>CONFIRMED</option><option>PROCESSING</option><option>SHIPPED</option><option>DELIVERED</option><option>CANCELLED</option></select><button onClick={async () => { const h = await api.admin.orderHistory(o.id); alert(h.map((x: any) => `${x.status} — ${new Date(x.createdAt).toLocaleString('en-IN')}`).join('\n')); }}>History</button></span></div>)}</div>}
   {tab === 'customers' && <div className="admin-list">{adminCustomers.map(c => <div key={c.id}><span>{c.name}<small>{c.email} · {c.phone || 'No phone'} · {c.role}</small></span><strong>#{c.id}</strong></div>)}</div>}
   {tab === 'reviews' && <div className="admin-list">{adminReviews.map(r => <div key={r.id}><span>Review #{r.id}<small>{r.rating}/5 · product {r.productId} · {r.review}</small></span><select value={r.status} onChange={async (e) => { await api.admin.reviewStatus(r.id, e.target.value); setAdminReviews(await api.admin.reviews()); }}><option>PENDING</option><option>APPROVED</option><option>REJECTED</option></select></div>)}</div>}
   {tab === 'quotes' && <div className="admin-list">{adminQuotes.map(q => <div key={q.id}><span>Quote #{q.id}<small>Customer {q.customerId} · {q.message}</small></span><select value={q.status} onChange={async (e) => { await api.admin.quoteStatus(q.id, e.target.value); setAdminQuotes(await api.admin.quotes()); }}><option>NEW</option><option>CONTACTED</option><option>QUOTED</option><option>CLOSED</option></select></div>)}</div>}
   {tab === 'visual' && <div className="admin-overview-grid"><div className="admin-panel"><h2>Visual content studio</h2><p className="admin-help">Hero videos, campaign reels and visual stories. Use hosted MP4/WebM URLs or image URLs.</p><select className="select-field" value={visualDraft.placement} onChange={e => setVisualDraft({ ...visualDraft, placement: e.target.value })}><option>HERO</option><option>COLLECTION_REEL</option><option>MOODBOARD</option></select><input className="field" value={visualDraft.title} onChange={e => setVisualDraft({ ...visualDraft, title: e.target.value })} placeholder="Title"/><input className="field" value={visualDraft.subtitle} onChange={e => setVisualDraft({ ...visualDraft, subtitle: e.target.value })} placeholder="Subtitle"/><select className="select-field" value={visualDraft.mediaType} onChange={e => setVisualDraft({ ...visualDraft, mediaType: e.target.value })}><option>VIDEO</option><option>IMAGE</option></select><input className="field" value={visualDraft.mediaUrl} onChange={e => setVisualDraft({ ...visualDraft, mediaUrl: e.target.value })} placeholder="Media URL"/><input className="field" value={visualDraft.posterUrl} onChange={e => setVisualDraft({ ...visualDraft, posterUrl: e.target.value })} placeholder="Video poster URL (optional)"/><input className="field" value={visualDraft.linkUrl} onChange={e => setVisualDraft({ ...visualDraft, linkUrl: e.target.value })} placeholder="CTA link (optional)"/><button className="btn btn-orange" onClick={async () => { if (!visualDraft.title || !visualDraft.mediaUrl)
            return; await api.admin.createVisualContent({ ...visualDraft, sortOrder: Number(visualDraft.sortOrder) }); setVisualDraft({ ...visualDraft, title: '', subtitle: '', mediaUrl: '', posterUrl: '', linkUrl: '' }); setVisualItems(await api.admin.visualContent()); }}>Publish visual</button></div><div className="admin-panel"><h2>Published visuals</h2>{visualItems.map(v => <div className="admin-row" key={v.id}><span><strong>{v.title}</strong><small>{v.placement} · {v.mediaType} · {v.mediaUrl}</small></span><button onClick={async () => { const title = window.prompt('Title', v.title); if (title?.trim()) {
            const mediaUrl = window.prompt('Media URL', v.mediaUrl);
            if (mediaUrl?.trim()) {
                const linkUrl = window.prompt('CTA link', v.linkUrl || '') ?? v.linkUrl || '';
                await api.admin.updateVisualContent(v.id, { placement: v.placement, title: title.trim(), subtitle: v.subtitle || '', mediaType: v.mediaType, mediaUrl: mediaUrl.trim(), posterUrl: v.posterUrl || '', linkUrl, active: v.active, sortOrder: v.sortOrder });
                setVisualItems(await api.admin.visualContent());
            }
        } }}>Edit</button><button onClick={async () => { await api.admin.deleteVisualContent(v.id); setVisualItems(await api.admin.visualContent()); }}>Remove</button></div>)}</div></div>}
   {tab === 'experience' && <div className="admin-overview-grid"><div className="admin-panel"><h2>360° / 3D / AR experience</h2><p className="admin-help">Attach a 3D model, AR-ready asset or poster to a product.</p><select className="select-field" defaultValue="" id="experience-product"><option value="" disabled>Select product</option>{adminProducts.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select><input id="experience-model" className="field" placeholder="3D model URL"/><input id="experience-ar" className="field" placeholder="AR asset URL"/><input id="experience-poster" className="field" placeholder="Poster URL"/><button className="btn btn-orange" onClick={async () => { const id = Number((document.getElementById('experience-product') as HTMLSelectElement).value); if (!id)
            return; await api.admin.visualAsset(id, { modelUrl: (document.getElementById('experience-model') as HTMLInputElement).value, arUrl: (document.getElementById('experience-ar') as HTMLInputElement).value, posterUrl: (document.getElementById('experience-poster') as HTMLInputElement).value, active: true }); setError('Visual asset saved.'); }}>Save visual asset</button></div><div className="admin-panel"><h2>360° spin frame</h2><p className="admin-help">Add a frame URL to the selected product. Frames are served in product spin view.</p><select className="select-field" defaultValue="" id="spin-product"><option value="" disabled>Select product</option>{adminProducts.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}</select><input id="spin-url" className="field" placeholder="Frame image URL"/><input id="spin-order" className="field" type="number" min="0" defaultValue="0" placeholder="Frame order"/><button className="btn" onClick={async () => { const id = Number((document.getElementById('spin-product') as HTMLSelectElement).value); const imageUrl = (document.getElementById('spin-url') as HTMLInputElement).value.trim(); if (!id || !imageUrl)
            return; await api.admin.spin(id, { imageUrl, sortOrder: Number((document.getElementById('spin-order') as HTMLInputElement).value || 0) }); setError('Spin frame added.'); }}>Add spin frame</button></div><div className="admin-panel"><h2>Visual hotspot</h2><p className="admin-help">Attach a hotspot to an existing visual-content record.</p><input id="hotspot-visual" className="field" type="number" placeholder="Visual content ID"/><input id="hotspot-label" className="field" placeholder="Hotspot label"/><input id="hotspot-target" className="field" placeholder="Target product slug"/><div className="admin-variant-add"><input id="hotspot-x" className="field" type="number" min="0" max="100" defaultValue="50" placeholder="X %"/><input id="hotspot-y" className="field" type="number" min="0" max="100" defaultValue="50" placeholder="Y %"/><button className="btn" onClick={async () => { const id = Number((document.getElementById('hotspot-visual') as HTMLInputElement).value); if (!id)
            return; await api.admin.hotspot(id, { label: (document.getElementById('hotspot-label') as HTMLInputElement).value, targetSlug: (document.getElementById('hotspot-target') as HTMLInputElement).value, x: Number((document.getElementById('hotspot-x') as HTMLInputElement).value), y: Number((document.getElementById('hotspot-y') as HTMLInputElement).value), active: true }); setError('Hotspot added.'); }}>Add hotspot</button></div></div></div>}
   {tab === 'commerce' && <div className="admin-overview-grid"><div className="admin-panel"><h2>Coupons</h2><div className="checkout-form"><input id="coupon-code" className="field" placeholder="Code"/><select id="coupon-type" className="select-field"><option>PERCENT</option><option>FIXED</option></select><input id="coupon-value" className="field" type="number" min="0" step="0.01" placeholder="Value"/><button className="btn btn-orange" onClick={async () => { const code = (document.getElementById('coupon-code') as HTMLInputElement).value.trim(); const type = (document.getElementById('coupon-type') as HTMLSelectElement).value; const value = Number((document.getElementById('coupon-value') as HTMLInputElement).value); if (!code || !value)
            return; try {
            await api.admin.createCoupon({ code, discountType: type, value, active: true });
            setCouponItems(await api.admin.coupons());
        }
        catch (e: any) {
            setError(e.message);
        } }}>Add coupon</button></div>{couponItems.map(c => <div className="admin-row" key={c.id}><span><strong>{c.code}</strong><small>{c.discountType} {c.value} · used {c.usedCount || 0}/{c.usageLimit ?? '∞'} · {c.active ? 'Active' : 'Hidden'}</small></span><button onClick={async () => { await api.admin.deleteCoupon(c.id); setCouponItems(await api.admin.coupons()); }}>Disable</button></div>)}</div><div className="admin-panel"><h2>Returns</h2>{returnItems.map(r => <div className="admin-row" key={r.id}><span>Return #{r.id}<small>Order {r.orderId} · {r.reason}</small></span><select value={r.status} onChange={async (e) => { await api.admin.updateReturn(r.id, { status: e.target.value, refundAmount: Number(r.refundAmount || 0), refundStatus: r.refundStatus || 'PENDING', adminNote: r.adminNote || '' }); setReturnItems(await api.admin.returns()); }}><option>PENDING</option><option>APPROVED</option><option>REJECTED</option><option>RECEIVED</option><option>COMPLETED</option></select></div>)}{!returnItems.length && <p className="empty-state">No return requests.</p>}</div><div className="admin-panel"><h2>Stock alerts</h2>{stockAlerts.map(a => <div className="admin-row" key={a.productId}><span>{a.name}<small>Available {a.available}</small></span><strong>LOW</strong></div>)}</div><div className="admin-panel"><h2>Back-in-stock subscriptions</h2>{stockSubscriptions.map(x => <div className="admin-row" key={x.id}><span>Customer {x.customerId}<small>Product {x.productId} · {x.active ? 'Active' : 'Not active'}</small></span></div>)}{!stockSubscriptions.length && <p className="empty-state">No subscriptions.</p>}</div><div className="admin-panel"><h2>Cart recovery</h2>{cartRecovery.map(x => <div className="admin-row" key={x.customerId}><span>Customer {x.customerId}<small>Last cart activity: {x.lastTouchedAt ? new Date(x.lastTouchedAt).toLocaleString('en-IN') : '—'}</small></span><strong>{x.recovered ? 'Recovered' : 'Pending'}</strong></div>)}{!cartRecovery.length && <p className="empty-state">No recovery records.</p>}</div></div>}
   {tab === 'custom' && <div className="admin-list">{adminCustom.map(d => <div key={d.id}><span>Design #{d.id} — {d.projectName}<small>Customer {d.customerId} · {d.requirements || 'No requirements'} {d.referenceImageUrl && ' · ' + d.referenceImageUrl}</small></span><select value={d.status} onChange={async (e) => { await api.admin.customDesignStatus(d.id, e.target.value); setAdminCustom(await api.admin.customDesign()); }}><option>NEW</option><option>CONTACTED</option><option>IN_PROGRESS</option><option>COMPLETED</option><option>CLOSED</option></select></div>)}</div>}
  </>}
  {showForm && <div className="admin-modal"><form className="admin-form" onSubmit={saveProduct}><div className="admin-form-head"><h2>{editing ? 'Edit product' : 'Add product'}</h2><button type="button" onClick={() => setShowForm(false)}>×</button></div>{[['slug', 'Slug'], ['name', 'Product name'], ['price', 'Price']].map(([k, l]) => <input key={k} required value={form[k]} type={k === 'price' ? 'number' : 'text'} min={k === 'price' ? '0.01' : undefined} step={k === 'price' ? '0.01' : undefined} onChange={e => setForm({ ...form, [k]: e.target.value })} placeholder={l} className="field"/>)}<input list="wolfe-categories" required value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} placeholder="Category" className="field"/><datalist id="wolfe-categories">{categories.filter(c => c.active).map(c => <option key={c.id} value={c.name}/>)}</datalist><input value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} placeholder="Primary image URL / path" className="field"/><textarea value={form.mediaUrls} onChange={e => setForm({ ...form, mediaUrls: e.target.value })} placeholder="Legacy media URLs, one per line" className="field admin-textarea"/><input required value={form.finish} onChange={e => setForm({ ...form, finish: e.target.value })} placeholder="Finish" className="field"/><input required value={form.material} onChange={e => setForm({ ...form, material: e.target.value })} placeholder="Material" className="field"/><input required value={form.color} onChange={e => setForm({ ...form, color: e.target.value })} placeholder="Colour" className="field"/><input required value={form.style} onChange={e => setForm({ ...form, style: e.target.value })} placeholder="Style" className="field"/><textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Description" className="field admin-textarea"/><label className="admin-check"><input type="checkbox" checked={form.active !== false} onChange={e => setForm({ ...form, active: e.target.checked })}/> Visible</label><label className="admin-check"><input type="checkbox" checked={form.featured === true} onChange={e => setForm({ ...form, featured: e.target.checked })}/> Featured</label><input required type="number" min="0" value={form.sortOrder ?? 0} onChange={e => setForm({ ...form, sortOrder: Number(e.target.value) })} placeholder="Catalog order" className="field"/>
   {editing && <><div className="admin-panel"><h3>Variants</h3>{variants.map(v => <div className="admin-row" key={v.id}><span>{v.optionName}: {v.optionValue}<small>{v.sku || 'No SKU'}{v.priceOverride ? ` · ₹${v.priceOverride}` : ''}</small></span><button type="button" onClick={async () => { const optionValue = window.prompt('Variant value', v.optionValue); if (optionValue?.trim()) {
                const sku = window.prompt('SKU', v.sku || '') ?? v.sku || '';
                const price = window.prompt('Price override', v.priceOverride ?? '');
                await api.admin.updateVariant(editing.id, v.id, { optionName: v.optionName, optionValue: optionValue.trim(), sku, priceOverride: price === '' || price === null ? null : Number(price), active: v.active });
                setVariants(await api.admin.variants(editing.id));
            } }}>Edit</button><button type="button" onClick={async () => { await api.admin.deleteVariant(editing.id, v.id); setVariants(await api.admin.variants(editing.id)); }}>Remove</button></div>)}<div className="admin-variant-add"><input value={variantDraft.optionName} onChange={e => setVariantDraft({ ...variantDraft, optionName: e.target.value })} placeholder="Option" className="field"/><input value={variantDraft.optionValue} onChange={e => setVariantDraft({ ...variantDraft, optionValue: e.target.value })} placeholder="Value" className="field"/><input value={variantDraft.sku} onChange={e => setVariantDraft({ ...variantDraft, sku: e.target.value })} placeholder="SKU" className="field"/><input value={variantDraft.priceOverride} onChange={e => setVariantDraft({ ...variantDraft, priceOverride: e.target.value })} placeholder="Price override" type="number" className="field"/><button type="button" className="btn" onClick={async () => { if (!variantDraft.optionValue)
                return; await api.admin.createVariant(editing.id, { ...variantDraft, priceOverride: variantDraft.priceOverride ? Number(variantDraft.priceOverride) : null, active: true }); setVariantDraft({ ...variantDraft, optionValue: '', sku: '', priceOverride: '' }); setVariants(await api.admin.variants(editing.id)); }}>Add variant</button></div></div>
   <div className="admin-panel"><h3>Media</h3>{media.map(m => <div className="admin-row" key={m.id}><span>{m.type}: {m.url}<small>{m.altText || 'No alt text'} · order {m.sortOrder}</small></span><button type="button" onClick={async () => { const url = window.prompt('Media URL', m.url); if (url?.trim()) {
                const altText = window.prompt('Alt text', m.altText || '') ?? m.altText || '';
                await api.admin.updateMedia(editing.id, m.id, { type: m.type, url: url.trim(), altText, sortOrder: m.sortOrder, active: m.active });
                setMedia(await api.admin.media(editing.id));
            } }}>Edit</button><button type="button" onClick={async () => { await api.admin.deleteMedia(editing.id, m.id); setMedia(await api.admin.media(editing.id)); }}>Remove</button></div>)}<div className="admin-variant-add"><select className="select-field" value={mediaDraft.type} onChange={e => setMediaDraft({ ...mediaDraft, type: e.target.value })}><option>IMAGE</option><option>VIDEO</option></select><input value={mediaDraft.url} onChange={e => setMediaDraft({ ...mediaDraft, url: e.target.value })} placeholder="Media URL" className="field"/><input value={mediaDraft.altText} onChange={e => setMediaDraft({ ...mediaDraft, altText: e.target.value })} placeholder="Alt text" className="field"/><input value={mediaDraft.sortOrder} onChange={e => setMediaDraft({ ...mediaDraft, sortOrder: Number(e.target.value) })} type="number" min="0" className="field"/><button type="button" className="btn" onClick={addMedia}>Add media</button></div></div><div className="admin-panel"><h3>Visual accessories / configurator</h3><p className="admin-help">Overlay a transparent PNG/WebP of a handle, knob or accessory on the product image. X/Y are percentages.</p>{accessoryItems.map(a => <div className="admin-row" key={a.id}><span>{a.name}<small>{a.type} · {a.x}% / {a.y}% · scale {a.scale}</small></span><button type="button" onClick={async () => { const name = window.prompt('Accessory name', a.name); if (name?.trim()) {
                const overlayUrl = window.prompt('Overlay URL', a.overlayUrl);
                if (overlayUrl?.trim()) {
                    await api.admin.updateAccessory(editing.id, a.id, { name: name.trim(), type: a.type, overlayUrl: overlayUrl.trim(), sku: a.sku || '', price: a.price, x: a.x, y: a.y, scale: a.scale, active: a.active });
                    setAccessoryItems(await api.admin.accessories(editing.id));
                }
            } }}>Edit</button><button type="button" onClick={async () => { await api.admin.deleteAccessory(editing.id, a.id); setAccessoryItems(await api.admin.accessories(editing.id)); }}>Remove</button></div>)}<div className="admin-variant-add"><input className="field" value={accessoryDraft.name} onChange={e => setAccessoryDraft({ ...accessoryDraft, name: e.target.value })} placeholder="Accessory name"/><input className="field" value={accessoryDraft.type} onChange={e => setAccessoryDraft({ ...accessoryDraft, type: e.target.value })} placeholder="Type: HANDLE / KNOB / PULL"/><input className="field" value={accessoryDraft.overlayUrl} onChange={e => setAccessoryDraft({ ...accessoryDraft, overlayUrl: e.target.value })} placeholder="Transparent overlay URL"/><input className="field" type="number" value={accessoryDraft.x} min="0" max="100" onChange={e => setAccessoryDraft({ ...accessoryDraft, x: Number(e.target.value) })} placeholder="X %"/><input className="field" type="number" value={accessoryDraft.y} min="0" max="100" onChange={e => setAccessoryDraft({ ...accessoryDraft, y: Number(e.target.value) })} placeholder="Y %"/><input className="field" type="number" value={accessoryDraft.scale} min="0.1" max="4" step="0.1" onChange={e => setAccessoryDraft({ ...accessoryDraft, scale: Number(e.target.value) })} placeholder="Scale"/><button type="button" className="btn" onClick={async () => { if (!accessoryDraft.name || !accessoryDraft.overlayUrl)
                return; await api.admin.createAccessory(editing.id, { ...accessoryDraft, price: accessoryDraft.price ? Number(accessoryDraft.price) : null }); setAccessoryDraft({ name: '', type: 'HANDLE', overlayUrl: '', sku: '', price: '', x: 50, y: 50, scale: 1 }); setAccessoryItems(await api.admin.accessories(editing.id)); }}>Add accessory</button></div></div></>}
   <button disabled={saving} className="btn btn-orange full">{saving ? 'Saving…' : editing ? 'Save changes' : 'Create product'}</button></form></div>}
 </main>;
}
function ProfilePage({ user, onSaved }: {
    user: Customer | null;
    onSaved: (u: Customer) => void;
}) { const [name, setName] = useState(user?.name || ''); const [phone, setPhone] = useState(user?.phone || ''); const [busy, setBusy] = useState(false); const [msg, setMsg] = useState(''); if (!user)
    return <main className="container-w narrow-page"><p className="empty-state">Sign in to manage your profile.</p><Link to="/account" className="btn btn-orange">Sign in</Link></main>; const save = async (e: any) => { e.preventDefault(); setBusy(true); setMsg(''); try {
    const u = await api.updateProfile({ name, phone });
    onSaved(u);
    setMsg('Profile saved.');
}
catch (err: any) {
    setMsg(err.message || 'Unable to save profile');
}
finally {
    setBusy(false);
} }; return <main className="container-w narrow-page"><p className="eyebrow">Customer account</p><h1>Your profile</h1><form onSubmit={save} className="login-form"><input required value={name} onChange={e => setName(e.target.value)} placeholder="Full name" className="field"/><input value={user.email} disabled className="field"/><input value={phone} onChange={e => setPhone(e.target.value)} placeholder="Phone" className="field"/>{msg && <p className="checkout-login-note">{msg}</p>}<button disabled={busy} className="btn btn-orange full">{busy ? 'Saving…' : 'Save profile'}</button></form><Link to="/addresses" className="text-link">Manage saved addresses <ArrowRight size={16}/></Link></main>; }
function AddressesPage({ user }: {
    user: Customer | null;
}) { const [items, setItems] = useState<any[]>([]); const [form, setForm] = useState<any>({ label: 'Home', recipientName: user?.name || '', phone: user?.phone || '', address: '', city: '', state: 'Rajasthan', pincode: '', isDefault: false }); const [editing, setEditing] = useState<number | null>(null); const [msg, setMsg] = useState(''); const load = () => user && api.addresses.get(user.id).then(setItems).catch(() => setItems([])); useEffect(() => { load(); }, [user]); if (!user)
    return <main className="container-w narrow-page"><p className="empty-state">Sign in to manage addresses.</p></main>; const save = async (e: any) => { e.preventDefault(); try {
    if (editing)
        await api.addresses.update(user.id, editing, form);
    else
        await api.addresses.create(user.id, form);
    setEditing(null);
    setForm({ label: 'Home', recipientName: user.name, phone: user.phone || '', address: '', city: '', state: 'Rajasthan', pincode: '', isDefault: false });
    await load();
}
catch (err: any) {
    setMsg(err.message || 'Unable to save address');
} }; const edit = (a: any) => { setEditing(a.id); setForm({ ...a, isDefault: a.default }); setMsg(''); }; return <main className="container-w section"><p className="eyebrow">Customer account</p><h1>Saved addresses</h1><div className="account-grid">{items.map(a => <div className="account-card" key={a.id}><strong>{a.label}{a.default ? ' · Default' : ''}</strong><p>{a.recipientName}<br />{a.address}<br />{a.city}, {a.state} — {a.pincode}<br />{a.phone}</p><button className="text-link" onClick={() => edit(a)}>Edit</button><button className="text-link" onClick={async () => { await api.addresses.remove(user.id, a.id); load(); }}>Remove</button></div>)}</div><form onSubmit={save} className="checkout-form" style={{ maxWidth: 720, marginTop: 32 }}><h2>{editing ? 'Edit address' : 'Add address'}</h2>{[['label', 'Label'], ['recipientName', 'Recipient name'], ['phone', 'Phone'], ['address', 'Address'], ['city', 'City'], ['state', 'State'], ['pincode', 'Pincode']].map(([k, l]) => <input key={k} required value={form[k] || ''} onChange={e => setForm({ ...form, [k]: e.target.value })} placeholder={l} className="field"/>)}<label className="admin-check"><input type="checkbox" checked={!!form.isDefault} onChange={e => setForm({ ...form, isDefault: e.target.checked })}/> Make default</label>{msg && <p className="error-message">{msg}</p>}<button className="btn btn-orange">{editing ? 'Update address' : 'Save address'}</button></form></main>; }
function OrderDetail({ user }: {
    user: Customer | null;
}) { const { id } = useParams(); const [data, setData] = useState<any>(null); const [error, setError] = useState(''); const [reason, setReason] = useState(''); const [returnSent, setReturnSent] = useState(false); const [history, setHistory] = useState<any[]>([]); useEffect(() => { if (user && id) {
    api.orderDetail(id).then(setData).catch(e => setError(e.message));
    api.orderHistory(id).then(setHistory).catch(() => setHistory([]));
} }, [user, id]); if (!user)
    return <main className="container-w narrow-page"><p className="empty-state">Sign in to view this order.</p></main>; if (error)
    return <main className="container-w narrow-page"><p className="error-message">{error}</p></main>; if (!data)
    return <main className="container-w section"><p>Loading order…</p></main>; const o = data.order; return <main className="container-w section"><p className="eyebrow">Order detail</p><h1>{o.id}</h1><p>{new Date(o.createdAt).toLocaleString('en-IN')} · <strong>{o.status}</strong></p><div className="order-card"><div><h2>Items</h2>{data.items.map((i: any) => { let cfgLabel = ''; if (i.configurationJson) {
    try {
        const c = JSON.parse(i.configurationJson);
        if (c.accessoryName)
            cfgLabel = ` · ${c.accessoryName}`;
        if (c.room)
            cfgLabel += ` · ${c.room} room`;
    }
    catch { }
} return <div className="summary-line" key={i.id}><span>{i.productName} × {i.quantity}{i.configurationToken ? ` · configured${cfgLabel}` : ''}</span><span>{money(i.unitPrice / 100 * i.quantity)}</span></div>; })}<div className="summary-total"><strong>Total</strong><strong>{money(o.total / 100)}</strong></div></div><div><h2>Delivery</h2><p>{o.customerName}<br />{o.phone}<br />{o.address}<br />{o.city} — {o.pincode}</p></div></div><div className="order-card" style={{ marginTop: 20 }}><div><h2>Order history</h2>{history.length ? history.map((h: any) => <div className="summary-line" key={h.id}><span><strong>{h.status}</strong>{h.note && <small> · {h.note}</small>}</span><span>{new Date(h.createdAt).toLocaleString('en-IN')}</span></div>) : <p className="empty-state">History will appear as the order progresses.</p>}</div></div>{['CONFIRMED', 'PROCESSING'].includes(o.status) && <button className="btn btn-light" onClick={async () => { await api.cancelOrder(o.id); setData(await api.orderDetail(o.id)); }}>Cancel order</button>}{o.status === 'DELIVERED' && !returnSent && <form className="checkout-form" style={{ maxWidth: 720, marginTop: 32 }} onSubmit={async (e) => { e.preventDefault(); await api.returns.create(user.id, o.id, reason); setReturnSent(true); }}><h2>Request a return</h2><textarea required minLength={10} maxLength={1000} value={reason} onChange={e => setReason(e.target.value)} placeholder="Tell us why you want to return this order" className="field admin-textarea"/><button className="btn btn-orange">Submit return request</button></form>}{returnSent && <p className="checkout-login-note">Return request submitted. Our team will review it.</p>}</main>; }
function NotificationsPage({ user }: {
    user: Customer | null;
}) { const [data, setData] = useState<any>({ items: [], unread: 0 }); useEffect(() => { if (user)
    api.notifications.get(user.id).then(setData).catch(() => setData({ items: [], unread: 0 })); }, [user]); if (!user)
    return <main className="container-w narrow-page"><p className="empty-state">Sign in to view notifications.</p></main>; return <main className="container-w section"><div className="admin-head"><div><p className="eyebrow">Customer account</p><h1>Notifications</h1><p>{data.unread || 0} unread</p></div>{data.items?.some((n: any) => !n.readAt) && <button className="btn btn-light" onClick={async () => { await api.notifications.readAll(user.id); setData(await api.notifications.get(user.id)); }}>Mark all read</button>}</div><div className="orders-list">{data.items?.length ? data.items.map((n: any) => <div className="order-card" key={n.id} style={{ opacity: n.readAt?.length ? 0.65 : 1 }} onClick={async () => { if (!n.readAt) {
    await api.notifications.read(user.id, n.id);
    setData(await api.notifications.get(user.id));
} }}><div><strong>{n.title}</strong><p>{n.message}</p><small>{new Date(n.createdAt).toLocaleString('en-IN')}</small></div></div>) : <p className="empty-state">No notifications yet.</p>}</div></main>; }
function ConfigurationShare() { const { token } = useParams(); const [data, setData] = useState<any>(null); useEffect(() => { if (token)
    api.experience.getConfiguration(token).then(setData).catch(() => setData(null)); }, [token]); if (!data)
    return <main className="container-w narrow-page"><p className="empty-state">Configuration not found.</p></main>; return <main className="container-w section"><p className="eyebrow">Saved Wolfe configuration</p><h1>Build your look.</h1><p>Product ID: {data.productId}</p><pre className="config-share-json">{data.configJson}</pre><Link to="/shop" className="btn btn-orange">Explore the collection</Link></main>; }
function Footer() { return <footer className="site-footer"><div className="container-w footer-grid"><div className="footer-brand"><img src="/wolfe-logo.png" alt="Wolfe — The Jewel of Villa"/><p>Premium architectural hardware for considered spaces.</p></div><div><p className="footer-title">Explore</p><Link to="/shop">Shop</Link><Link to="/wishlist">Wishlist</Link><Link to="/consultation">Consultation</Link><Link to="/custom-design">Custom design</Link><Link to="/quote">Project quote</Link><Link to="/bundles">Bundles</Link><Link to="/catalogue">Catalogue</Link></div><div><p className="footer-title">Customer</p><Link to="/account">Account</Link><Link to="/orders">Orders</Link><Link to="/notifications">Notifications</Link><Link to="/shop">Shipping & Returns</Link></div><div className="footer-address"><p className="footer-title">Visit us</p><strong>Hinglaj Hardware</strong><p>10B Road, Opp. Barmer Bhavan<br />Sardarpura, Jodhpur, Rajasthan, India</p><p>For product and project enquiries, contact the Wolfe team.</p></div></div><div className="container-w footer-bottom"><span>© {new Date().getFullYear()} Wolfe. All rights reserved.</span><span>Hinglaj Hardware · Jodhpur</span></div></footer>; }
function Bundles({ onAddBundle }: {
    onAddBundle: (b: any) => void;
}) { const [items, setItems] = useState<any[]>([]); useEffect(() => { api.bundles.list().then(setItems).catch(() => setItems([])); }, []); return <main className="container-w section"><p className="eyebrow">Wolfe bundles</p><h1>Considered combinations.</h1><p className="detail-description">Curated pieces together, with bundle savings calculated server-side at checkout.</p><div className="product-grid">{items.map((b: any) => <article className="product-card" key={b.id}><div className="product-card-image"><img src={b.items?.[0]?.imageUrl || '/wolfe-logo.png'} alt={b.name}/></div><div className="product-card-copy"><p className="eyebrow">Bundle</p><h3>{b.name}</h3><p>{b.description}</p><div className="detail-price">{money(Number(b.total))} <small style={{ textDecoration: 'line-through', opacity: .55 }}>{money(Number(b.subtotal))}</small></div><p className="checkout-login-note">Save {money(Number(b.discount))}</p><button className="btn btn-orange" onClick={() => onAddBundle(b)}>Add bundle to bag</button></div></article>)}</div>{!items.length && <p className="empty-state">No active bundles yet.</p>}</main>; }
function App() { const [compareList, setCompareList] = useState<string[]>(() => read('wolfe_compare', [])); const toggleCompare = (id: string) => { setCompareList(v => { const next = v.includes(id) ? v.filter(x => x !== id) : v.length >= 4 ? v : [...v, id]; write('wolfe_compare', next); return next; }); }; const [user, setUser] = useState<Customer | null>(() => read('wolfe_user', null)); const [, setCatalogVersion] = useState(0); useEffect(() => { api.products().then((server: any[]) => { const mapped = server.filter(s => s.active !== false).map(s => ({ id: s.slug, serverId: Number(s.id), name: s.name, category: s.category, price: Number(s.price), image: s.imageUrl || '/wolfe-logo.png', description: s.description || '', finish: s.finish, material: s.material || 'Metal', color: s.color || 'Brass', style: s.style || 'Modern', featured: s.featured === true, media: String(s.mediaUrls || '').split(/\r?\n|,/).map((x: string) => x.trim()).filter(Boolean) })); products.splice(0, products.length, ...mapped); activeProductSlugs = new Set(mapped.map(p => p.id)); setCatalogVersion(v => v + 1); }).catch(() => { }); }, []); const [cart, setCart] = useState<CartItem[]>(() => read('wolfe_cart', [])); const addBundle = async (b: any) => { const next = [...cart]; for (const line of b.items || []) {
    const same = (x: CartItem) => x.id === line.slug && x.bundleId === Number(b.id) && !x.configurationToken;
    const found = next.find(same);
    if (found)
        found.qty += Number(line.quantity || 1);
    else
        next.push({ id: line.slug, qty: Number(line.quantity || 1), bundleId: Number(b.id), bundleSlug: b.slug });
} persistCart(next); setDrawer(true); }; const [wishes, setWishes] = useState<string[]>(() => read('wolfe_wishlist', [])); const [drawer, setDrawer] = useState(false); const persistCart = (v: CartItem[]) => { setCart(v); write('wolfe_cart', v); }; const syncAccount = async (u: Customer) => { setUser(u); write('wolfe_user', u); try {
    const [serverCart, serverWishes] = await Promise.all([api.cart.get(u.id), api.wishlist.get(u.id).catch(() => [])]);
    const mapped = serverCart.map((x: any) => { const p = products.find(p => p.serverId === x.productId); return p ? { id: p.id, qty: x.quantity } : null; }).filter(Boolean) as CartItem[];
    const localSpecial = cart.filter(x => !!x.configurationToken || x.bundleId);
    const merged = [...mapped, ...localSpecial];
    if (merged.length || serverCart.length === 0)
        persistCart(merged);
    setWishes(serverWishes);
    write('wolfe_wishlist', serverWishes);
}
catch { } }; const add = async (id: string) => { const next = cart.some(x => x.id === id && !x.configurationToken) ? cart.map(x => x.id === id && !x.configurationToken ? { ...x, qty: x.qty + 1 } : x) : [...cart, { id, qty: 1 }]; persistCart(next); setDrawer(true); if (user) {
    const item = next.find(x => x.id === id)!;
    try {
        await api.cart.put(user.id, id, item.qty);
    }
    catch { }
} }; const addConfigured = async (id: string, configurationToken: string) => { const next = cart.some(x => x.id === id && x.configurationToken === configurationToken) ? cart.map(x => x.id === id && x.configurationToken === configurationToken ? { ...x, qty: x.qty + 1 } : x) : [...cart, { id, qty: 1, configurationToken }]; persistCart(next); setDrawer(true); }; const qty = async (id: string, configurationToken: string | undefined, n: number, bundleId?: number) => { const same = (x: CartItem) => x.id === id && x.configurationToken === configurationToken && x.bundleId === bundleId; const next = bundleId ? (n < 1 ? cart.filter(x => x.bundleId !== bundleId) : cart.map(x => x.bundleId === bundleId ? { ...x, qty: n } : x)) : (n < 1 ? cart.filter(x => !same(x)) : cart.map(x => same(x) ? { ...x, qty: n } : x)); persistCart(next); if (user && !configurationToken && !bundleId) {
    try {
        if (n < 1)
            await api.cart.remove(user.id, id);
        else
            await api.cart.put(user.id, id, n);
    }
    catch { }
} }; const remove = (id: string, configurationToken: string | undefined, bundleId?: number) => qty(id, configurationToken, 0, bundleId); const wish = async (id: string) => { const adding = !wishes.includes(id); const v = adding ? [...wishes, id] : wishes.filter(x => x !== id); setWishes(v); write('wolfe_wishlist', v); if (user) {
    try {
        if (adding)
            await api.wishlist.add(user.id, id);
        else
            await api.wishlist.remove(user.id, id);
    }
    catch { }
} }; const placed = (order: any) => { persistCart([]); return order; }; const logout = async () => { try {
    await api.logout();
}
catch { } setUser(null); localStorage.removeItem('wolfe_user'); setCart([]); setWishes([]); localStorage.removeItem('wolfe_cart'); localStorage.removeItem('wolfe_wishlist'); }; return <div><Header cartCount={cart.reduce((s, x) => s + x.qty, 0)} wishCount={wishes.length} onCart={() => setDrawer(true)}/><Routes><Route path="/" element={<Home onAdd={add} onWish={wish} wishes={wishes} onCompare={toggleCompare} compared={compareList}/>}/><Route path="/bundles" element={<Bundles onAddBundle={addBundle}/>}/><Route path="/shop" element={<Shop onAdd={add} onWish={wish} wishes={wishes} onCompare={toggleCompare} compared={compareList}/>}/><Route path="/product/:id" element={<ProductPage onAdd={add} onAddConfigured={addConfigured} onWish={wish} wishes={wishes} user={user}/>}/><Route path="/compare" element={<Comparison items={compareList.map(id => products.find(p => p.id === id)).filter(Boolean) as any} onAdd={add} onWish={wish} wishes={wishes} onCompare={toggleCompare} compared={compareList}/>}/><Route path="/config/:token" element={<ConfigurationShare />}/><Route path="/wishlist" element={<Wishlist wishes={wishes} onWish={wish} onAdd={add}/>}/><Route path="/checkout" element={<Checkout cart={cart} user={user} onPlaced={placed}/>}/><Route path="/account" element={<Account user={user} onLogin={syncAccount} onLogout={logout}/>}/><Route path="/orders" element={<Orders user={user}/>}/><Route path="/notifications" element={<NotificationsPage user={user}/>}/><Route path="/orders/:id" element={<OrderDetail user={user}/>}/><Route path="/profile" element={<ProfilePage user={user} onSaved={u => { setUser(u); write('wolfe_user', u); }}/>}/><Route path="/addresses" element={<AddressesPage user={user}/>}/><Route path="/consultation" element={<Consultation />}/><Route path="/quote" element={<QuoteRequest user={user}/>}/><Route path="/custom-design" element={<CustomDesign user={user}/>}/><Route path="/catalogue" element={<SimpleContent eyebrow="Wolfe catalogue" title="Explore the complete collection." copy="Request the latest product catalogue for finishes, dimensions and project planning."/>}/><Route path="/journal" element={<SimpleContent eyebrow="Journal" title="Ideas for considered spaces." copy="A growing editorial space for materials, rooms, finishes and the small details that make a home feel like yours."/>}/><Route path="/admin" element={<Admin />}/></Routes><Footer />{drawer && <CartDrawer items={cart} onClose={() => setDrawer(false)} onQty={qty} onRemove={remove}/>}</div>; }
export default App;

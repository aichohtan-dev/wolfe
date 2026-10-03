import React, { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { Heart, Check, ShieldCheck, Truck, Layers, Info } from 'lucide-react';
import { products } from '../data';
import { api, type Customer, type ProductVariant } from '../api';
import { money } from '../utils/format';
import { read, write } from '../utils/storage';
import { useSeo } from '../hooks/useSeo';
import { ReviewPanel } from '../components/commerce/ReviewPanel';
import { AccessoryConfigurator } from '../components/visual/AccessoryConfigurator';
import { SpinViewer } from '../components/visual/SpinViewer';
import { SectionHead } from '../components/layout/SectionHead';
import { ProductGrid } from '../App';

export interface ProductPageProps {
  onAdd: (id: string, variant?: Partial<ProductVariant>) => void;
  onAddConfigured: (id: string, token: string) => void;
  onWish: (id: string) => void;
  wishes: string[];
  user: Customer | null;
}

export default function ProductPage({
  onAdd,
  onAddConfigured,
  onWish,
  wishes,
  user,
}: ProductPageProps) {
  const { id } = useParams();
  const baseP = products.find((x) => x.id === id);

  const [liveProduct, setLiveProduct] = useState<any>(baseP || null);
  const [variants, setVariants] = useState<ProductVariant[]>([]);
  const [selectedVariant, setSelectedVariant] = useState<ProductVariant | null>(null);

  // Selected attribute filters for combination resolution
  const [selectedColor, setSelectedColor] = useState<string>('');
  const [selectedSize, setSelectedSize] = useState<string>('');
  const [selectedFinish, setSelectedFinish] = useState<string>('');
  const [selectedMaterial, setSelectedMaterial] = useState<string>('');

  const [activeImage, setActiveImage] = useState<string>(baseP?.image || '/catalog/brass-01.jpg');
  const [related, setRelated] = useState<any[]>([]);
  const [recent, setRecent] = useState<any[]>([]);
  const [stockSubscribed, setStockSubscribed] = useState(false);
  const [stockMessage, setStockMessage] = useState('');

  useSeo(
    liveProduct ? `${liveProduct.name} | Wolfe — Master Collection` : 'Product | Wolfe',
    liveProduct
      ? `${liveProduct.name} — premium ${liveProduct.category.toLowerCase()} from Wolfe. ${liveProduct.description}`
      : 'Explore architectural hardware and surfaces from Wolfe.'
  );

  useEffect(() => {
    if (id) {
      api.product(id).then((p) => {
        if (p) {
          setLiveProduct(p);
          if (p.imageUrl) setActiveImage(p.imageUrl);
          if (user) {
            const resolvedServerId = Number(p.id || 0);
            if (resolvedServerId > 0) {
              api.experience.touchRecent(user.id, resolvedServerId).catch(() => {});
              api.experience.recent(user.id).then(setRecent).catch(() => setRecent([]));
            }
          }
        }
      }).catch(() => {
        if (!user && id) {
          const ids = read('wolfe_recently_viewed', []).filter((x: string) => x !== id);
          const next = [id, ...ids].slice(0, 8);
          write('wolfe_recently_viewed', next);
          setRecent(next.map((x: string) => products.find((y) => y.id === x)).filter(Boolean));
        }
      });

      api.productVariants(id).then((vars) => {
        setVariants(vars || []);
        if (vars && vars.length > 0) {
          const first = vars[0];
          setSelectedVariant(first);
          if (first.color) setSelectedColor(first.color);
          if (first.size) setSelectedSize(first.size);
          if (first.finish) setSelectedFinish(first.finish);
          if (first.material) setSelectedMaterial(first.material);
          if (first.imageUrl) setActiveImage(first.imageUrl);
        }
      }).catch(() => setVariants([]));

      api.relatedProducts(id).then(setRelated).catch(() => setRelated([]));


    }
  }, [id, user?.id]);

  // Extract distinct variant attributes
  const colors = useMemo(() => {
    return Array.from(new Set(variants.map((v) => v.color).filter(Boolean))) as string[];
  }, [variants]);

  const sizes = useMemo(() => {
    return Array.from(new Set(variants.map((v) => v.size).filter(Boolean))) as string[];
  }, [variants]);

  const finishes = useMemo(() => {
    return Array.from(new Set(variants.map((v) => v.finish).filter(Boolean))) as string[];
  }, [variants]);

  const materials = useMemo(() => {
    return Array.from(new Set(variants.map((v) => v.material).filter(Boolean))) as string[];
  }, [variants]);

  // Resolve matching variant when attribute selection changes
  const handleSelectAttribute = (type: 'color' | 'size' | 'finish' | 'material', value: string) => {
    let nextColor = selectedColor;
    let nextSize = selectedSize;
    let nextFinish = selectedFinish;
    let nextMaterial = selectedMaterial;

    if (type === 'color') nextColor = value;
    if (type === 'size') nextSize = value;
    if (type === 'finish') nextFinish = value;
    if (type === 'material') nextMaterial = value;

    if (type === 'color') setSelectedColor(value);
    if (type === 'size') setSelectedSize(value);
    if (type === 'finish') setSelectedFinish(value);
    if (type === 'material') setSelectedMaterial(value);

    // Find closest or exact matching variant
    const exact = variants.find(
      (v) =>
        (!nextColor || v.color === nextColor) &&
        (!nextSize || v.size === nextSize) &&
        (!nextFinish || v.finish === nextFinish) &&
        (!nextMaterial || v.material === nextMaterial)
    );

    const match = exact;

    if (match) {
      setSelectedVariant(match);
      if (match.imageUrl) setActiveImage(match.imageUrl);
      if (match.color) setSelectedColor(match.color);
      if (match.size) setSelectedSize(match.size);
      if (match.finish) setSelectedFinish(match.finish);
      if (match.material) setSelectedMaterial(match.material);
    } else if (variants.length) {
      // Never silently fall back to a different variant combination.
      // The cart must contain exactly the combination the customer selected.
      setSelectedVariant(null);
    }
  };

  const handleAddVariantToBag = () => {
    if (!p) return;
    if (selectedVariant) {
      onAdd(p.id, {
        id: selectedVariant.id,
        sku: selectedVariant.sku,
        title: selectedVariant.title || selectedVariant.optionValue,
        color: selectedVariant.color,
        material: selectedVariant.material,
        size: selectedVariant.size,
        finish: selectedVariant.finish,
        price: selectedVariant.price || selectedVariant.priceOverride || p.price,
        imageUrl: selectedVariant.imageUrl || activeImage,
      });
    } else {
      onAdd(p.id);
    }
  };

  if (!baseP && !liveProduct) {
    return <main className="container-w section">Product not found.</main>;
  }

  const p = liveProduct || baseP;

  // Parse category-specific attributes json
  let customAttrs: Record<string, string> = {};
  try {
    if (p.attributesJson) {
      customAttrs = JSON.parse(p.attributesJson);
    }
  } catch {}

  const currentPrice = selectedVariant?.price || selectedVariant?.priceOverride || p.price;
  const currentSku = selectedVariant?.sku || p.modelNumber || `WLF-${p.id}`;
  // Checkout is the stock authority; variant stockQuantity is admin-only metadata.
  const inStock = variants.length ? !!selectedVariant : true;
  const subscribeProductId = Number(p.id);

  const mapped = (x: any) => ({
    id: x.slug || x.id,
    serverId: Number(x.id),
    name: x.name,
    category: x.category,
    price: Number(x.price),
    image: x.imageUrl || x.image || '/catalog/brass-01.jpg',
    description: x.description || '',
    finish: x.finish,
    material: x.material || 'Metal',
    color: x.color || 'Brass',
    style: x.style || 'Modern',
    featured: x.featured === true,
    media: [],
  });

  const recentItems = user ? recent.map(mapped) : (recent as any);

  return (
    <main className="product-page container-w">
      <div className="product-detail-image">
        <img
          src={activeImage}
          alt={p.name}
          style={{ width: '100%', borderRadius: '4px', objectFit: 'cover' }}
        />
        {p.media?.length > 0 && (
          <div className="product-media-gallery">
            {p.media.map((src: string, i: number) => (
              <img
                key={src + i}
                src={src}
                alt={`${p.name} ${i + 1}`}
                loading="lazy"
                onClick={() => setActiveImage(src)}
                style={{ cursor: 'pointer', opacity: activeImage === src ? 1 : 0.6 }}
              />
            ))}
          </div>
        )}
      </div>

      <div className="product-detail-copy">
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <p className="eyebrow">{p.category}</p>
          {p.subcategory && <span style={{ color: 'var(--color-muted)', fontSize: '0.8rem' }}>· {p.subcategory}</span>}
          {p.brandName && <span style={{ color: 'var(--color-gold)', fontSize: '0.8rem', fontWeight: 600 }}>· {p.brandName}</span>}
        </div>

        <h1>{p.name}</h1>

        <div className="detail-price">
          {money(currentPrice)}
          {selectedVariant && selectedVariant.priceOverride && selectedVariant.priceOverride !== p.price && (
            <span style={{ fontSize: '0.85rem', color: 'var(--color-muted)', textDecoration: 'line-through', marginLeft: '10px' }}>
              {money(p.price)}
            </span>
          )}
        </div>

        <div style={{ fontSize: '0.85rem', color: 'var(--color-muted)', margin: '-10px 0 16px' }}>
          SKU: <strong>{currentSku}</strong> · Status:{' '}
          <span style={{ color: inStock ? '#2e7d32' : '#c62828', fontWeight: 600 }}>
            {inStock ? 'In Stock (Ready to dispatch)' : 'Out of stock'}
          </span>
        </div>

        <p className="detail-description">{p.description}</p>

        {/* Multi-attribute Variant Matrix Selectors */}
        {colors.length > 0 && (
          <div className="option-block">
            <span>Color / Tone</span>
            <div className="option-row">
              {colors.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => handleSelectAttribute('color', c)}
                  className={selectedColor === c ? 'selected' : ''}
                >
                  {selectedColor === c && <Check size={14} style={{ marginRight: '4px' }} />}
                  {c}
                </button>
              ))}
            </div>
          </div>
        )}

        {sizes.length > 0 && (
          <div className="option-block">
            <span>Size / Thickness</span>
            <div className="option-row">
              {sizes.map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => handleSelectAttribute('size', s)}
                  className={selectedSize === s ? 'selected' : ''}
                >
                  {selectedSize === s && <Check size={14} style={{ marginRight: '4px' }} />}
                  {s}
                </button>
              ))}
            </div>
          </div>
        )}

        {finishes.length > 1 && (
          <div className="option-block">
            <span>Surface Finish</span>
            <div className="option-row">
              {finishes.map((f) => (
                <button
                  key={f}
                  type="button"
                  onClick={() => handleSelectAttribute('finish', f)}
                  className={selectedFinish === f ? 'selected' : ''}
                >
                  {f}
                </button>
              ))}
            </div>
          </div>
        )}

        {materials.length > 1 && (
          <div className="option-block">
            <span>Material</span>
            <div className="option-row">
              {materials.map((m) => (
                <button
                  key={m}
                  type="button"
                  onClick={() => handleSelectAttribute('material', m)}
                  className={selectedMaterial === m ? 'selected' : ''}
                >
                  {m}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Category Specifications Card */}
        {Object.keys(customAttrs).length > 0 && (
          <div
            className="specs-block"
            style={{
              background: 'var(--color-bg-subtle, #fbfaf8)',
              border: '1px solid var(--color-border, #eae6df)',
              padding: '16px',
              borderRadius: '6px',
              margin: '20px 0',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, fontSize: '0.9rem', marginBottom: '8px' }}>
              <Layers size={16} /> Technical Specifications
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '8px', fontSize: '0.85rem' }}>
              {Object.entries(customAttrs).map(([k, v]) => (
                <div key={k}>
                  <span style={{ color: 'var(--color-muted)', textTransform: 'capitalize' }}>
                    {k.replace(/([A-Z])/g, ' $1')}:
                  </span>{' '}
                  <strong>{v}</strong>
                </div>
              ))}
              {p.dimensions && (
                <div>
                  <span style={{ color: 'var(--color-muted)' }}>Dimensions:</span> <strong>{p.dimensions}</strong>
                </div>
              )}
            </div>
          </div>
        )}

        <div className="detail-actions">
          {variants.length && !selectedVariant ? (
            <button type="button" className="btn btn-orange" disabled>
              Selected combination unavailable
            </button>
          ) : inStock ? (
            <button
              type="button"
              onClick={handleAddVariantToBag}
              className="btn btn-orange"
            >
              Add to bag
            </button>
          ) : (
            <button
              type="button"
              className="btn btn-orange"
              disabled={stockSubscribed}
              onClick={async () => {
                if (!user) { setStockMessage('Please sign in to get a back-in-stock alert.'); return; }
                try {
                  await api.experience.subscribeStock(user.id, subscribeProductId);
                  setStockSubscribed(true);
                  setStockMessage('We will notify you when this product is back in stock.');
                } catch (err: any) { setStockMessage(err.message || 'Unable to subscribe for stock alerts.'); }
              }}
            >
              {stockSubscribed ? 'Stock alert enabled' : 'Notify me when available'}
            </button>
          )}
          {stockMessage && <p className="checkout-login-note">{stockMessage}</p>}
          <button
            type="button"
            onClick={() => onWish(p.id)}
            className="btn btn-light"
            aria-label="Save to wishlist"
          >
            <Heart fill={wishes.includes(p.id) ? 'currentColor' : 'none'} size={18} />{' '}
            {wishes.includes(p.id) ? 'Saved' : 'Save'}
          </button>
        </div>

        <div style={{ marginTop: '20px', display: 'flex', gap: '20px', fontSize: '0.85rem', color: 'var(--color-muted)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Truck size={16} /> Express Dispatch
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <ShieldCheck size={16} /> Authentic Wolfe Guarantee
          </div>
        </div>
      </div>

      <ReviewPanel slug={p.id} user={user} />
      <AccessoryConfigurator
        slug={p.id}
        baseImage={activeImage}
        user={user}
        productId={Number((p as any).serverId ?? p.id) || 0}
        onAddConfigured={onAddConfigured}
      />
      <SpinViewer slug={p.id} />

      <section className="room-visualizer">
        <div>
          <p className="eyebrow">Room visualizer</p>
          <h2>Preview the detail in an architectural setting.</h2>
          <p>
            Experience how the material tone, hardware luster, and textures integrate
            seamlessly into modern living spaces.
          </p>
        </div>
        <div className="room-preview room-preview-demo">
          <img src={activeImage} alt={`${p.name} room preview`} />
        </div>
      </section>

      {related.length > 0 && (
        <section className="section">
          <SectionHead eyebrow="You may also like" title="Related architectural pieces." />
          <ProductGrid
            items={related.map(mapped) as any}
            onAdd={(addId) => onAdd(addId)}
            onWish={onWish}
            wishes={wishes}
            onCompare={() => {}}
            compared={[]}
          />
        </section>
      )}

      {recentItems.length > 1 && (
        <section className="section">
          <SectionHead eyebrow="Recently viewed" title="Pieces you explored." />
          <ProductGrid
            items={recentItems.filter((x: any) => x.id !== p.id) as any}
            onAdd={(addId) => onAdd(addId)}
            onWish={onWish}
            wishes={wishes}
            onCompare={() => {}}
            compared={[]}
          />
        </section>
      )}
    </main>
  );
}

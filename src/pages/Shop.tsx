import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { X, SlidersHorizontal, Search, RotateCcw } from 'lucide-react';
import { products, type Product as LocalProduct } from '../data';
import { api, type Product, type Brand, type Subcategory } from '../api';
import { useSeo } from '../hooks/useSeo';
import { read, write } from '../utils/storage';
import { FilterBlock } from '../components/commerce/FilterBlock';
import { ProductGrid } from '../App';

export interface ShopProps {
  onAdd: (id: string) => void;
  onWish: (id: string) => void;
  wishes: string[];
  onCompare: (id: string) => void;
  compared: string[];
}

export default function Shop({
  onAdd,
  onWish,
  wishes,
  onCompare,
  compared,
}: ShopProps) {
  useSeo(
    'Shop Master Catalog | Wolfe — Architectural Hardware & Surfaces',
    'Explore curated hardware, plywood, laminates, and modular kitchen accessories across luxury finishes.'
  );

  const loc = useLocation();
  const navigate = useNavigate();
  const params = new URLSearchParams(loc.search);

  const [q, setQ] = useState(params.get('q') || '');
  const [debouncedQ, setDebouncedQ] = useState(params.get('q') || '');
  const [cat, setCat] = useState(params.get('category') || 'All');
  const [subcat, setSubcat] = useState(params.get('subcategory') || 'All');
  const [brand, setBrand] = useState(params.get('brand') || 'All');
  const [finish, setFinish] = useState('All');
  const [material, setMaterial] = useState('All');
  const [color, setColor] = useState('All');
  const [style, setStyle] = useState('All');
  const [sort, setSort] = useState('featured');
  const [filtersOpen, setFiltersOpen] = useState(false);

  // Pagination & Progressive loading state
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState<LocalProduct[]>([]);
  const [totalCount, setTotalCount] = useState(0);

  // Filter options from backend
  const [options, setOptions] = useState<{
    categories: string[];
    subcategories: string[];
    brands: string[];
    materials: string[];
    colors: string[];
    styles: string[];
    finishes: string[];
  }>({
    categories: ['Hardware', 'Plywood', 'Laminates', 'Kitchen Accessories'],
    subcategories: [],
    brands: [],
    materials: [],
    colors: [],
    styles: [],
    finishes: [],
  });

  const filterDrawerRef = useRef<HTMLElement>(null);
  const loadMoreRef = useRef<HTMLDivElement>(null);

  // Synchronize category param if URL changes
  useEffect(() => {
    const urlParams = new URLSearchParams(loc.search);
    const categoryParam = urlParams.get('category');
    const qParam = urlParams.get('q');
    const subcategoryParam = urlParams.get('subcategory');
    const brandParam = urlParams.get('brand');

    if (categoryParam && categoryParam !== cat) setCat(categoryParam);
    if (qParam !== null && qParam !== q) {
      setQ(qParam);
      setDebouncedQ(qParam);
    }
    if (subcategoryParam && subcategoryParam !== subcat) setSubcat(subcategoryParam);
    if (brandParam && brandParam !== brand) setBrand(brandParam);
  }, [loc.search]);

  // Debounce search query
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQ(q);
    }, 300);
    return () => clearTimeout(timer);
  }, [q]);

  // Load dynamic filter metadata
  useEffect(() => {
    api.productFilters().then((res) => {
      if (res) {
        setOptions({
          categories: res.categories?.length ? res.categories : ['Hardware', 'Plywood', 'Laminates', 'Kitchen Accessories'],
          subcategories: res.subcategories || [],
          brands: res.brands || [],
          materials: res.materials || [],
          colors: res.colors || [],
          styles: res.styles || [],
          finishes: res.finishes || [],
        });
      }
    }).catch(() => {});
  }, []);

  // Close filter drawer on Escape key or outside click
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && filtersOpen) {
        setFiltersOpen(false);
      }
    };
    const handleOutsideClick = (e: MouseEvent | TouchEvent) => {
      if (filtersOpen && filterDrawerRef.current && !filterDrawerRef.current.contains(e.target as Node)) {
        setFiltersOpen(false);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    document.addEventListener('mousedown', handleOutsideClick);
    document.addEventListener('touchstart', handleOutsideClick);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('touchstart', handleOutsideClick);
    };
  }, [filtersOpen]);

  // Fetch products progressively
  const fetchProducts = useCallback(
    async (pageNum: number, isReset: boolean) => {
      setLoading(true);
      try {
        const res = await api.pagedProducts({
          category: cat,
          subcategory: subcat,
          brand: brand,
          q: debouncedQ,
          finish: finish,
          material: material,
          color: color,
          style: style,
          sort: sort,
          page: pageNum,
          pageSize: 20,
        });

        const mapped: LocalProduct[] = (res.content || []).map((s) => ({
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
          media: String(s.mediaUrls || '')
            .split(/\r?\n|,/)
            .map((x) => x.trim())
            .filter(Boolean),
        }));

        setTotalCount(res.totalElements || 0);
        setHasMore(res.hasNext);

        if (isReset) {
          setItems(mapped);
        } else {
          setItems((prev) => {
            const seen = new Set(prev.map((x) => x.id));
            const newItems = mapped.filter((x) => !seen.has(x.id));
            return [...prev, ...newItems];
          });
        }
      } catch {
        // Fallback to client-side data
        if (isReset) {
          let filtered = products.filter((p) => {
            if (cat !== 'All' && p.category.toLowerCase() !== cat.toLowerCase()) return false;
            if (subcat !== 'All' && p.subcategory && p.subcategory.toLowerCase() !== subcat.toLowerCase()) return false;
            if (brand !== 'All' && p.brandName && p.brandName.toLowerCase() !== brand.toLowerCase()) return false;
            if (finish !== 'All' && p.finish.toLowerCase() !== finish.toLowerCase()) return false;
            if (material !== 'All' && p.material.toLowerCase() !== material.toLowerCase()) return false;
            if (color !== 'All' && p.color.toLowerCase() !== color.toLowerCase()) return false;
            if (style !== 'All' && p.style.toLowerCase() !== style.toLowerCase()) return false;
            if (debouncedQ.trim()) {
              const ql = debouncedQ.toLowerCase();
              return (
                p.name.toLowerCase().includes(ql) ||
                p.description.toLowerCase().includes(ql) ||
                (p.subcategory && p.subcategory.toLowerCase().includes(ql)) ||
                (p.brandName && p.brandName.toLowerCase().includes(ql))
              );
            }
            return true;
          });
          setItems(filtered);
          setTotalCount(filtered.length);
          setHasMore(false);
        }
      } finally {
        setLoading(false);
      }
    },
    [cat, subcat, brand, debouncedQ, finish, material, color, style, sort]
  );

  // Reset and load first page on filter change
  useEffect(() => {
    setPage(0);
    fetchProducts(0, true);
  }, [fetchProducts]);

  // Load next page
  const loadNextPage = () => {
    if (!loading && hasMore) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchProducts(nextPage, false);
    }
  };

  // IntersectionObserver for infinite scrolling
  useEffect(() => {
    if (!hasMore || loading) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          loadNextPage();
        }
      },
      { threshold: 0.2 }
    );
    if (loadMoreRef.current) {
      observer.observe(loadMoreRef.current);
    }
    return () => observer.disconnect();
  }, [hasMore, loading, page]);

  const clearFilters = () => {
    setCat('All');
    setSubcat('All');
    setBrand('All');
    setFinish('All');
    setMaterial('All');
    setColor('All');
    setStyle('All');
    setQ('');
    setDebouncedQ('');
    navigate('/shop');
  };

  const cats = ['All', ...(options.categories || [])];
  const subcats = ['All', ...(options.subcategories || [])];
  const brands = ['All', ...(options.brands || [])];
  const finishes = ['All', ...(options.finishes || [])];
  const materials = ['All', ...(options.materials || [])];
  const colors = ['All', ...(options.colors || [])];
  const styles = ['All', ...(options.styles || [])];

  const activeFilterCount = [
    cat !== 'All',
    subcat !== 'All',
    brand !== 'All',
    finish !== 'All',
    material !== 'All',
    color !== 'All',
    style !== 'All',
    !!debouncedQ.trim(),
  ].filter(Boolean).length;

  return (
    <main>
      <div className="collection-hero">
        <div className="container-w">
          <p className="eyebrow">Wolfe Master Catalog</p>
          <h1>Architectural hardware & surface materials.</h1>
          <p>
            Explore solid brass hardware, BWP marine plywood, decorative architectural laminates,
            and modular kitchen systems.
          </p>
        </div>
      </div>

      <div className="container-w shop-layout">
        <aside
          ref={filterDrawerRef}
          className={`filters ${filtersOpen ? 'filters-open' : ''}`}
          aria-label="Product filters"
        >
          <div className="filter-title">
            <strong>
              Filter by {activeFilterCount > 0 && `(${activeFilterCount})`}
            </strong>
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              {activeFilterCount > 0 && (
                <button
                  type="button"
                  onClick={clearFilters}
                  className="icon-btn"
                  title="Reset all filters"
                  style={{ fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                >
                  <RotateCcw size={14} /> Reset
                </button>
              )}
              <button
                type="button"
                onClick={() => setFiltersOpen(false)}
                className="mobile-only icon-btn"
                aria-label="Close filters"
              >
                <X size={18} />
              </button>
            </div>
          </div>

          <FilterBlock title="Category">
            <div className="filter-list">
              {cats.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => {
                    setCat(c);
                    setSubcat('All');
                  }}
                  className={cat === c ? 'active' : ''}
                >
                  {c}
                </button>
              ))}
            </div>
          </FilterBlock>

          {subcats.length > 1 && (
            <FilterBlock title="Subcategory">
              <div className="filter-list">
                {subcats.map((sc) => (
                  <button
                    key={sc}
                    type="button"
                    onClick={() => setSubcat(sc)}
                    className={subcat === sc ? 'active' : ''}
                  >
                    {sc}
                  </button>
                ))}
              </div>
            </FilterBlock>
          )}

          {brands.length > 1 && (
            <FilterBlock title="Brand">
              <div className="filter-list">
                {brands.map((b) => (
                  <button
                    key={b}
                    type="button"
                    onClick={() => setBrand(b)}
                    className={brand === b ? 'active' : ''}
                  >
                    {b}
                  </button>
                ))}
              </div>
            </FilterBlock>
          )}

          <FilterBlock title="Finish">
            <div className="filter-list">
              {finishes.map((f) => (
                <button
                  key={f}
                  type="button"
                  onClick={() => setFinish(f)}
                  className={finish === f ? 'active' : ''}
                >
                  {f}
                </button>
              ))}
            </div>
          </FilterBlock>

          <FilterBlock title="Material">
            <div className="filter-list">
              {materials.map((m) => (
                <button
                  key={m}
                  type="button"
                  onClick={() => setMaterial(m)}
                  className={material === m ? 'active' : ''}
                >
                  {m}
                </button>
              ))}
            </div>
          </FilterBlock>

          <FilterBlock title="Colour">
            <div className="filter-list">
              {colors.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setColor(c)}
                  className={color === c ? 'active' : ''}
                >
                  {c}
                </button>
              ))}
            </div>
          </FilterBlock>

          <FilterBlock title="Style">
            <div className="filter-list">
              {styles.map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setStyle(s)}
                  className={style === s ? 'active' : ''}
                >
                  {s}
                </button>
              ))}
            </div>
          </FilterBlock>
        </aside>

        <div className="shop-results">
          <div className="shop-toolbar">
            <button
              type="button"
              className="filter-mobile-btn"
              onClick={() => setFiltersOpen(true)}
              aria-label="Open product filters"
            >
              <SlidersHorizontal size={16} /> Filters {activeFilterCount > 0 && `(${activeFilterCount})`}
            </button>
            <span>{totalCount} products found</span>
            <div className="shop-controls">
              <div className="shop-search-wrap">
                <Search size={16} />
                <input
                  value={q}
                  onChange={(e) => setQ(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && q.trim()) {
                      const next = [
                        q.trim(),
                        ...read('wolfe_recent_searches', []).filter(
                          (x: string) => x.toLowerCase() !== q.trim().toLowerCase()
                        ),
                      ].slice(0, 6);
                      write('wolfe_recent_searches', next);
                    }
                  }}
                  placeholder="Search products, brands, materials…"
                  aria-label="Search within catalog"
                />
              </div>
              <select
                value={sort}
                onChange={(e) => setSort(e.target.value)}
                aria-label="Sort products by"
              >
                <option value="featured">Featured</option>
                <option value="price_asc">Price: Low to High</option>
                <option value="price_desc">Price: High to Low</option>
                <option value="name_asc">Name: A–Z</option>
                <option value="newest">New Arrivals</option>
              </select>
            </div>
          </div>

          {items.length > 0 ? (
            <>
              <ProductGrid
                items={items}
                onAdd={onAdd}
                onWish={onWish}
                wishes={wishes}
                onCompare={onCompare}
                compared={compared}
              />
              <div ref={loadMoreRef} style={{ textAlign: 'center', padding: '30px 0' }}>
                {loading && <p style={{ color: 'var(--color-muted)' }}>Loading pieces…</p>}
                {!hasMore && items.length > 0 && (
                  <p style={{ color: 'var(--color-muted)', fontSize: '0.9rem' }}>
                    You have viewed all {totalCount} pieces in this collection.
                  </p>
                )}
                {hasMore && !loading && (
                  <button
                    type="button"
                    onClick={loadNextPage}
                    className="btn btn-light"
                    style={{ minWidth: '160px' }}
                  >
                    Load More
                  </button>
                )}
              </div>
            </>
          ) : !loading ? (
            <div className="empty-state" style={{ padding: '60px 20px', textAlign: 'center' }}>
              <p>No products match your selected filters.</p>
              <button
                type="button"
                onClick={clearFilters}
                className="btn btn-orange"
                style={{ marginTop: '16px' }}
              >
                Clear all filters
              </button>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '60px 0' }}>
              <p style={{ color: 'var(--color-muted)' }}>Loading catalog pieces…</p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}

import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Menu, X, Search, Heart, User, ShoppingBag, ChevronDown, ArrowRight } from 'lucide-react';
import { api } from '../../api';
import { money } from '../../utils/format';
import { read, write } from '../../utils/storage';
import { Badge } from './Badge';

interface HeaderProps {
  cartCount: number;
  wishCount: number;
  onCart: () => void;
}

export default function Header({ cartCount, wishCount, onCart }: HeaderProps) {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [exploreOpen, setExploreOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [recentSearches, setRecentSearches] = useState<string[]>(() => read('wolfe_recent_searches', []));

  const exploreRef = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLDivElement>(null);
  const navRef = useRef<HTMLElement>(null);

  // Global keydown for Escape
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (searchOpen) setSearchOpen(false);
        if (exploreOpen) setExploreOpen(false);
        if (open) setOpen(false);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [searchOpen, exploreOpen, open]);

  // Outside click listener for explore and search
  useEffect(() => {
    const handleOutsideClick = (e: MouseEvent | TouchEvent) => {
      const target = e.target as Node;
      if (exploreOpen && exploreRef.current && !exploreRef.current.contains(target)) {
        setExploreOpen(false);
      }
      if (searchOpen && searchRef.current && !searchRef.current.contains(target)) {
        setSearchOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    document.addEventListener('touchstart', handleOutsideClick);
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('touchstart', handleOutsideClick);
    };
  }, [exploreOpen, searchOpen]);

  useEffect(() => {
    let live = true;
    const q = search.trim();
    if (q.length < 2) {
      setSuggestions([]);
      return;
    }
    const t = setTimeout(() => {
      api.productSuggestions(q)
        .then((x) => {
          if (live) setSuggestions(x);
        })
        .catch(() => {});
    }, 180);
    return () => {
      live = false;
      clearTimeout(t);
    };
  }, [search]);

  const submitSearch = (q: string) => {
    const term = q.trim();
    if (!term) return;
    const next = [term, ...recentSearches.filter((x) => x.toLowerCase() !== term.toLowerCase())].slice(0, 6);
    setRecentSearches(next);
    write('wolfe_recent_searches', next);
    setSearchOpen(false);
    setSearch('');
    setOpen(false);
    navigate(`/shop?q=${encodeURIComponent(term)}`);
  };

  const navCategories = [
    { name: 'Hardware', path: '/shop?category=Hardware' },
    { name: 'Plywood', path: '/shop?category=Plywood' },
    { name: 'Laminates', path: '/shop?category=Laminates' },
    { name: 'Kitchen Accessories', path: '/shop?category=Kitchen+Accessories' },
  ];

  return (
    <>
      <div className="wolfe-topbar">
        Complimentary delivery on orders above ₹2,500 <span>•</span> Easy returns <span>•</span> WhatsApp us for design help
      </div>
      <header className="site-header">
        <div className="container-w header-main">
          <button
            className="icon-btn mobile-menu"
            onClick={() => {
              setOpen(!open);
              setExploreOpen(false);
              setSearchOpen(false);
            }}
            aria-label="Toggle navigation menu"
            aria-expanded={open}
            aria-controls="main-site-nav"
          >
            {open ? <X /> : <Menu />}
          </button>
          <Link to="/" className="brand" onClick={() => { setOpen(false); setExploreOpen(false); }}>
            <img src="/wolfe-logo-crop.png" alt="Wolfe — The Jewel of Villa" />
          </Link>
          <nav id="main-site-nav" ref={navRef} className={`${open ? 'mobile-nav-open' : 'mobile-nav-closed'} main-nav`}>
            {navCategories.map((c) => (
              <Link key={c.name} onClick={() => { setOpen(false); setExploreOpen(false); }} to={c.path}>
                {c.name}
              </Link>
            ))}
            <div className="nav-dropdown-wrap" ref={exploreRef} style={{ position: 'relative', display: 'inline-block' }}>
              <button
                type="button"
                className="nav-explore-btn"
                onClick={() => setExploreOpen(!exploreOpen)}
                aria-expanded={exploreOpen}
                aria-haspopup="true"
                style={{
                  background: 'none',
                  border: 'none',
                  font: 'inherit',
                  cursor: 'pointer',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '8px 0',
                  color: 'inherit'
                }}
              >
                Explore <ChevronDown size={14} style={{ transform: exploreOpen ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} />
              </button>
              {exploreOpen && (
                <div className="nav-menu nav-menu-open" style={{ display: 'block', position: 'absolute', top: '100%', left: 0, zIndex: 1000 }}>
                  <Link to="/shop" onClick={() => { setExploreOpen(false); setOpen(false); }}>All Products</Link>
                  <Link to="/bundles" onClick={() => { setExploreOpen(false); setOpen(false); }}>Curated Bundles</Link>
                  <Link to="/consultation" onClick={() => { setExploreOpen(false); setOpen(false); }}>Design Consultation</Link>
                  <Link to="/custom-design" onClick={() => { setExploreOpen(false); setOpen(false); }}>Custom Design</Link>
                  <Link to="/quote" onClick={() => { setExploreOpen(false); setOpen(false); }}>Project Quote</Link>
                  <Link to="/catalogue" onClick={() => { setExploreOpen(false); setOpen(false); }}>Catalogue</Link>
                  <Link to="/journal" onClick={() => { setExploreOpen(false); setOpen(false); }}>Journal</Link>
                </div>
              )}
            </div>
          </nav>
          <div className="header-actions">
            <button
              type="button"
              className="header-search-button"
              onClick={() => {
                setSearchOpen(!searchOpen);
                setExploreOpen(false);
                setOpen(false);
              }}
              aria-label="Search catalog"
              aria-expanded={searchOpen}
            >
              <Search size={19} />
            </button>
            <Link to="/wishlist" className="relative" aria-label="Wishlist" onClick={() => setOpen(false)}>
              <Heart size={19} />
              {wishCount > 0 && <Badge n={wishCount} />}
            </Link>
            <Link to="/account" aria-label="Account" onClick={() => setOpen(false)}>
              <User size={19} />
            </Link>
            <button onClick={onCart} className="relative" aria-label="Shopping bag">
              <ShoppingBag size={19} />
              {cartCount > 0 && <Badge n={cartCount} />}
            </button>
          </div>
        </div>
      </header>

      {searchOpen && (
        <div className="search-panel" ref={searchRef}>
          <div className="container-w search-panel-inner">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                submitSearch(search);
              }}
              className="search-form"
            >
              <Search size={18} />
              <input
                autoFocus
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search hardware, plywood, laminates, kitchen accessories…"
              />
              <button
                type="button"
                onClick={() => {
                  setSearchOpen(false);
                  setSearch('');
                }}
                aria-label="Close search"
              >
                <X size={18} />
              </button>
            </form>
            {search.trim().length < 2 && recentSearches.length > 0 && (
              <div className="search-section">
                <span className="search-label">Recent searches</span>
                <div className="search-chips">
                  {recentSearches.map((x) => (
                    <button key={x} type="button" onClick={() => submitSearch(x)}>
                      {x}
                    </button>
                  ))}
                </div>
              </div>
            )}
            {suggestions.length > 0 && (
              <div className="search-results">
                <span className="search-label">Suggestions</span>
                {suggestions.map((x) => (
                  <Link
                    key={x.id}
                    to={`/product/${x.slug}`}
                    onClick={() => {
                      setSearchOpen(false);
                      setSearch('');
                    }}
                    className="search-result"
                  >
                    <img src={x.imageUrl || '/wolfe-logo.png'} alt="" />
                    <span>
                      <strong>{x.name}</strong>
                      <small>
                        {x.category} {x.subcategory ? `· ${x.subcategory}` : ''} · {money(Number(x.price))}
                      </small>
                    </span>
                    <ArrowRight size={15} />
                  </Link>
                ))}
              </div>
            )}
            {search.trim().length >= 2 && !suggestions.length && (
              <p className="search-empty">No matching products found.</p>
            )}
          </div>
        </div>
      )}
    </>
  );
}

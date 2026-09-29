import React from 'react';
import { Link } from 'react-router-dom';
import { Heart } from 'lucide-react';
import { Product, products } from '../../data';
import { money } from '../../utils/format';

export interface ProductCardProps {
  p: typeof products[number];
  onAdd: (id: string) => void;
  onWish: (id: string) => void;
  wishes: string[];
  onCompare: (id: string) => void;
  compared?: boolean;
  featured?: boolean;
}

export function ProductCard({
  p,
  onAdd,
  onWish,
  wishes,
  onCompare,
  compared = false,
  featured = false,
}: ProductCardProps) {
  return (
    <article className={`product-card ${featured ? 'product-featured' : ''}`}>
      <div className="product-media">
        {p.featured && <span className="product-badge">Featured</span>}
        <Link to={`/product/${p.id}`} className="product-image">
          <img src={p.image} alt={p.name} loading="lazy" />
        </Link>
        <button onClick={() => onWish(p.id)} className="wish-btn" aria-label="Add to wishlist">
          <Heart size={18} fill={wishes.includes(p.id) ? 'currentColor' : 'none'} />
        </button>
        <button onClick={() => onAdd(p.id)} className="quick-add">
          Add to bag
        </button>
        <button onClick={() => onCompare(p.id)} className="compare-add" aria-pressed={compared}>
          {compared ? 'Compared' : 'Compare'}
        </button>
      </div>
      <div className="product-info">
        <div>
          <Link to={`/product/${p.id}`} className="product-name">
            {p.name}
          </Link>
          <p className="product-meta">{p.finish}</p>
        </div>
        <p className="product-price">{money(p.price)}</p>
      </div>
    </article>
  );
}

export default ProductCard;

import { products } from '../data';
import { api, type Customer } from '../api';
import { read } from '../utils/storage';
import { useSeo } from '../hooks/useSeo';
import { ProductGrid } from '../App';

export interface WishlistProps {
  wishes: string[];
  onWish: (id: string) => void;
  onAdd: (id: string) => void;
}

export function Wishlist({ wishes, onWish, onAdd }: WishlistProps) {
  useSeo('Wishlist | Wolfe — The Jewel of Villa', 'Your saved architectural hardware pieces and finishes.');
  const user = read<Customer | null>('wolfe_user', null);
  const items = products.filter(p => wishes.includes(p.id));

  const clear = async () => {
    if (user) await api.wishlist.clear(user.id).catch(() => { });
    wishes.forEach(onWish);
  };

  return (
    <main className="container-w section">
      <p className="eyebrow">Saved pieces</p>
      <div className="admin-head">
        <div>
          <h1>Wishlist</h1>
          <p>Keep your considered pieces together.</p>
        </div>
        {items.length > 0 && (
          <button className="btn btn-light" onClick={clear}>
            Clear wishlist
          </button>
        )}
      </div>
      <div className="wishlist-grid">
        {items.length ? (
          <ProductGrid items={items} onAdd={onAdd} onWish={onWish} wishes={wishes} />
        ) : (
          <p className="empty-state">Your wishlist is empty.</p>
        )}
      </div>
    </main>
  );
}

export default Wishlist;

import React, { useState, useEffect } from 'react';
import { Star } from 'lucide-react';
import { api, type Customer } from '../../api';
import { SectionHead } from '../layout/SectionHead';

export interface ReviewPanelProps {
  slug: string;
  user: Customer | null;
}

export function ReviewPanel({ slug, user }: ReviewPanelProps) {
  const [data, setData] = useState<any>({ average: 0, count: 0, reviews: [] });
  const [rating, setRating] = useState(5);
  const [review, setReview] = useState('');
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [mine, setMine] = useState<any | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);

  useEffect(() => {
    api.reviews.get(slug, page, 20).then(setData).catch(() => {});
    if (user) api.reviews.mine(slug).then(setMine).catch(() => setMine(null));
    else setMine(null);
  }, [slug, sent, page, user]);

  const submit = async (e: any) => {
    e.preventDefault();
    if (!user) {
      setError('Please sign in to leave a review.');
      return;
    }
    try {
      if (editingId != null) {
        await api.reviews.update(editingId, { rating, review });
        setEditingId(null);
      } else {
        await api.reviews.create(slug, { rating, review });
      }
      setSent(true);
      setReview('');
      setError('');
    } catch (err: any) {
      setError(err.message || 'Unable to submit review');
    }
  };

  return (
    <section className="container-w section review-section">
      <SectionHead eyebrow="Customer reviews" title={`${data.average.toFixed(1)} / 5 · ${data.count} reviews`} />
      {data.reviews.map((r: any) => (
        <div className="testimonial" key={r.id}>
          <div className="stars">
            {[1, 2, 3, 4, 5].map((n) => (
              <Star key={n} size={14} fill={n <= r.rating ? 'currentColor' : 'none'} />
            ))}
          </div>
          <p>{r.review}</p>
        </div>
      ))}
      <form onSubmit={submit} className="review-form">
        <h3>Share your experience</h3>
        <select value={rating} onChange={(e) => setRating(Number(e.target.value))} className="field">
          <option value={5}>5 stars</option>
          <option value={4}>4 stars</option>
          <option value={3}>3 stars</option>
          <option value={2}>2 stars</option>
          <option value={1}>1 star</option>
        </select>
        <textarea
          required
          minLength={10}
          maxLength={1000}
          value={review}
          onChange={(e) => setReview(e.target.value)}
          placeholder="Tell us about the product"
          className="field textarea"
        />
        <button className="btn btn-orange">{editingId != null ? 'Update review' : 'Submit review'}</button>
        {mine && <button type="button" className="btn btn-light" onClick={() => { setEditingId(null); setRating(mine.rating); setReview(mine.review); setError(''); }}>Edit my review</button>}
        {mine && <button type="button" className="btn btn-light" onClick={async () => {
          try { await api.reviews.remove(mine.id); setMine(null); setSent(v => !v); setError(''); }
          catch (err: any) { setError(err.message || 'Unable to delete review'); }
        }}>Delete my review</button>
        {sent && <p className="checkout-login-note">Review submitted for approval.</p>}
        {data.totalPages > 1 && <div className="admin-actions" style={{ marginTop: 16 }}>
        <button className="btn btn-light" disabled={page <= 0} onClick={() => setPage(p => Math.max(0, p - 1))}>Previous</button>
        <span>Page {page + 1} of {data.totalPages}</span>
        <button className="btn btn-light" disabled={page + 1 >= data.totalPages} onClick={() => setPage(p => p + 1)}>Next</button>
      </div>}
      {error && <p className="error-message">{error}</p>}
      </form>
    </section>
  );
}

export default ReviewPanel;

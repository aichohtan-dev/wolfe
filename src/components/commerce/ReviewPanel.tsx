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

  useEffect(() => {
    api.reviews.get(slug).then(setData).catch(() => {});
  }, [slug, sent]);

  const submit = async (e: any) => {
    e.preventDefault();
    if (!user) {
      setError('Please sign in to leave a review.');
      return;
    }
    try {
      await api.reviews.create(slug, { rating, review });
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
        <button className="btn btn-orange">Submit review</button>
        {sent && <p className="checkout-login-note">Review submitted for approval.</p>}
        {error && <p className="error-message">{error}</p>}
      </form>
    </section>
  );
}

export default ReviewPanel;

import { useEffect, useState } from 'react';
import { api } from '../../api';

export function VisualHero() {
  const [items, setItems] = useState<any[]>([]);
  const [index, setIndex] = useState(0);

  useEffect(() => {
    api.visualContent('HERO').then(setItems).catch(() => setItems([]));
  }, []);

  if (!items.length) return null;

  const x = items[index % items.length];
  return (
    <section className="visual-hero">
      <div className="visual-hero-media">
        {x.mediaType === 'VIDEO' ? (
          <video src={x.mediaUrl} poster={x.posterUrl || undefined} autoPlay muted loop playsInline controls={false} />
        ) : (
          <img src={x.mediaUrl} alt={x.title} />
        )}
      </div>
      <div className="visual-hero-copy">
        <p className="eyebrow">Wolfe visual story</p>
        <h2>{x.title}</h2>
        {x.subtitle && <p>{x.subtitle}</p>}
        {x.linkUrl && <a className="btn btn-orange" href={x.linkUrl}>Explore</a>}
      </div>
      {items.length > 1 && (
        <div className="visual-hero-dots">
          {items.map((_: any, i: number) => (
            <button
              type="button"
              key={i}
              aria-label={`Show visual ${i + 1}`}
              className={i === index ? 'active' : ''}
              onClick={() => setIndex(i)}
            />
          ))}
        </div>
      )}
    </section>
  );
}

export default VisualHero;

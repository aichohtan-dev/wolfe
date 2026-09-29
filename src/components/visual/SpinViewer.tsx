import { useEffect, useState } from 'react';
import { api } from '../../api';

export function SpinViewer({ slug }: { slug: string }) {
  const [frames, setFrames] = useState<any[]>([]);
  const [index, setIndex] = useState(0);

  useEffect(() => {
    api.experience.spin(slug).then(setFrames).catch(() => setFrames([]));
  }, [slug]);

  if (frames.length < 2) return null;

  return (
    <section className="spin-viewer">
      <div>
        <p className="eyebrow">360° view</p>
        <h2>See every angle.</h2>
        <p>Rotate through the product frames.</p>
      </div>
      <img src={frames[index].imageUrl} alt="Product 360 degree view" />
      <input
        aria-label="Rotate product"
        type="range"
        min="0"
        max={frames.length - 1}
        value={index}
        onChange={e => setIndex(Number(e.target.value))}
      />
      <div className="spin-actions">
        <button className="btn btn-light" onClick={() => setIndex((index - 1 + frames.length) % frames.length)}>
          Previous
        </button>
        <button className="btn btn-light" onClick={() => setIndex((index + 1) % frames.length)}>
          Next
        </button>
      </div>
    </section>
  );
}

export default SpinViewer;

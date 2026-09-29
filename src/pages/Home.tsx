import React from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  ShieldCheck,
  Truck,
  RotateCcw,
  MessageCircle,
  Star,
} from 'lucide-react';
import { products } from '../data';
import { useSeo } from '../hooks/useSeo';
import { VisualHero } from '../components/visual/VisualHero';
import { SectionHead } from '../components/layout/SectionHead';
import { Trust } from '../components/layout/Trust';
import { ProductGrid } from '../App';

const editorial = [
  {
    title: 'Handles',
    copy: 'Refined pulls for kitchens, wardrobes and furniture.',
    image:
      'https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1200&q=85',
    category: 'Handles',
  },
  {
    title: 'Knobs',
    copy: 'Small details with a distinct point of view.',
    image:
      'https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=1200&q=85',
    category: 'Knobs',
  },
  {
    title: 'Hooks',
    copy: 'Functional forms designed to live beautifully.',
    image:
      'https://images.unsplash.com/photo-1617104678098-de229db51175?auto=format&fit=crop&w=1200&q=85',
    category: 'Hooks',
  },
];

export interface HomeProps {
  onAdd: (id: string) => void;
  onWish: (id: string) => void;
  wishes: string[];
  onCompare: (id: string) => void;
  compared: string[];
}

export default function Home({
  onAdd,
  onWish,
  wishes,
  onCompare,
  compared,
}: HomeProps) {
  useSeo(
    'Wolfe — The Jewel of Villa',
    'Premium architectural hardware for considered spaces. Handles, knobs and hooks in refined finishes.'
  );

  return (
    <main>
      <section className="hero">
        <div className="hero-image"></div>
        <div className="hero-overlay">
          <p className="eyebrow">Architectural hardware for considered spaces</p>
          <h1>
            Details make
            <br />
            the room.
          </h1>
          <p>
            Premium handles, knobs and hooks designed to bring quiet character to
            kitchens, wardrobes and living spaces.
          </p>
          <Link to="/shop" className="btn btn-orange">
            Explore the collection <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      <VisualHero />

      <section className="section container-w">
        <SectionHead
          eyebrow="Shop by category"
          title="The details, curated."
          link="View all"
          to="/shop"
        />
        <div className="category-grid">
          {editorial.map((x) => (
            <Link
              to={`/shop?category=${x.category}`}
              className="category-card"
              key={x.title}
            >
              <img src={x.image} alt="" />
              <div>
                <span>{x.title}</span>
                <p>{x.copy}</p>
                <ArrowRight size={18} />
              </div>
            </Link>
          ))}
        </div>
      </section>

      <section className="section section-soft">
        <div className="container-w">
          <SectionHead
            eyebrow="Featured collection"
            title="Made to be noticed."
            link="Shop all"
            to="/shop"
          />
          <ProductGrid
            items={products.slice(0, 4)}
            onAdd={onAdd}
            onWish={onWish}
            wishes={wishes}
            onCompare={onCompare}
            compared={compared}
          />
        </div>
      </section>

      <section className="inspiration container-w">
        <div className="inspiration-copy">
          <p className="eyebrow">Inspiration starts here</p>
          <h2>Moodboards for spaces with character.</h2>
          <p>
            Explore considered combinations of finishes, forms and materials, and
            find the details that belong in your space.
          </p>
          <Link to="/journal" className="text-link">
            Explore inspiration <ArrowRight size={16} />
          </Link>
        </div>
        <div className="inspiration-image">
          <img
            src="https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1600&q=85"
            alt="Warm contemporary interior"
          />
        </div>
      </section>

      <section className="section container-w">
        <SectionHead
          eyebrow="Shop our favourites"
          title="Pieces worth living with."
        />
        <ProductGrid
          items={products.slice(2, 6)}
          onAdd={onAdd}
          onWish={onWish}
          wishes={wishes}
          onCompare={onCompare}
          compared={compared}
        />
      </section>

      <section className="service-banner">
        <div className="container-w service-inner">
          <div>
            <p className="eyebrow">Need a second opinion?</p>
            <h2>Let's choose the details together.</h2>
            <p>
              Book a complimentary consultation for finish, sizing and quantity
              guidance.
            </p>
          </div>
          <Link to="/consultation" className="btn btn-light">
            Book a consultation <ArrowRight size={16} />
          </Link>
        </div>
      </section>

      <section className="catalogue container-w">
        <div>
          <p className="eyebrow">Wolfe catalogue</p>
          <h2>See the complete collection.</h2>
          <p>
            Browse finishes, forms and specifications in one considered
            catalogue.
          </p>
        </div>
        <Link to="/catalogue" className="btn btn-orange">
          Request catalogue
        </Link>
      </section>

      <section className="trust-section">
        <div className="container-w">
          <p className="eyebrow center">The Wolfe assurance</p>
          <div className="trust-grid">
            <Trust icon={<ShieldCheck />} title="Secure transactions" />
            <Trust icon={<Truck />} title="Reliable dispatch" />
            <Trust icon={<RotateCcw />} title="Easy returns" />
            <Trust icon={<MessageCircle />} title="Design support" />
          </div>
        </div>
      </section>

      <section className="testimonials container-w">
        <SectionHead
          eyebrow="Hear from our community"
          title="Details that stay with you."
        />
        <div className="testimonial-grid">
          {[
            'Beautiful quality and beautifully packed. The finish is exactly what we needed.',
            'The collection makes it very easy to coordinate a whole kitchen without it feeling too uniform.',
            'Helpful advice, fast communication and pieces that feel much more premium in person.',
          ].map((x, i) => (
            <div className="testimonial" key={i}>
              <div className="stars">
                {[1, 2, 3, 4, 5].map((n) => (
                  <Star key={n} size={14} fill="currentColor" />
                ))}
              </div>
              <p>“{x}”</p>
              <span>Verified customer</span>
            </div>
          ))}
        </div>
      </section>

      <section className="quote-section">
        <div className="container-w quote-inner">
          <div>
            <p className="eyebrow">Create your own quote</p>
            <h2>A simpler way to plan your project.</h2>
            <p>
              Add products to your bag, then request a project quote from our
              team.
            </p>
          </div>
          <Link to="/quote" className="btn btn-orange">
            Start a quote <ArrowRight size={16} />
          </Link>
        </div>
      </section>
    </main>
  );
}

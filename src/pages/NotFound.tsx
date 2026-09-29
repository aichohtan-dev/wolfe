import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { useSeo } from '../hooks/useSeo';

export function NotFound() {
    useSeo('Page Not Found | Wolfe — The Jewel of Villa', 'The page you are looking for does not exist or has moved.');
    return (
        <main className="container-w not-found-page">
            <p className="eyebrow">404 Error</p>
            <h1>Page not found.</h1>
            <p>The piece or page you are looking for does not exist, has moved, or is temporarily unavailable.</p>
            <Link to="/shop" className="btn btn-orange">Explore the collection <ArrowRight size={16} /></Link>
        </main>
    );
}

export default NotFound;

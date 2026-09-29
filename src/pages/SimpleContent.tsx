import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { useSeo } from '../hooks/useSeo';

export function SimpleContent({ title, eyebrow, copy }: {
    title: string;
    eyebrow: string;
    copy: string;
}) {
    useSeo(`${title} | Wolfe — The Jewel of Villa`, copy);
    return (
        <main className="container-w narrow-page">
            <p className="eyebrow">{eyebrow}</p>
            <h1>{title}</h1>
            <p>{copy}</p>
            <Link to="/shop" className="btn btn-orange">Shop collection <ArrowRight size={16} /></Link>
        </main>
    );
}

export default SimpleContent;

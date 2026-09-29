import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';

export function SectionHead({ eyebrow, title, link, to }: {
    eyebrow: string;
    title: string;
    link?: string;
    to?: string;
}) {
    return (
        <div className="section-head">
            <div>
                <p className="eyebrow">{eyebrow}</p>
                <h2>{title}</h2>
            </div>
            {link && to && (
                <Link to={to} className="text-link">
                    {link} <ArrowRight size={15} />
                </Link>
            )}
        </div>
    );
}

import { useEffect } from 'react';

export function useSeo(title: string, description: string) {
    useEffect(() => {
        document.title = title;
        const meta = document.querySelector('meta[name=description]');
        if (meta) {
            meta.setAttribute('content', description);
        }
        let canonical = document.querySelector('link[rel=canonical]') as HTMLLinkElement | null;
        if (!canonical) {
            canonical = document.createElement('link');
            canonical.rel = 'canonical';
            document.head.appendChild(canonical);
        }
        canonical.href = window.location.origin + window.location.pathname;
        const setMeta = (selector: string, attr: string, value: string) => { let el = document.querySelector(selector); if (!el) { el = document.createElement('meta'); el.setAttribute(attr, selector.includes('property=') ? selector.split('=')[1].replace(']','') : selector.includes('name=') ? selector.split('=')[1].replace(']','') : attr); document.head.appendChild(el); } el.setAttribute(attr, value); };
        setMeta('meta[property=og:title]', 'content', title);
        setMeta('meta[property=og:description]', 'content', description);
        setMeta('meta[property=og:url]', 'content', canonical.href);
        setMeta('meta[property=og:type]', 'content', 'website');
        setMeta('meta[name=twitter:title]', 'content', title);
        setMeta('meta[name=twitter:description]', 'content', description);
        setMeta('meta[name=twitter:card]', 'content', 'summary_large_image');
        return () => { };
    }, [title, description]);
}

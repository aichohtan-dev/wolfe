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
        return () => { };
    }, [title, description]);
}

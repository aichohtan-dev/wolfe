export function CookieConsentBanner({
    onChoice,
    onClose,
    showClose = false
}: {
    onChoice: (val: 'accepted' | 'essential_only') => void;
    onClose?: () => void;
    showClose?: boolean;
}) {
    return (
        <aside className="cookie-banner" role="dialog" aria-label="Storage and privacy preferences">
            <div>
                <strong style={{ fontSize: '13px', display: 'block', marginBottom: '6px' }}>Browser storage &amp; privacy</strong>
                <p>Wolfe uses browser local storage for essential commerce functions (keeping your shopping bag, wishlist, and session state active). Optional preferences help refine your experience. You can update your choice anytime from the footer.</p>
            </div>
            <div className="cookie-banner-actions">
                <button type="button" className="btn btn-orange" onClick={() => onChoice('accepted')}>Accept all</button>
                <button type="button" className="btn btn-outline" onClick={() => onChoice('essential_only')}>Essential only</button>
                {showClose && onClose && <button type="button" className="btn btn-light" onClick={onClose}>Close</button>}
            </div>
        </aside>
    );
}

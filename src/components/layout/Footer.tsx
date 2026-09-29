import { Link } from 'react-router-dom';

export function Footer({ onOpenConsent }: { onOpenConsent?: () => void }) {
    return (
        <footer className="site-footer">
            <div className="container-w footer-grid">
                <div className="footer-brand">
                    <img src="/wolfe-logo.png" alt="Wolfe — The Jewel of Villa" loading="lazy" />
                    <p>Premium architectural hardware for considered spaces.</p>
                </div>
                <div>
                    <p className="footer-title">Explore</p>
                    <Link to="/shop">Shop</Link>
                    <Link to="/wishlist">Wishlist</Link>
                    <Link to="/consultation">Consultation</Link>
                    <Link to="/custom-design">Custom design</Link>
                    <Link to="/quote">Project quote</Link>
                    <Link to="/bundles">Bundles</Link>
                    <Link to="/catalogue">Catalogue</Link>
                </div>
                <div>
                    <p className="footer-title">Customer</p>
                    <Link to="/account">Account</Link>
                    <Link to="/orders">Orders</Link>
                    <Link to="/notifications">Notifications</Link>
                    <Link to="/shop">Shipping & Returns</Link>
                    <Link to="/privacy-policy">Privacy Policy</Link>
                    <Link to="/terms">Terms & Conditions</Link>
                    {onOpenConsent && <button type="button" onClick={onOpenConsent}>Storage preferences</button>}
                </div>
                <div className="footer-address">
                    <p className="footer-title">Visit us</p>
                    <strong>Hinglaj Hardware</strong>
                    <p>10 B Road, Opp. Barmer Bhavan<br />Sardarpura, Jodhpur-jaipur, Rajasthan, India</p>
                    <p>For product and project enquiries, contact the Wolfe team.</p>
                </div>
            </div>
            <div className="container-w footer-bottom">
                <span>© {new Date().getFullYear()} Wolfe Team. All rights reserved.</span>
                <span>Hinglaj Hardware · Jodhpur-jaipur</span>
            </div>
        </footer>
    );

}

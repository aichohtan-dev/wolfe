import React, { useEffect } from 'react';

export default function Terms() {
  useEffect(() => {
    document.title = 'Terms & Conditions | Wolfe — The Jewel of Villa';
    const meta = document.querySelector('meta[name=description]');
    if (meta) meta.setAttribute('content', 'Wolfe and Hinglaj Hardware terms and conditions for website use, products, ordering, payment, delivery, and returns.');
  }, []);

  return <main className="container-w section legal-page">
    <p className="eyebrow">Wolfe</p>
    <h1>Terms &amp; Conditions</h1>
    <p className="detail-description">These terms describe the basic rules for using the Wolfe website and purchasing products from Hinglaj Hardware.</p>
    <h2>Website use</h2>
    <p>You may use this website for lawful browsing, product research, enquiries, and purchases. Do not misuse the website, attempt unauthorised access, interfere with its operation, or submit unlawful or misleading information.</p>
    <h2>Products and pricing</h2>
    <p>Product descriptions, finishes, dimensions, availability and prices may change. Project-specific requirements should be confirmed with the Wolfe team.</p>
    <h2>Orders</h2>
    <p>An order request is subject to product availability and confirmation. We may contact you to verify delivery details or clarify an order before fulfilment.</p>
    <h2>Payments and delivery</h2>
    <p>Wolfe currently offers cash on delivery. Delivery timing, serviceability and charges may depend on destination and the applicable order.</p>
    <h2>Returns and cancellations</h2>
    <p>Cancellation and return requests are handled according to the order status and the applicable Wolfe return process. Product-specific conditions may apply.</p>
    <h2>Intellectual property</h2>
    <p>Wolfe branding, product imagery, copy, layouts and other website content belong to their respective owners and may not be copied or commercially reused without permission.</p>
    <h2>Contact</h2>
    <p>Hinglaj Hardware, 10B Road, Opp. Barmer Bhavan, Sardarpura, Jodhpur, Rajasthan, India.</p>
    <p><small>Last updated: September 28, 2026.</small></p>
  </main>;
}

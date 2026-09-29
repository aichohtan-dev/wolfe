import React, { useEffect } from 'react';

export default function PrivacyPolicy() {
  useEffect(() => {
    document.title = 'Privacy Policy | Wolfe — The Jewel of Villa';
    const meta = document.querySelector('meta[name=description]');
    if (meta) meta.setAttribute('content', 'Wolfe and Hinglaj Hardware privacy policy explaining information collection, usage, browser storage, and customer privacy rights.');
  }, []);

  return (
    <main className="container-w section legal-page">
      <p className="eyebrow">Wolfe</p>
      <h1>Privacy Policy</h1>
      <p className="detail-description">This policy explains how Wolfe and Hinglaj Hardware handle information provided through this website and our commerce services.</p>
      <h2>Information we collect</h2>
      <p>We may collect information you provide when creating an account, placing an order, requesting a quote or consultation, contacting us, or managing your delivery details.</p>
      <h2>How we use information</h2>
      <p>We use information to provide products and services, process and deliver orders, support your account, respond to enquiries, prevent misuse, maintain security, and improve the website and customer experience.</p>
      <h2>Cookies and local storage</h2>
      <p>The website may use browser storage for functions such as your cart, wishlist, recent searches, comparison list, and signed-in session state. Optional analytics or marketing technologies will be introduced with appropriate notice and consent where required.</p>
      <h2>Sharing information</h2>
      <p>We may share information with service providers who help operate the website, fulfil orders, provide delivery or customer support, or maintain infrastructure.</p>
      <h2>Security and retention</h2>
      <p>We use reasonable technical and organisational measures to protect information and retain information only as reasonably necessary for the purposes described above and applicable legal obligations.</p>
      <h2>Your choices</h2>
      <p>You may contact Wolfe to request access to, correction of, or deletion of personal information, subject to applicable requirements.</p>
      <h2>Contact</h2>
      <p>Hinglaj Hardware, 10B Road, Opp. Barmer Bhavan, Sardarpura, Jodhpur, Rajasthan, India.</p>
      <p><small>Last updated: September 28, 2026.</small></p>
    </main>
  );
}
export { PrivacyPolicy };

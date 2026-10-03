import { useState } from 'react';
import { useSeo } from '../../hooks/useSeo';
import { api, type Customer } from '../../api';
import { TurnstileWidget } from '../../components/security/TurnstileWidget';

export function QuoteRequest({ user }: {
    user: Customer | null;
}) {
    useSeo('Project Quote | Wolfe — The Jewel of Villa', 'Request a tailored architectural hardware project quote.');
    const [message, setMessage] = useState('');
    const [hp, setHp] = useState('');
    const [sent, setSent] = useState(false);
    const [captchaToken, setCaptchaToken] = useState('');

    const submit = async (e: any) => {
        e.preventDefault();
        if (!user || hp) return;
        await api.quotes.create(user.id, { message, captchaToken });
        setSent(true);
        setMessage('');
    };

    return (
        <main className="container-w narrow-page">
            <p className="eyebrow">Project quote</p>
            <h1>Tell us about your project.</h1>
            {!user ? (
                <p>Please sign in to request a quote.</p>
            ) : sent ? (
                <div className="success-box">
                    <h2>Request received.</h2>
                    <p>Our team will contact you with the next steps.</p>
                </div>
            ) : (
                <form onSubmit={submit} className="checkout-form">
                    <input type="text" name="_hp" style={{ display: 'none' }} tabIndex={-1} autoComplete="off" value={hp} onChange={e => setHp(e.target.value)} />
                    <textarea
                        required
                        minLength={10}
                        maxLength={1000}
                        value={message}
                        onChange={e => setMessage(e.target.value)}
                        placeholder="Products, quantities, room, timeline or anything else we should know"
                        className="field textarea"
                    />
                    <TurnstileWidget onToken={setCaptchaToken} />
                    <button className="btn btn-orange">Request quote</button>
                </form>
            )}
        </main>
    );
}

export default QuoteRequest;

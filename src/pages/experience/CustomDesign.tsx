import { useState } from 'react';
import { useSeo } from '../../hooks/useSeo';
import { api, type Customer } from '../../api';
import { TurnstileWidget } from '../../components/security/TurnstileWidget';

export function CustomDesign({ user }: {
    user: Customer | null;
}) {
    useSeo('Custom Design | Wolfe — The Jewel of Villa', 'Submit bespoke hardware requirements and custom design briefs.');
    const [form, setForm] = useState({ projectName: '', requirements: '', referenceImageUrl: '' });
    const [hp, setHp] = useState('');
    const [sent, setSent] = useState(false);
    const [captchaToken, setCaptchaToken] = useState('');

    const submit = async (e: any) => {
        e.preventDefault();
        if (!user || hp) return;
        await api.customDesign.create(user.id, {...form, captchaToken});
        setSent(true);
    };

    return (
        <main className="container-w narrow-page">
            <p className="eyebrow">Custom design</p>
            <h1>Make it yours.</h1>
            {!user ? (
                <p>Please sign in to submit a custom design request.</p>
            ) : sent ? (
                <div className="success-box">
                    <h2>Design request received.</h2>
                    <p>Our team will review your brief and reference image.</p>
                </div>
            ) : (
                <form onSubmit={submit} className="checkout-form">
                    <input type="text" name="_hp" style={{ display: 'none' }} tabIndex={-1} autoComplete="off" value={hp} onChange={e => setHp(e.target.value)} />
                    <input
                        required
                        maxLength={160}
                        value={form.projectName}
                        onChange={e => setForm({ ...form, projectName: e.target.value })}
                        placeholder="Project name"
                        className="field"
                    />
                    <textarea
                        maxLength={2000}
                        value={form.requirements}
                        onChange={e => setForm({ ...form, requirements: e.target.value })}
                        placeholder="Dimensions, finish, quantity and design requirements"
                        className="field textarea"
                    />
                    <input
                        type="url"
                        maxLength={2000}
                        value={form.referenceImageUrl}
                        onChange={e => setForm({ ...form, referenceImageUrl: e.target.value })}
                        placeholder="Reference image URL (optional)"
                        className="field"
                    />
                    <TurnstileWidget onToken={setCaptchaToken} />
                    <button className="btn btn-orange">Submit design request</button>
                </form>
            )}
        </main>
    );
}

export default CustomDesign;

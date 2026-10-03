import { useState } from 'react';
import { useSeo } from '../../hooks/useSeo';
import { api } from '../../api';
import { TurnstileWidget } from '../../components/security/TurnstileWidget';

export function Consultation() {
    useSeo('Design Consultation | Wolfe — The Jewel of Villa', 'Book a complimentary architectural hardware design consultation with the Wolfe team.');
    const [sent, setSent] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [hp, setHp] = useState('');
    const [form, setForm] = useState({ name: '', email: '', project: '', message: '' });
    const [captchaToken, setCaptchaToken] = useState('');
    const submit = async (e: React.FormEvent) => {
        e.preventDefault(); setError(''); if (hp) return; setSaving(true);
        try { await api.consultations.create({...form, captchaToken}); setSent(true); }
        catch (err:any) { setError(err.message || 'Could not submit consultation request'); }
        finally { setSaving(false); }
    };
    return (
        <main className="consultation-page">
            <div className="consultation-image"></div>
            <div className="consultation-copy">
                <p className="eyebrow">Design service</p><h1>Find the right details for your space.</h1>
                <p>Not sure which pieces, finish or size will work? Book a complimentary consultation with the Wolfe team.</p>
                {sent ? <div className="success-box"><h2>Request received.</h2><p>We’ll follow up with guidance for your project.</p></div> : (
                    <form onSubmit={submit} className="consultation-form">
                        <input type="text" name="_hp" style={{display:'none'}} tabIndex={-1} autoComplete="off" value={hp} onChange={e=>setHp(e.target.value)} />
                        <input required maxLength={100} placeholder="Name" className="field" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} />
                        <input required maxLength={150} type="email" placeholder="Email" className="field" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} />
                        <input maxLength={160} placeholder="Project / room" className="field" value={form.project} onChange={e=>setForm({...form,project:e.target.value})} />
                        <textarea required minLength={10} maxLength={1000} placeholder="Tell us what you’re working on" className="field textarea" value={form.message} onChange={e=>setForm({...form,message:e.target.value})} />
                        <TurnstileWidget onToken={setCaptchaToken} />
                        {error && <p className="admin-note" role="alert">{error}</p>}
                        <button disabled={saving} className="btn btn-orange">{saving ? 'Sending…' : 'Request consultation'}</button>
                    </form>
                )}
            </div>
        </main>
    );
}
export default Consultation;

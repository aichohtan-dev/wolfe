import { useState } from 'react';
import { useSeo } from '../../hooks/useSeo';

export function Consultation() {
    useSeo('Design Consultation | Wolfe — The Jewel of Villa', 'Book a complimentary architectural hardware design consultation with the Wolfe team.');
    const [sent, setSent] = useState(false);
    const [hp, setHp] = useState('');

    return (
        <main className="consultation-page">
            <div className="consultation-image"></div>
            <div className="consultation-copy">
                <p className="eyebrow">Design service</p>
                <h1>Find the right details for your space.</h1>
                <p>Not sure which pieces, finish or size will work? Book a complimentary consultation with the Wolfe team.</p>
                {sent ? (
                    <div className="success-box">
                        <h2>Request received.</h2>
                        <p>We’ll follow up with guidance for your project.</p>
                    </div>
                ) : (
                    <form
                        onSubmit={e => {
                            e.preventDefault();
                            if (hp) return;
                            setSent(true);
                        }}
                        className="consultation-form"
                    >
                        <input type="text" name="_hp" style={{ display: 'none' }} tabIndex={-1} autoComplete="off" value={hp} onChange={e => setHp(e.target.value)} />
                        <input required maxLength={100} placeholder="Name" className="field" />
                        <input required maxLength={150} type="email" placeholder="Email" className="field" />
                        <input maxLength={160} placeholder="Project / room" className="field" />
                        <textarea required minLength={10} maxLength={1000} placeholder="Tell us what you're working on" className="field textarea" />
                        <button className="btn btn-orange">Request consultation</button>
                    </form>
                )}
            </div>
        </main>
    );
}

export default Consultation;

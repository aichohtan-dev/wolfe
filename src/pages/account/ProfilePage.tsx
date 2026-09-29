import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { api, type Customer } from '../../api';

export interface ProfilePageProps {
  user: Customer | null;
  onSaved: (u: Customer) => void;
}

export function ProfilePage({ user, onSaved }: ProfilePageProps) {
  const [name, setName] = useState(user?.name || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState('');

  if (!user) {
    return (
      <main className="container-w narrow-page">
        <p className="empty-state">Sign in to manage your profile.</p>
        <Link to="/account" className="btn btn-orange">Sign in</Link>
      </main>
    );
  }

  const save = async (e: any) => {
    e.preventDefault();
    setBusy(true);
    setMsg('');
    try {
      const u = await api.updateProfile({ name, phone });
      onSaved(u);
      setMsg('Profile saved.');
    } catch (err: any) {
      setMsg(err.message || 'Unable to save profile');
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="container-w narrow-page">
      <p className="eyebrow">Customer account</p>
      <h1>Your profile</h1>
      <form onSubmit={save} className="login-form">
        <input required value={name} onChange={e => setName(e.target.value)} placeholder="Full name" className="field" />
        <input value={user.email} disabled className="field" />
        <input value={phone} onChange={e => setPhone(e.target.value)} placeholder="Phone" className="field" />
        {msg && <p className="checkout-login-note">{msg}</p>}
        <button disabled={busy} className="btn btn-orange full">
          {busy ? 'Saving…' : 'Save profile'}
        </button>
      </form>
      <Link to="/addresses" className="text-link">
        Manage saved addresses <ArrowRight size={16} />
      </Link>
    </main>
  );
}

export default ProfilePage;

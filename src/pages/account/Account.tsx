import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Heart, Package, ShieldCheck, Truck, User } from 'lucide-react';
import { api, type Customer } from '../../api';
import { useSeo } from '../../hooks/useSeo';

export interface AccountProps {
  user: Customer | null;
  onLogin: (u: Customer) => void;
  onLogout: () => void;
}

export function Account({ user, onLogin, onLogout }: AccountProps) {
  useSeo('Customer Account | Wolfe — The Jewel of Villa', 'Manage your Wolfe account, orders, and addresses.');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [sessions, setSessions] = useState<any[]>([]);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [securityMessage, setSecurityMessage] = useState('');

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      if (mode === 'register') {
        const result = await api.register(name, email, password);
        setMode('login');
        setError(result?.message || 'If registration is available for the submitted details, the account is now available. Please sign in.');
      } else {
        const result = await api.login(email, password);
        onLogin(result.customer);
      }
    } catch (err: any) {
      setError(err.message || (mode === 'register' ? 'Could not create account' : 'Invalid credentials'));
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    if (!user) return;
    api.sessions().then(setSessions).catch(() => setSessions([]));
  }, [user]);

  const changePassword = async () => {
    setSecurityMessage('');
    try { await api.changePassword(currentPassword, newPassword); setCurrentPassword(''); setNewPassword(''); setSecurityMessage('Password changed. Other sessions were signed out.'); }
    catch (e: any) { setSecurityMessage(e.message || 'Could not change password.'); }
  };

  const revokeSession = async (id: number) => {
    await api.revokeSession(id);
    setSessions(v => v.filter(x => x.id !== id));
  };

  if (user) {
    return (
      <main className="container-w section">
        <p className="eyebrow">Customer account</p>
        <h1>Hello, {user.name}</h1>
        {user.emailVerified === false && <div className="account-card" role="status" style={{ marginBottom: 20 }}>
          <h2>Verify your email</h2><p>Verify your email before placing a cash-on-delivery order.</p>
          <button className="btn btn-light" onClick={async () => { await api.requestEmailVerification(user.email); }}>Resend verification email</button>
        </div>}
        <div className="account-grid">
          <Link to="/orders" className="account-card">
            <Package />
            <h2>Orders</h2>
            <p>Track your purchases.</p>
          </Link>
          <Link to="/profile" className="account-card">
            <User />
            <h2>Profile</h2>
            <p>Update your details.</p>
          </Link>
          <Link to="/addresses" className="account-card">
            <Truck />
            <h2>Addresses</h2>
            <p>Manage saved delivery addresses.</p>
          </Link>
          <Link to="/wishlist" className="account-card">
            <Heart />
            <h2>Wishlist</h2>
          </Link>
          <button onClick={onLogout} className="account-card">
            <ShieldCheck />
            <h2>Sign out</h2>
          </button>
        </div>
        <section className="account-card" style={{ marginTop: 24 }}>
          <h2>Security</h2>
          <p>Change your password and manage active sessions.</p>
          <div className="login-form">
            <input className="field" type="password" placeholder="Current password" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} />
            <input className="field" type="password" minLength={8} maxLength={72} placeholder="New password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
            <button className="btn btn-light" disabled={!currentPassword || newPassword.length < 8} onClick={changePassword}>Change password</button>
            {securityMessage && <p role="status">{securityMessage}</p>}
          </div>
          <h3>Active sessions</h3>
          {sessions.length === 0 ? <p>No active refresh sessions.</p> : sessions.map(s => (
            <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', gap: 12, marginBottom: 8 }}>
              <span>{s.device || 'Browser session'} · expires {new Date(s.expiresAt).toLocaleString()}</span>
              <button className="btn btn-light" onClick={() => revokeSession(s.id)}>Revoke</button>
            </div>
          ))}
        </section>
      </main>
    );
  }

  return (
    <main className="container-w narrow-page">
      <p className="eyebrow">Customer account</p>
      <h1>{mode === 'login' ? 'Sign in' : 'Create your account'}</h1>
      <p>{mode === 'login' ? 'Continue to manage orders and saved pieces.' : 'Create an account to save your bag, wishlist, addresses and orders.'}</p>
      <form onSubmit={submit} className="login-form">
        {mode === 'register' && (
          <input
            required
            maxLength={100}
            value={name}
            onChange={e => setName(e.target.value)}
            placeholder="Full name"
            className="field"
          />
        )}
        <input
          required
          maxLength={150}
          type="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          placeholder="Email address"
          className="field"
        />
        <input
          required
          minLength={8}
          maxLength={100}
          type="password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          placeholder="Password"
          className="field"
        />
        {error && <p className="error-message" role="alert">{error}</p>}
        <button disabled={busy} className="btn btn-orange full">
          {busy ? (mode === 'register' ? 'Creating account…' : 'Signing in…') : (mode === 'register' ? 'Create account' : 'Sign in')}
        </button>
      </form>
      <button
        type="button"
        className="btn btn-light full"
        style={{ marginTop: 10 }}
        onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}
      >
        {mode === 'login' ? 'Create a new account' : 'Already have an account? Sign in'}
      </button>
    </main>
  );
}

export default Account;

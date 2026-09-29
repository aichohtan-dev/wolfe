import React, { useState } from 'react';
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
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const login = async (e: any) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const result = await api.login(email, password);
      localStorage.setItem('wolfe_access_token', result.accessToken);
      localStorage.setItem('wolfe_refresh_token', result.refreshToken);
      onLogin(result.customer);
    } catch (err: any) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setBusy(false);
    }
  };

  if (user) {
    return (
      <main className="container-w section">
        <p className="eyebrow">Customer account</p>
        <h1>Hello, {user.name}</h1>
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
      </main>
    );
  }

  return (
    <main className="container-w narrow-page">
      <p className="eyebrow">Customer account</p>
      <h1>Sign in</h1>
      <p>Continue to manage orders and saved pieces.</p>
      <form onSubmit={login} className="login-form">
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
        {error && <p className="error-message">{error}</p>}
        <button disabled={busy} className="btn btn-orange full">
          {busy ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </main>
  );
}

export default Account;

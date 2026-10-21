import { useEffect, useState } from 'react';

const API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export default function App() {
  const [mode, setMode] = useState('register');
  const [email, setEmail] = useState('demo@example.com');
  const [password, setPassword] = useState('');
  const [token, setToken] = useState('');
  const [wallets, setWallets] = useState([]);
  const [history, setHistory] = useState([]);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [toWalletId, setToWalletId] = useState('');
  const [fromWalletId, setFromWalletId] = useState('');
  const [amount, setAmount] = useState('1.00');

  async function api(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...(options.headers || {}) }
    });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(body.detail || body.message || `Request failed (${response.status})`);
    return body;
  }

  async function authenticate(e) {
    e.preventDefault(); setBusy(true); setMessage('');
    try {
      const result = await api(`/api/auth/${mode}`, { method: 'POST', body: JSON.stringify({ email, password }) });
      setToken(result.token); setMessage(`Signed in as ${result.email}`);
    } catch (err) { setMessage(err.message); }
    finally { setBusy(false); }
  }

  async function refresh() {
    try {
      const ws = await api('/api/wallets');
      setWallets(ws);
      if (!fromWalletId && ws.length) setFromWalletId(ws[0].id);
      const tx = await api('/api/transfers');
      setHistory(tx);
    } catch (err) { setMessage(err.message); }
  }

  useEffect(() => { if (token) refresh(); }, [token]);

  async function createWallet() {
    try { await api('/api/wallets?currency=CAD', { method: 'POST' }); setMessage('Wallet created.'); await refresh(); }
    catch (err) { setMessage(err.message); }
  }

  async function demoCredit(walletId) {
    try { await api(`/api/wallets/${walletId}/demo-credit?amount=100.00`, { method: 'POST' }); setMessage('Added CAD 100.00 in simulated demo funds.'); await refresh(); }
    catch (err) { setMessage(err.message); }
  }

  async function transfer(e) {
    e.preventDefault();
    try {
      const key = crypto.randomUUID();
      await api('/api/transfers', { method: 'POST', headers: { 'Idempotency-Key': key }, body: JSON.stringify({
        fromWalletId, toWalletId, amount: Number(amount), currency: 'CAD', description: 'Portfolio demo transfer'
      })});
      setMessage('Transfer completed.'); await refresh();
    } catch (err) { setMessage(err.message); }
  }

  return <main className="shell">
    <header><div className="brand">SP<span>●</span></div><div><p className="eyebrow">PORTFOLIO PROJECT</p><h1>Secure Payments</h1><p className="sub">A simulated wallet platform built with Spring Boot</p></div></header>
    <div className="notice">Demo only — no real money or bank connections. Use fake accounts and test data.</div>
    {message && <div className="message" role="status">{message}</div>}
    {!token ? <section className="panel auth">
      <div className="tabs"><button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Create account</button><button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Sign in</button></div>
      <form onSubmit={authenticate}>
        <label>Email<input type="email" required value={email} onChange={e => setEmail(e.target.value)} /></label>
        <label>Password (minimum 10 characters)<input type="password" required minLength={mode === 'register' ? 10 : 1} value={password} onChange={e => setPassword(e.target.value)} /></label>
        <button className="primary" disabled={busy}>{busy ? 'Please wait…' : mode === 'register' ? 'Create account' : 'Sign in'}</button>
      </form>
    </section> : <>
      <section className="panel"><div className="section-head"><div><p className="eyebrow">YOUR ACCOUNTS</p><h2>Wallets</h2></div><button className="primary" onClick={createWallet}>+ Create CAD wallet</button></div>
        {wallets.length === 0 ? <p className="empty">No wallets yet. Create one to get started.</p> : <div className="wallet-grid">{wallets.map(w => <article className="wallet" key={w.id}><p>CAD WALLET</p><strong>{Number(w.balance).toLocaleString('en-CA', { style: 'currency', currency: w.currency })}</strong><small>{w.id}</small><button className="fund-button" onClick={() => demoCredit(w.id)}>Add CAD 100 demo funds</button></article>)}</div>}
      </section>
      <section className="panel"><p className="eyebrow">MOVE FUNDS</p><h2>New transfer</h2>
        <form onSubmit={transfer} className="transfer-form">
          <label>From wallet<select required value={fromWalletId} onChange={e => setFromWalletId(e.target.value)}>{wallets.map(w => <option key={w.id} value={w.id}>{w.id.slice(0, 8)} · {w.currency} {w.balance}</option>)}</select></label>
          <label>Destination wallet ID<input required value={toWalletId} onChange={e => setToWalletId(e.target.value)} placeholder="Paste another user's wallet UUID" /></label>
          <label>Amount (CAD)<input type="number" min="0.01" step="0.01" required value={amount} onChange={e => setAmount(e.target.value)} /></label>
          <button className="primary" disabled={!wallets.length}>Send demo transfer</button>
        </form>
      </section>
      <section className="panel"><div className="section-head"><div><p className="eyebrow">ACTIVITY</p><h2>Transfer history</h2></div><button onClick={refresh}>Refresh</button></div>
        {history.length === 0 ? <p className="empty">No transfers yet.</p> : <div className="table-wrap"><table><thead><tr><th>Date</th><th>Direction</th><th>Amount</th><th>Status</th></tr></thead><tbody>{history.map(t => <tr key={t.id}><td>{new Date(t.createdAt).toLocaleString()}</td><td>{wallets.some(w => w.id === t.fromWalletId) ? 'Sent' : 'Received'}</td><td>{Number(t.amount).toLocaleString('en-CA', { style: 'currency', currency: t.currency })}</td><td><span className="status">{t.status}</span></td></tr>)}</tbody></table></div>}
      </section>
      <button className="logout" onClick={() => { setToken(''); setWallets([]); setHistory([]); setMessage('Signed out.'); }}>Sign out</button>
    </>}
    <footer>Spring Boot · PostgreSQL · JWT · Kafka · React · Docker</footer>
  </main>;
}

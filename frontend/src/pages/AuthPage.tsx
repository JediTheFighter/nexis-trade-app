import { useState } from "react";
import type { FormEvent } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { ApiError, login, register } from "../api";
import { useAuth } from "../auth";

export function AuthPage({ mode }: { mode: "login" | "register" }) {
  const { session, signIn } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);
  const registering = mode === "register";

  if (session) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setPending(true);
    setError(null);
    try {
      const result = registering ? await register(email, password) : await login(email, password);
      signIn(result);
      navigate("/");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Could not reach the API.");
    } finally {
      setPending(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-copy">
        <p className="eyebrow">Nexis</p>
        <h1>A paper desk for learning the trade path.</h1>
        <p>
          Orders fill at the server&apos;s latest simulated price. Cash, positions, and profit stay in Postgres.
          Nothing here is a live brokerage.
        </p>
      </section>
      <form className="panel auth-form" onSubmit={onSubmit}>
        <h2>{registering ? "Open an account" : "Sign in"}</h2>
        <p className="hint">
          {registering
            ? "A new account starts with $100,000 of paper cash. Use at least 8 characters."
            : "Use the email and password you registered with."}
        </p>
        <label>
          Email
          <input
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            autoComplete={registering ? "new-password" : "current-password"}
            minLength={8}
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </label>
        {error ? <p className="form-error">{error}</p> : null}
        <button className="primary" type="submit" disabled={pending}>
          {pending ? "Working..." : registering ? "Create account" : "Enter the desk"}
        </button>
        {registering ? (
          <p className="switch">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        ) : (
          <p className="switch">
            New here? <Link to="/register">Open an account</Link>
          </p>
        )}
      </form>
    </main>
  );
}

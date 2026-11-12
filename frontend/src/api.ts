import type { AuthResult, Instrument, Order, Portfolio } from "./types";

const TOKEN_KEY = "nexis.token";
const EMAIL_KEY = "nexis.email";

export type Session = {
  token: string;
  email: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly code: string | undefined;

  constructor(message: string, status: number, code?: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

export function getSession(): Session | null {
  const token = sessionStorage.getItem(TOKEN_KEY);
  const email = sessionStorage.getItem(EMAIL_KEY);
  if (!token || !email) {
    return null;
  }
  return { token, email };
}

export function setSession(session: Session) {
  sessionStorage.setItem(TOKEN_KEY, session.token);
  sessionStorage.setItem(EMAIL_KEY, session.email);
}

export function clearSession() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(EMAIL_KEY);
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  const session = getSession();
  if (session) {
    headers.set("Authorization", `Bearer ${session.token}`);
  }

  const response = await fetch(path, { ...init, headers });
  if (response.status === 401 && !path.startsWith("/api/auth/")) {
    clearSession();
    window.dispatchEvent(new Event("nexis-unauthorized"));
  }
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as { message?: string; code?: string };
    throw new ApiError(body.message ?? "Request failed.", response.status, body.code);
  }
  return response.json() as Promise<T>;
}

export function login(email: string, password: string) {
  return api<AuthResult>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function register(email: string, password: string) {
  return api<AuthResult>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function fetchInstruments() {
  return api<Instrument[]>("/api/instruments");
}

export function fetchPortfolio() {
  return api<Portfolio>("/api/portfolio");
}

export function fetchOrders() {
  return api<Order[]>("/api/orders");
}

export function placeOrder(symbol: string, side: "BUY" | "SELL", quantity: number) {
  return api<Order>("/api/orders", {
    method: "POST",
    body: JSON.stringify({ symbol, side, quantity }),
  });
}

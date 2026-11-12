import { createContext, useContext, useEffect, useState } from "react";
import type { ReactNode } from "react";
import { clearSession, getSession, setSession } from "./api";
import type { Session } from "./api";

type AuthValue = {
  session: Session | null;
  signIn: (session: Session) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<Session | null>(() => getSession());

  useEffect(() => {
    const onUnauthorized = () => setSessionState(null);
    window.addEventListener("nexis-unauthorized", onUnauthorized);
    return () => window.removeEventListener("nexis-unauthorized", onUnauthorized);
  }, []);

  const value: AuthValue = {
    session,
    signIn: (next) => {
      setSession(next);
      setSessionState(next);
    },
    signOut: () => {
      clearSession();
      setSessionState(null);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return value;
}

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { aoSessaoExpirar, api, definirToken, type Sessao } from './api';

const CHAVE = 'carboncalc.sessao';

interface Auth {
  sessao: Sessao | null;
  /** Gestor ou administrador: registra consumo, calcula, compensa e envia relatórios. */
  podeGerir: boolean;
  ehAdmin: boolean;
  entrar: (email: string, senha: string) => Promise<void>;
  sair: () => void;
}

const AuthContext = createContext<Auth | null>(null);

function sessaoSalva(): Sessao | null {
  try {
    const bruto = localStorage.getItem(CHAVE);
    return bruto ? (JSON.parse(bruto) as Sessao) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(() => {
    const salva = sessaoSalva();
    definirToken(salva?.token ?? null);
    return salva;
  });

  const sair = useCallback(() => {
    localStorage.removeItem(CHAVE);
    definirToken(null);
    setSessao(null);
  }, []);

  const entrar = useCallback(async (email: string, senha: string) => {
    const nova = await api.login(email, senha);
    localStorage.setItem(CHAVE, JSON.stringify(nova));
    definirToken(nova.token);
    setSessao(nova);
  }, []);

  useEffect(() => aoSessaoExpirar(sair), [sair]);

  const valor = useMemo<Auth>(
    () => ({
      sessao,
      ehAdmin: sessao?.perfil === 'ADMINISTRADOR',
      podeGerir: sessao?.perfil === 'ADMINISTRADOR' || sessao?.perfil === 'GESTOR',
      entrar,
      sair,
    }),
    [sessao, entrar, sair],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): Auth {
  const auth = useContext(AuthContext);
  if (!auth) throw new Error('useAuth precisa estar dentro de AuthProvider');
  return auth;
}

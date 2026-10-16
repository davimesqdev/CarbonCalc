import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, type Empresa } from './api';

const CHAVE = 'carboncalc.empresa';

interface EmpresaAtual {
  empresas: Empresa[];
  empresa: Empresa | null;
  erro: string | null;
  selecionar: (id: number) => void;
  recarregar: () => Promise<void>;
}

const EmpresaContext = createContext<EmpresaAtual | null>(null);

/** Mantém a lista de empresas e qual delas está em uso nas demais telas. */
export function EmpresaProvider({ children }: { children: ReactNode }) {
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [idAtual, setIdAtual] = useState<number | null>(() => Number(localStorage.getItem(CHAVE)) || null);
  const [erro, setErro] = useState<string | null>(null);

  const recarregar = useCallback(async () => {
    try {
      setEmpresas(await api.empresas());
      setErro(null);
    } catch (e) {
      setErro((e as Error).message);
    }
  }, []);

  useEffect(() => {
    void recarregar();
  }, [recarregar]);

  const selecionar = useCallback((id: number) => {
    localStorage.setItem(CHAVE, String(id));
    setIdAtual(id);
  }, []);

  const valor = useMemo<EmpresaAtual>(() => {
    const empresa = empresas.find((e) => e.id === idAtual) ?? empresas[0] ?? null;
    return { empresas, empresa, erro, selecionar, recarregar };
  }, [empresas, idAtual, erro, selecionar, recarregar]);

  return <EmpresaContext.Provider value={valor}>{children}</EmpresaContext.Provider>;
}

export function useEmpresa(): EmpresaAtual {
  const contexto = useContext(EmpresaContext);
  if (!contexto) throw new Error('useEmpresa precisa estar dentro de EmpresaProvider');
  return contexto;
}

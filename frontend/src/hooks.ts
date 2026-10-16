import { useCallback, useEffect, useState } from 'react';

/**
 * Carrega dados da API e os recarrega quando `chave` muda. Com `chave` nula nada é buscado
 * (por exemplo, enquanto nenhuma empresa está selecionada).
 */
export function useDados<T>(buscar: () => Promise<T>, chave: string | number | null) {
  const [dados, setDados] = useState<T | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [versao, setVersao] = useState(0);

  useEffect(() => {
    if (chave === null) {
      setDados(null);
      return;
    }
    let ativo = true;
    buscar()
      .then((resultado) => {
        if (ativo) {
          setDados(resultado);
          setErro(null);
        }
      })
      .catch((e: Error) => {
        if (ativo) setErro(e.message);
      });
    return () => {
      ativo = false;
    };
    // `buscar` é recriada a cada render; a chave é o que identifica a consulta.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [chave, versao]);

  const recarregar = useCallback(() => setVersao((v) => v + 1), []);
  return { dados, erro, recarregar };
}

/** Executa uma ação da API controlando os estados de "enviando" e de erro. */
export function useAcao() {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const executar = useCallback(async (acao: () => Promise<void>) => {
    setEnviando(true);
    setErro(null);
    try {
      await acao();
    } catch (e) {
      setErro((e as Error).message);
    } finally {
      setEnviando(false);
    }
  }, []);

  return { enviando, erro, executar };
}

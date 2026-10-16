import type { ReactNode } from 'react';
import type { Calculo } from './api';
import { toneladas } from './formato';

export function Aviso({ tipo = 'erro', children }: { tipo?: 'erro' | 'ok' | 'info'; children: ReactNode }) {
  if (!children) return null;
  return (
    <p className={`aviso aviso-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {children}
    </p>
  );
}

export function Pagina({ titulo, descricao, children }: { titulo: string; descricao?: string; children: ReactNode }) {
  return (
    <>
      <header className="pagina-cabecalho">
        <h1>{titulo}</h1>
        {descricao && <p>{descricao}</p>}
      </header>
      {children}
    </>
  );
}

export function Cartao({ titulo, children }: { titulo?: string; children: ReactNode }) {
  return (
    <section className="cartao">
      {titulo && <h2>{titulo}</h2>}
      {children}
    </section>
  );
}

export function Campo({ rotulo, children }: { rotulo: string; children: ReactNode }) {
  return (
    <label className="campo">
      <span>{rotulo}</span>
      {children}
    </label>
  );
}

export function Vazio({ children }: { children: ReactNode }) {
  return <p className="vazio">{children}</p>;
}

export function SemEmpresa() {
  return <Vazio>Nenhuma empresa cadastrada ainda. Cadastre uma na tela Empresas para começar.</Vazio>;
}

const ESCOPOS = [
  { chave: 'escopo1', rotulo: 'Escopo 1', detalhe: 'emissões diretas' },
  { chave: 'escopo2', rotulo: 'Escopo 2', detalhe: 'energia elétrica' },
  { chave: 'escopo3', rotulo: 'Escopo 3', detalhe: 'cadeia de valor' },
] as const;

/** Participação de cada escopo no total: barra empilhada com legenda e valores. */
export function EmissoesPorEscopo({ calculo }: { calculo: Calculo }) {
  const total = calculo.totalCO2e;
  return (
    <figure className="escopos">
      <div className="escopos-barra" role="img" aria-label="Participação de cada escopo nas emissões">
        {ESCOPOS.map(({ chave, rotulo }) => {
          const valor = calculo[chave];
          if (valor <= 0) return null;
          return (
            <span
              key={chave}
              className={`escopos-segmento ${chave}`}
              style={{ flexGrow: valor }}
              title={`${rotulo}: ${toneladas(valor)} t CO₂e`}
            />
          );
        })}
      </div>
      <figcaption>
        {ESCOPOS.map(({ chave, rotulo, detalhe }) => {
          const valor = calculo[chave];
          const parte = total > 0 ? Math.round((valor / total) * 100) : 0;
          return (
            <div key={chave} className="escopos-item">
              <span className={`escopos-marca ${chave}`} aria-hidden="true" />
              <span>
                <strong>{rotulo}</strong> <small>{detalhe}</small>
              </span>
              <span className="num">
                {toneladas(valor)} t <small>({parte}%)</small>
              </span>
            </div>
          );
        })}
      </figcaption>
    </figure>
  );
}

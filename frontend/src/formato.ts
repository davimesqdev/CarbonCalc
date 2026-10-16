const numero3 = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
const numeroLivre = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 3 });
const reais = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

/** Toneladas de CO2e, sempre com três casas. */
export const toneladas = (valor: number) => numero3.format(valor);
export const quantidade = (valor: number) => numeroLivre.format(valor);
export const moeda = (valor: number) => reais.format(valor);

/** "2026-03-01" -> "01/03/2026" */
export function data(iso: string): string {
  const [ano, mes, dia] = iso.slice(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}

export function dataHora(iso: string): string {
  return `${data(iso)} ${iso.slice(11, 16)}`;
}

/** "2026-03" -> "03/2026" */
export function mes(competencia: string): string {
  const [ano, m] = competencia.split('-');
  return `${m}/${ano}`;
}

export function cnpj(digitos: string): string {
  return digitos.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5');
}

export function periodo(inicio: string, fim: string): string {
  return `${mes(inicio.slice(0, 7))} a ${mes(fim.slice(0, 7))}`;
}

export const ROTULO_ESCOPO = { ESCOPO_1: 'Escopo 1', ESCOPO_2: 'Escopo 2', ESCOPO_3: 'Escopo 3' } as const;

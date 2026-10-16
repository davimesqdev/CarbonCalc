export type Perfil = 'ADMINISTRADOR' | 'GESTOR' | 'AUDITOR';
export type Escopo = 'ESCOPO_1' | 'ESCOPO_2' | 'ESCOPO_3';
export type StatusPlanta = 'CADASTRADA' | 'ATIVA' | 'EM_MANUTENCAO' | 'INATIVA' | 'ARQUIVADA';
export type AcaoPlanta =
  | 'ATIVAR'
  | 'CANCELAR_CADASTRO'
  | 'ENTRAR_MANUTENCAO'
  | 'SAIR_MANUTENCAO'
  | 'INATIVAR'
  | 'REATIVAR';
export type TipoRelatorio = 'INVENTARIO_EMISSOES' | 'PLANO_COMPENSACAO';
export type Formato = 'PDF' | 'XLSX';

export interface Sessao {
  token: string;
  nome: string;
  email: string;
  perfil: Perfil;
}
export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
}
export interface Empresa {
  id: number;
  nome: string;
  cnpj: string;
  setor: string;
}
export interface Planta {
  id: number;
  empresaId: number;
  nome: string;
  localizacao: string;
  tipo: 'NOVA' | 'EXISTENTE';
  status: StatusPlanta;
  subestado: 'OPERANDO' | 'OCIOSA' | null;
  acoesDisponiveis: AcaoPlanta[];
}
export interface Fonte {
  id: number;
  plantaId: number;
  tipo: string;
  rotulo: string;
  escopo: Escopo;
  unidade: string;
  descricao: string;
}
export interface Fator {
  tipo: string;
  rotulo: string;
  escopo: Escopo;
  unidade: string;
  valor: number;
  vigenciaInicio: string;
}
export interface Consumo {
  id: number;
  fonteId: number;
  fonte: string;
  tipo: string;
  unidade: string;
  planta: string;
  competencia: string;
  quantidade: number;
}
export interface Calculo {
  id: number;
  empresaId: number;
  cenario: string;
  periodoInicio: string;
  periodoFim: string;
  escopo1: number;
  escopo2: number;
  escopo3: number;
  totalCO2e: number;
  origemFatores: 'EXTERNO' | 'CACHE_LOCAL';
  dataCalculo: string;
}
export interface Diferenca {
  item: string;
  base: number;
  alternativo: number;
  diferenca: number;
  percentual: number | null;
}
export interface Comparacao {
  base: Calculo;
  alternativo: Calculo;
  diferencas: Diferenca[];
}
export interface Compensacao {
  id: number;
  tipoAcao: string;
  rotulo: string;
  descricao: string;
  creditosNecessarios: number;
  quantidade: number;
  unidade: string;
  custoEstimado: number;
}
export interface PlanoCompensacao {
  calculoId: number;
  totalCO2e: number;
  creditosNecessarios: number;
  custoTotal: number;
  acoes: Compensacao[];
}
export interface Relatorio {
  id: number;
  empresaId: number;
  calculoId: number;
  cenario: string;
  tipo: TipoRelatorio;
  rotulo: string;
  formato: Formato;
  geradoEm: string;
  protocoloEnvio: string | null;
  enviadoEm: string | null;
}

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

let token: string | null = null;
let aoExpirar: () => void = () => {};

export function definirToken(novo: string | null) {
  token = novo;
}

/** Registra o que fazer quando a API recusa o token (sessão expirada). */
export function aoSessaoExpirar(acao: () => void) {
  aoExpirar = acao;
}

async function chamar(metodo: string, caminho: string, corpo?: unknown): Promise<Response> {
  const headers: Record<string, string> = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  if (corpo !== undefined) headers['Content-Type'] = 'application/json';
  let resposta: Response;
  try {
    resposta = await fetch(`/api${caminho}`, {
      method: metodo,
      headers,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    });
  } catch {
    throw new ApiError(0, 'Não foi possível falar com o servidor. Verifique se o backend está no ar.');
  }
  if (resposta.ok) return resposta;
  if (resposta.status === 401 && caminho !== '/auth/login') {
    aoExpirar();
    throw new ApiError(401, 'Sua sessão expirou. Entre novamente.');
  }
  const dados = await resposta.json().catch(() => null);
  throw new ApiError(resposta.status, dados?.mensagem ?? mensagemPadrao(resposta.status));
}

function mensagemPadrao(status: number): string {
  if (status === 403) return 'Seu perfil não tem permissão para esta ação.';
  if (status >= 500) return 'O servidor não conseguiu concluir a operação. Tente novamente.';
  return `A operação falhou (HTTP ${status}).`;
}

async function json<T>(metodo: string, caminho: string, corpo?: unknown): Promise<T> {
  return (await chamar(metodo, caminho, corpo)).json();
}

export const api = {
  login: (email: string, senha: string) => json<Sessao>('POST', '/auth/login', { email, senha }),

  usuarios: () => json<Usuario[]>('GET', '/usuarios'),
  criarUsuario: (dados: { nome: string; email: string; senha: string; perfil: Perfil }) =>
    json<Usuario>('POST', '/usuarios', dados),
  alterarAtivo: (id: number, ativo: boolean) => json<Usuario>('PATCH', `/usuarios/${id}/ativo`, { ativo }),

  empresas: () => json<Empresa[]>('GET', '/empresas'),
  criarEmpresa: (dados: { nome: string; cnpj: string; setor: string }) =>
    json<Empresa>('POST', '/empresas', dados),

  plantas: (empresaId: number) => json<Planta[]>('GET', `/empresas/${empresaId}/plantas`),
  criarPlanta: (empresaId: number, dados: { nome: string; localizacao: string; tipo: string }) =>
    json<Planta>('POST', `/empresas/${empresaId}/plantas`, dados),
  transicionar: (plantaId: number, acao: AcaoPlanta, documentacaoOk = false) =>
    json<Planta>('POST', `/plantas/${plantaId}/transicao`, { acao, documentacaoOk }),

  fontes: (plantaId: number) => json<Fonte[]>('GET', `/plantas/${plantaId}/fontes`),
  criarFonte: (plantaId: number, dados: { tipo: string; descricao: string }) =>
    json<Fonte>('POST', `/plantas/${plantaId}/fontes`, dados),
  fatores: () => json<Fator[]>('GET', '/fatores'),

  consumos: (empresaId: number) => json<Consumo[]>('GET', `/empresas/${empresaId}/consumos`),
  registrarConsumo: (dados: { fonteId: number; competencia: string; quantidade: number }) =>
    json<Consumo>('POST', '/consumos', dados),

  calculos: (empresaId: number) => json<Calculo[]>('GET', `/calculos?empresaId=${empresaId}`),
  calcular: (dados: { empresaId: number; inicio: string; fim: string; cenario: string }) =>
    json<Calculo>('POST', '/calculos', dados),
  comparar: (base: number, alternativo: number) =>
    json<Comparacao>('GET', `/calculos/comparar?base=${base}&alternativo=${alternativo}`),

  plano: (calculoId: number) => json<PlanoCompensacao>('GET', `/calculos/${calculoId}/compensacoes`),
  gerarPlano: (calculoId: number, percentual: number) =>
    json<PlanoCompensacao>('POST', `/calculos/${calculoId}/compensacoes`, { percentual }),

  relatorios: (empresaId: number) => json<Relatorio[]>('GET', `/relatorios?empresaId=${empresaId}`),
  gerarRelatorio: (dados: { calculoId: number; tipo: TipoRelatorio; formato: Formato }) =>
    json<Relatorio>('POST', '/relatorios', dados),
  enviarRelatorio: (id: number) => json<Relatorio>('POST', `/relatorios/${id}/enviar`),

  /** Baixa o arquivo do relatório e dispara o "salvar como" do navegador. */
  async baixarRelatorio(relatorio: Relatorio) {
    const resposta = await chamar('GET', `/relatorios/${relatorio.id}/download`);
    const nome =
      /filename="([^"]+)"/.exec(resposta.headers.get('Content-Disposition') ?? '')?.[1] ??
      `relatorio-${relatorio.id}.${relatorio.formato.toLowerCase()}`;
    const url = URL.createObjectURL(await resposta.blob());
    const link = document.createElement('a');
    link.href = url;
    link.download = nome;
    link.click();
    URL.revokeObjectURL(url);
  },
};

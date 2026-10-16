import { useState, type FormEvent } from 'react';
import { api, type AcaoPlanta, type Planta, type StatusPlanta } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, Pagina, SemEmpresa, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { ROTULO_ESCOPO } from '../formato';
import { useAcao, useDados } from '../hooks';

const ROTULO_STATUS: Record<StatusPlanta, string> = {
  CADASTRADA: 'Cadastrada',
  ATIVA: 'Ativa',
  EM_MANUTENCAO: 'Em manutenção',
  INATIVA: 'Inativa',
  ARQUIVADA: 'Arquivada',
};

const ROTULO_ACAO: Record<AcaoPlanta, string> = {
  ATIVAR: 'Ativar',
  CANCELAR_CADASTRO: 'Cancelar cadastro',
  ENTRAR_MANUTENCAO: 'Iniciar manutenção',
  SAIR_MANUTENCAO: 'Concluir manutenção',
  INATIVAR: 'Inativar',
  REATIVAR: 'Reativar',
};

export default function Plantas() {
  const { ehAdmin } = useAuth();
  const { empresa } = useEmpresa();
  const id = empresa?.id ?? null;
  const plantas = useDados(() => api.plantas(id!), id);
  const acao = useAcao();
  const [nome, setNome] = useState('');
  const [localizacao, setLocalizacao] = useState('');
  const [tipo, setTipo] = useState('EXISTENTE');

  if (!empresa) {
    return (
      <Pagina titulo="Plantas e fontes">
        <SemEmpresa />
      </Pagina>
    );
  }

  function cadastrar(evento: FormEvent) {
    evento.preventDefault();
    void acao.executar(async () => {
      await api.criarPlanta(empresa!.id, { nome, localizacao, tipo });
      setNome('');
      setLocalizacao('');
      plantas.recarregar();
    });
  }

  return (
    <Pagina
      titulo="Plantas e fontes"
      descricao="Unidades da empresa e as fontes de emissão de cada uma. Só plantas ativas recebem consumo."
    >
      {ehAdmin && (
        <Cartao titulo="Cadastrar planta">
          <form className="formulario" onSubmit={cadastrar}>
            <Campo rotulo="Nome">
              <input value={nome} onChange={(e) => setNome(e.target.value)} required />
            </Campo>
            <Campo rotulo="Localização">
              <input value={localizacao} onChange={(e) => setLocalizacao(e.target.value)} required placeholder="Cidade/UF" />
            </Campo>
            <Campo rotulo="Tipo">
              <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
                <option value="EXISTENTE">Existente (informa histórico)</option>
                <option value="NOVA">Nova (projeta emissões futuras)</option>
              </select>
            </Campo>
            <button type="submit" disabled={acao.enviando}>
              Cadastrar
            </button>
          </form>
          <Aviso>{acao.erro}</Aviso>
        </Cartao>
      )}

      <Aviso>{plantas.erro}</Aviso>
      {plantas.dados?.length === 0 && <Vazio>Nenhuma planta cadastrada para {empresa.nome}.</Vazio>}
      {plantas.dados?.map((planta) => (
        <CartaoPlanta key={planta.id} planta={planta} podeEditar={ehAdmin} aoMudar={plantas.recarregar} />
      ))}
    </Pagina>
  );
}

function CartaoPlanta({ planta, podeEditar, aoMudar }: { planta: Planta; podeEditar: boolean; aoMudar: () => void }) {
  const fontes = useDados(() => api.fontes(planta.id), planta.id);
  const fatores = useDados(() => api.fatores(), 'fatores');
  const acao = useAcao();
  const [documentacaoOk, setDocumentacaoOk] = useState(false);
  const [tipoFonte, setTipoFonte] = useState('');
  const [descricao, setDescricao] = useState('');

  const precisaDocumentacao = planta.acoesDisponiveis.includes('ATIVAR');

  function transicionar(qual: AcaoPlanta) {
    void acao.executar(async () => {
      await api.transicionar(planta.id, qual, documentacaoOk);
      aoMudar();
    });
  }

  function adicionarFonte(evento: FormEvent) {
    evento.preventDefault();
    void acao.executar(async () => {
      await api.criarFonte(planta.id, { tipo: tipoFonte, descricao });
      setDescricao('');
      fontes.recarregar();
    });
  }

  return (
    <section className="cartao">
      <div className="planta-topo">
        <div>
          <h2>{planta.nome}</h2>
          <p className="legenda">
            {planta.localizacao} · {planta.tipo === 'NOVA' ? 'planta nova' : 'planta existente'}
          </p>
        </div>
        <span className={`etiqueta status-${planta.status.toLowerCase()}`}>
          {ROTULO_STATUS[planta.status]}
          {planta.subestado && ` · ${planta.subestado === 'OPERANDO' ? 'operando' : 'ociosa'}`}
        </span>
      </div>

      {podeEditar && planta.acoesDisponiveis.length > 0 && (
        <div className="planta-acoes">
          {precisaDocumentacao && (
            <label className="marcar">
              <input type="checkbox" checked={documentacaoOk} onChange={(e) => setDocumentacaoOk(e.target.checked)} />
              Documentação conferida
            </label>
          )}
          {planta.acoesDisponiveis.map((qual) => (
            <button
              key={qual}
              type="button"
              className="botao-secundario"
              disabled={acao.enviando || (qual === 'ATIVAR' && !documentacaoOk)}
              onClick={() => transicionar(qual)}
            >
              {ROTULO_ACAO[qual]}
            </button>
          ))}
        </div>
      )}
      <Aviso>{acao.erro ?? fontes.erro}</Aviso>

      <h3>Fontes de emissão</h3>
      {fontes.dados?.length === 0 ? (
        <Vazio>Nenhuma fonte registrada nesta planta.</Vazio>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Descrição</th>
              <th>Tipo</th>
              <th>Escopo</th>
              <th>Unidade</th>
            </tr>
          </thead>
          <tbody>
            {fontes.dados?.map((fonte) => (
              <tr key={fonte.id}>
                <td>{fonte.descricao}</td>
                <td>{fonte.rotulo}</td>
                <td>{ROTULO_ESCOPO[fonte.escopo]}</td>
                <td>{fonte.unidade}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {podeEditar && planta.status !== 'ARQUIVADA' && (
        <form className="formulario" onSubmit={adicionarFonte}>
          <Campo rotulo="Tipo de fonte">
            <select value={tipoFonte} onChange={(e) => setTipoFonte(e.target.value)} required>
              <option value="">Selecione…</option>
              {fatores.dados?.map((f) => (
                <option key={f.tipo} value={f.tipo}>
                  {f.rotulo} ({ROTULO_ESCOPO[f.escopo]}, em {f.unidade})
                </option>
              ))}
            </select>
          </Campo>
          <Campo rotulo="Descrição">
            <input value={descricao} onChange={(e) => setDescricao(e.target.value)} required placeholder="Ex.: Geradores a diesel" />
          </Campo>
          <button type="submit" disabled={acao.enviando}>
            Adicionar fonte
          </button>
        </form>
      )}
    </section>
  );
}

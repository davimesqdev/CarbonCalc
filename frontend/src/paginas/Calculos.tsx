import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api, type Calculo, type Comparacao } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, EmissoesPorEscopo, Pagina, SemEmpresa, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { dataHora, periodo, toneladas } from '../formato';
import { useAcao, useDados } from '../hooks';

export default function Calculos() {
  const { podeGerir } = useAuth();
  const { empresa } = useEmpresa();
  const id = empresa?.id ?? null;
  const historico = useDados(() => api.calculos(id!), id);
  const calculo = useAcao();
  const comparacao = useAcao();

  const ano = new Date().getFullYear();
  const [inicio, setInicio] = useState(`${ano}-01`);
  const [fim, setFim] = useState(`${ano}-12`);
  const [cenario, setCenario] = useState('');
  const [resultado, setResultado] = useState<Calculo | null>(null);
  const [marcados, setMarcados] = useState<number[]>([]);
  const [comparado, setComparado] = useState<Comparacao | null>(null);

  if (!empresa) {
    return (
      <Pagina titulo="Cálculos">
        <SemEmpresa />
      </Pagina>
    );
  }

  function calcular(evento: FormEvent) {
    evento.preventDefault();
    setResultado(null);
    void calculo.executar(async () => {
      setResultado(await api.calcular({ empresaId: empresa!.id, inicio, fim, cenario }));
      historico.recarregar();
    });
  }

  // Mantém no máximo dois cálculos marcados: o primeiro é a base, o segundo a alternativa.
  function marcar(calculoId: number) {
    setComparado(null);
    setMarcados((atuais) =>
      atuais.includes(calculoId) ? atuais.filter((m) => m !== calculoId) : [...atuais, calculoId].slice(-2),
    );
  }

  function comparar() {
    void comparacao.executar(async () => setComparado(await api.comparar(marcados[0], marcados[1])));
  }

  const semDados = calculo.erro?.startsWith('Dados insuficientes');

  return (
    <Pagina
      titulo="Cálculos"
      descricao="Pegada de carbono da empresa no período, em toneladas de CO₂ equivalente, separada por escopo."
    >
      {podeGerir && (
        <Cartao titulo="Calcular pegada de carbono">
          <form className="formulario" onSubmit={calcular}>
            <Campo rotulo="De">
              <input type="month" value={inicio} onChange={(e) => setInicio(e.target.value)} required />
            </Campo>
            <Campo rotulo="Até">
              <input type="month" value={fim} onChange={(e) => setFim(e.target.value)} required />
            </Campo>
            <Campo rotulo="Nome do cenário (opcional)">
              <input value={cenario} onChange={(e) => setCenario(e.target.value)} maxLength={120} placeholder="Cenário base" />
            </Campo>
            <button type="submit" disabled={calculo.enviando}>
              {calculo.enviando ? 'Calculando…' : 'Calcular'}
            </button>
          </form>
          {calculo.erro && (
            <Aviso>
              {calculo.erro} {semDados && <Link to="/consumos">Registrar consumo</Link>}
            </Aviso>
          )}
          {resultado && (
            <div className="resultado">
              <p className="resultado-total">
                <strong>{toneladas(resultado.totalCO2e)}</strong> t CO₂e
              </p>
              <p className="legenda">
                {resultado.cenario} · {periodo(resultado.periodoInicio, resultado.periodoFim)} · fatores{' '}
                {resultado.origemFatores === 'EXTERNO' ? 'da fonte externa' : 'do cache local (fonte externa indisponível)'}
              </p>
              <EmissoesPorEscopo calculo={resultado} />
              <p>
                <Link to={`/compensacao?calculo=${resultado.id}`}>Calcular a compensação deste resultado</Link>
              </p>
            </div>
          )}
        </Cartao>
      )}

      <Cartao titulo="Histórico">
        <Aviso>{historico.erro}</Aviso>
        {historico.dados?.length === 0 ? (
          <Vazio>Nenhum cálculo realizado para {empresa.nome}.</Vazio>
        ) : (
          <>
            <p className="legenda">Marque dois cálculos para comparar cenários.</p>
            <table>
              <thead>
                <tr>
                  <th />
                  <th>Cenário</th>
                  <th>Período</th>
                  <th className="num">Escopo 1</th>
                  <th className="num">Escopo 2</th>
                  <th className="num">Escopo 3</th>
                  <th className="num">Total (t CO₂e)</th>
                  <th>Calculado em</th>
                </tr>
              </thead>
              <tbody>
                {historico.dados?.map((c) => (
                  <tr key={c.id}>
                    <td>
                      <input
                        type="checkbox"
                        checked={marcados.includes(c.id)}
                        onChange={() => marcar(c.id)}
                        aria-label={`Comparar ${c.cenario}`}
                      />
                    </td>
                    <td>{c.cenario}</td>
                    <td>{periodo(c.periodoInicio, c.periodoFim)}</td>
                    <td className="num">{toneladas(c.escopo1)}</td>
                    <td className="num">{toneladas(c.escopo2)}</td>
                    <td className="num">{toneladas(c.escopo3)}</td>
                    <td className="num">
                      <strong>{toneladas(c.totalCO2e)}</strong>
                    </td>
                    <td>{dataHora(c.dataCalculo)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <button type="button" disabled={marcados.length !== 2 || comparacao.enviando} onClick={comparar}>
              Comparar cenários
            </button>
            <Aviso>{comparacao.erro}</Aviso>
          </>
        )}
      </Cartao>

      {comparado && (
        <Cartao titulo="Comparação de cenários">
          <table>
            <thead>
              <tr>
                <th />
                <th className="num">{comparado.base.cenario} (base)</th>
                <th className="num">{comparado.alternativo.cenario}</th>
                <th className="num">Diferença (t CO₂e)</th>
                <th className="num">Variação</th>
              </tr>
            </thead>
            <tbody>
              {comparado.diferencas.map((d) => (
                <tr key={d.item} className={d.item === 'Total' ? 'linha-total' : undefined}>
                  <td>{d.item}</td>
                  <td className="num">{toneladas(d.base)}</td>
                  <td className="num">{toneladas(d.alternativo)}</td>
                  <td className="num">
                    {d.diferenca > 0 ? '+' : ''}
                    {toneladas(d.diferenca)}
                  </td>
                  <td className="num">
                    {d.percentual === null ? '—' : `${d.percentual > 0 ? '+' : ''}${d.percentual.toLocaleString('pt-BR')}%`}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Cartao>
      )}
    </Pagina>
  );
}

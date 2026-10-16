import { useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, type PlanoCompensacao } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, Pagina, SemEmpresa, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { moeda, periodo, quantidade, toneladas } from '../formato';
import { useAcao, useDados } from '../hooks';

export default function Compensacao() {
  const { podeGerir } = useAuth();
  const { empresa } = useEmpresa();
  const id = empresa?.id ?? null;
  const [parametros, setParametros] = useSearchParams();
  const calculos = useDados(() => api.calculos(id!), id);
  const acao = useAcao();
  const [percentual, setPercentual] = useState('100');
  const [gerado, setGerado] = useState<PlanoCompensacao | null>(null);

  const pedido = Number(parametros.get('calculo'));
  const calculo = calculos.dados?.find((c) => c.id === pedido) ?? calculos.dados?.[0] ?? null;
  const salvo = useDados(() => api.plano(calculo!.id), calculo?.id ?? null);
  const plano = gerado?.calculoId === calculo?.id ? gerado : salvo.dados;

  if (!empresa) {
    return (
      <Pagina titulo="Compensação">
        <SemEmpresa />
      </Pagina>
    );
  }

  function gerar(evento: FormEvent) {
    evento.preventDefault();
    void acao.executar(async () => setGerado(await api.gerarPlano(calculo!.id, Number(percentual))));
  }

  return (
    <Pagina
      titulo="Compensação"
      descricao="Quanto das emissões precisa ser reposto e de que forma. Um crédito de carbono equivale a 1 t de CO₂e."
    >
      <Aviso>{calculos.erro}</Aviso>
      {calculos.dados?.length === 0 ? (
        <Vazio>Faça um cálculo de pegada antes de planejar a compensação.</Vazio>
      ) : (
        calculo && (
          <>
            <Cartao titulo="Plano de compensação">
              <form className="formulario" onSubmit={gerar}>
                <Campo rotulo="Cálculo">
                  <select value={calculo.id} onChange={(e) => setParametros({ calculo: e.target.value })}>
                    {calculos.dados?.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.cenario} · {periodo(c.periodoInicio, c.periodoFim)} · {toneladas(c.totalCO2e)} t
                      </option>
                    ))}
                  </select>
                </Campo>
                {podeGerir && (
                  <>
                    <Campo rotulo="Compensar (% das emissões)">
                      <input type="number" min="1" max="100" value={percentual} onChange={(e) => setPercentual(e.target.value)} required />
                    </Campo>
                    <button type="submit" disabled={acao.enviando}>
                      {plano && plano.acoes.length > 0 ? 'Refazer plano' : 'Gerar plano'}
                    </button>
                  </>
                )}
              </form>
              <Aviso>{acao.erro ?? salvo.erro}</Aviso>
            </Cartao>

            {plano && plano.acoes.length === 0 && <Vazio>Este cálculo ainda não tem plano de compensação.</Vazio>}
            {plano && plano.acoes.length > 0 && (
              <Cartao titulo="Ações sugeridas">
                <div className="indicadores">
                  <div className="indicador">
                    <span>Emissões do cálculo</span>
                    <strong>{toneladas(plano.totalCO2e)} t</strong>
                    <small>CO₂e</small>
                  </div>
                  <div className="indicador destaque">
                    <span>A compensar</span>
                    <strong>{toneladas(plano.creditosNecessarios)} t</strong>
                    <small>{Math.round((plano.creditosNecessarios / plano.totalCO2e) * 100)}% das emissões</small>
                  </div>
                  <div className="indicador">
                    <span>Custo estimado</span>
                    <strong>{moeda(plano.custoTotal)}</strong>
                    <small>soma das ações</small>
                  </div>
                </div>
                <table>
                  <thead>
                    <tr>
                      <th>Ação</th>
                      <th className="num">Compensa (t CO₂e)</th>
                      <th className="num">Quantidade</th>
                      <th className="num">Custo estimado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {plano.acoes.map((a) => (
                      <tr key={a.id}>
                        <td>
                          <strong>{a.rotulo}</strong>
                          <br />
                          <small>{a.descricao}</small>
                        </td>
                        <td className="num">{toneladas(a.creditosNecessarios)}</td>
                        <td className="num">
                          {quantidade(a.quantidade)} {a.unidade}
                        </td>
                        <td className="num">{moeda(a.custoEstimado)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <p className="legenda">
                  A energia elétrica (Escopo 2) é coberta por certificados de energia renovável; o restante é dividido
                  entre créditos de carbono (60%) e reflorestamento (40%). Preços e rendimentos são premissas
                  ilustrativas, não cotações de mercado.
                </p>
              </Cartao>
            )}
          </>
        )
      )}
    </Pagina>
  );
}

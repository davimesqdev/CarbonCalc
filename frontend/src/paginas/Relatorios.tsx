import { useState, type FormEvent } from 'react';
import { api, type Formato, type Relatorio, type TipoRelatorio } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, Pagina, SemEmpresa, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { dataHora, periodo, toneladas } from '../formato';
import { useAcao, useDados } from '../hooks';

export default function Relatorios() {
  const { podeGerir } = useAuth();
  const { empresa } = useEmpresa();
  const id = empresa?.id ?? null;
  const calculos = useDados(() => api.calculos(id!), id);
  const relatorios = useDados(() => api.relatorios(id!), id);
  const geracao = useAcao();
  const lista = useAcao();
  const [calculoId, setCalculoId] = useState('');
  const [tipo, setTipo] = useState<TipoRelatorio>('INVENTARIO_EMISSOES');
  const [formato, setFormato] = useState<Formato>('PDF');

  if (!empresa) {
    return (
      <Pagina titulo="Relatórios">
        <SemEmpresa />
      </Pagina>
    );
  }

  const calculoEscolhido = calculoId || String(calculos.dados?.[0]?.id ?? '');

  function gerar(evento: FormEvent) {
    evento.preventDefault();
    void geracao.executar(async () => {
      await api.gerarRelatorio({ calculoId: Number(calculoEscolhido), tipo, formato });
      relatorios.recarregar();
    });
  }

  function enviar(relatorio: Relatorio) {
    if (!window.confirm(`Enviar o relatório "${relatorio.rotulo}" ao órgão regulador? O envio não pode ser desfeito.`)) return;
    void lista.executar(async () => {
      await api.enviarRelatorio(relatorio.id);
      relatorios.recarregar();
    });
  }

  return (
    <Pagina titulo="Relatórios" descricao="Gere o relatório de um cálculo, baixe em PDF ou Excel e envie ao órgão regulador.">
      <Cartao titulo="Gerar relatório">
        {calculos.dados?.length === 0 ? (
          <Vazio>Faça um cálculo de pegada antes de gerar relatórios.</Vazio>
        ) : (
          <form className="formulario" onSubmit={gerar}>
            <Campo rotulo="Cálculo">
              <select value={calculoEscolhido} onChange={(e) => setCalculoId(e.target.value)} required>
                {calculos.dados?.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.cenario} · {periodo(c.periodoInicio, c.periodoFim)} · {toneladas(c.totalCO2e)} t
                  </option>
                ))}
              </select>
            </Campo>
            <Campo rotulo="Tipo">
              <select value={tipo} onChange={(e) => setTipo(e.target.value as TipoRelatorio)}>
                <option value="INVENTARIO_EMISSOES">Inventário de emissões</option>
                <option value="PLANO_COMPENSACAO">Plano de compensação</option>
              </select>
            </Campo>
            <Campo rotulo="Formato">
              <select value={formato} onChange={(e) => setFormato(e.target.value as Formato)}>
                <option value="PDF">PDF</option>
                <option value="XLSX">Excel</option>
              </select>
            </Campo>
            <button type="submit" disabled={geracao.enviando || !calculoEscolhido}>
              {geracao.enviando ? 'Gerando…' : 'Gerar'}
            </button>
          </form>
        )}
        <Aviso>{geracao.erro ?? calculos.erro}</Aviso>
      </Cartao>

      <Cartao titulo="Relatórios gerados">
        <Aviso>{lista.erro ?? relatorios.erro}</Aviso>
        {relatorios.dados?.length === 0 ? (
          <Vazio>Nenhum relatório gerado para {empresa.nome}.</Vazio>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Relatório</th>
                <th>Cenário</th>
                <th>Gerado em</th>
                <th>Envio ao órgão</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {relatorios.dados?.map((r) => (
                <tr key={r.id}>
                  <td>
                    {r.rotulo} <small>({r.formato === 'XLSX' ? 'Excel' : 'PDF'})</small>
                  </td>
                  <td>{r.cenario}</td>
                  <td>{dataHora(r.geradoEm)}</td>
                  <td>
                    {r.protocoloEnvio ? (
                      <>
                        Protocolo <strong>{r.protocoloEnvio}</strong>
                        <br />
                        <small>{dataHora(r.enviadoEm!)}</small>
                      </>
                    ) : (
                      <small>Não enviado</small>
                    )}
                  </td>
                  <td className="acoes">
                    <button type="button" className="botao-secundario" disabled={lista.enviando} onClick={() => void lista.executar(() => api.baixarRelatorio(r))}>
                      Baixar
                    </button>
                    {podeGerir && !r.protocoloEnvio && (
                      <button type="button" className="botao-secundario" disabled={lista.enviando} onClick={() => enviar(r)}>
                        Enviar ao órgão
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Cartao>
    </Pagina>
  );
}

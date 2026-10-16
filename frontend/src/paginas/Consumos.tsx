import { useEffect, useState, type FormEvent } from 'react';
import { api, type Fonte } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, Pagina, SemEmpresa, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { mes, quantidade as formatarQuantidade } from '../formato';
import { useAcao, useDados } from '../hooks';

export default function Consumos() {
  const { podeGerir } = useAuth();
  const { empresa } = useEmpresa();
  const id = empresa?.id ?? null;
  const consumos = useDados(() => api.consumos(id!), id);
  const plantas = useDados(() => api.plantas(id!), id);
  const acao = useAcao();

  const ativas = plantas.dados?.filter((p) => p.status === 'ATIVA') ?? [];
  const [plantaId, setPlantaId] = useState<number | null>(null);
  const [fontes, setFontes] = useState<Fonte[]>([]);
  const [fonteId, setFonteId] = useState('');
  const [competencia, setCompetencia] = useState('');
  const [quantidade, setQuantidade] = useState('');

  const plantaEscolhida = ativas.find((p) => p.id === plantaId)?.id ?? ativas[0]?.id ?? null;

  useEffect(() => {
    setFonteId('');
    if (plantaEscolhida === null) {
      setFontes([]);
      return;
    }
    let ativo = true;
    api.fontes(plantaEscolhida).then((lista) => ativo && setFontes(lista), () => ativo && setFontes([]));
    return () => {
      ativo = false;
    };
  }, [plantaEscolhida]);

  if (!empresa) {
    return (
      <Pagina titulo="Consumos">
        <SemEmpresa />
      </Pagina>
    );
  }

  const unidade = fontes.find((f) => String(f.id) === fonteId)?.unidade;

  function registrar(evento: FormEvent) {
    evento.preventDefault();
    void acao.executar(async () => {
      await api.registrarConsumo({ fonteId: Number(fonteId), competencia, quantidade: Number(quantidade) });
      setQuantidade('');
      consumos.recarregar();
    });
  }

  return (
    <Pagina titulo="Consumos" descricao="Quanto cada fonte consumiu por mês. É a base do cálculo da pegada.">
      {podeGerir && (
        <Cartao titulo="Registrar consumo">
          {plantas.dados && ativas.length === 0 ? (
            <Vazio>Não há planta ativa. Ative uma planta em Plantas e fontes para registrar consumo.</Vazio>
          ) : (
            <form className="formulario" onSubmit={registrar}>
              <Campo rotulo="Planta">
                <select value={plantaEscolhida ?? ''} onChange={(e) => setPlantaId(Number(e.target.value))}>
                  {ativas.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.nome}
                    </option>
                  ))}
                </select>
              </Campo>
              <Campo rotulo="Fonte">
                <select value={fonteId} onChange={(e) => setFonteId(e.target.value)} required>
                  <option value="">{fontes.length === 0 ? 'Planta sem fontes' : 'Selecione…'}</option>
                  {fontes.map((f) => (
                    <option key={f.id} value={f.id}>
                      {f.descricao} ({f.rotulo})
                    </option>
                  ))}
                </select>
              </Campo>
              <Campo rotulo="Mês">
                <input type="month" value={competencia} onChange={(e) => setCompetencia(e.target.value)} required />
              </Campo>
              <Campo rotulo={unidade ? `Quantidade (${unidade})` : 'Quantidade'}>
                <input type="number" min="0" step="any" value={quantidade} onChange={(e) => setQuantidade(e.target.value)} required />
              </Campo>
              <button type="submit" disabled={acao.enviando}>
                Registrar
              </button>
            </form>
          )}
          <Aviso>{acao.erro}</Aviso>
        </Cartao>
      )}

      <Cartao titulo="Consumos registrados">
        <Aviso>{consumos.erro ?? plantas.erro}</Aviso>
        {consumos.dados?.length === 0 ? (
          <Vazio>Nenhum consumo registrado para {empresa.nome}.</Vazio>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Mês</th>
                <th>Planta</th>
                <th>Fonte</th>
                <th className="num">Quantidade</th>
              </tr>
            </thead>
            <tbody>
              {consumos.dados?.map((c) => (
                <tr key={c.id}>
                  <td>{mes(c.competencia)}</td>
                  <td>{c.planta}</td>
                  <td>{c.fonte}</td>
                  <td className="num">
                    {formatarQuantidade(c.quantidade)} {c.unidade}
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

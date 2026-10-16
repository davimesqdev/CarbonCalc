import { Link } from 'react-router-dom';
import { api } from '../api';
import { Aviso, Cartao, EmissoesPorEscopo, Pagina, SemEmpresa, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { dataHora, moeda, periodo, toneladas } from '../formato';
import { useDados } from '../hooks';

export default function Painel() {
  const { empresa } = useEmpresa();
  const id = empresa?.id ?? null;
  const calculos = useDados(() => api.calculos(id!), id);
  const plantas = useDados(() => api.plantas(id!), id);
  const ultimo = calculos.dados?.[0] ?? null;
  const plano = useDados(() => api.plano(ultimo!.id), ultimo?.id ?? null);

  if (!empresa) {
    return (
      <Pagina titulo="Painel">
        <SemEmpresa />
      </Pagina>
    );
  }

  const ativas = plantas.dados?.filter((p) => p.status === 'ATIVA').length ?? 0;
  const aCompensar = plano.dados && plano.dados.acoes.length > 0 ? plano.dados : null;

  return (
    <Pagina titulo={empresa.nome} descricao={`Setor: ${empresa.setor}`}>
      <Aviso>{calculos.erro ?? plantas.erro}</Aviso>
      <div className="indicadores">
        <div className="indicador destaque">
          <span>Emissões no último cálculo</span>
          <strong>{ultimo ? `${toneladas(ultimo.totalCO2e)} t` : '—'}</strong>
          <small>{ultimo ? `CO₂e · ${periodo(ultimo.periodoInicio, ultimo.periodoFim)}` : 'nenhum cálculo ainda'}</small>
        </div>
        <div className="indicador">
          <span>A compensar</span>
          <strong>{aCompensar ? `${toneladas(aCompensar.creditosNecessarios)} t` : '—'}</strong>
          <small>{aCompensar ? `custo estimado ${moeda(aCompensar.custoTotal)}` : 'nenhum plano gerado'}</small>
        </div>
        <div className="indicador">
          <span>Plantas ativas</span>
          <strong>{ativas}</strong>
          <small>de {plantas.dados?.length ?? 0} cadastrada(s)</small>
        </div>
      </div>

      <Cartao titulo="Emissões por escopo">
        {ultimo ? (
          <>
            <p className="legenda">
              {ultimo.cenario} · calculado em {dataHora(ultimo.dataCalculo)}
            </p>
            <EmissoesPorEscopo calculo={ultimo} />
          </>
        ) : (
          <Vazio>
            Ainda não há cálculo para esta empresa. O caminho é: cadastrar <Link to="/plantas">plantas e fontes</Link>,
            registrar <Link to="/consumos">consumos</Link> e então <Link to="/calculos">calcular a pegada</Link>.
          </Vazio>
        )}
      </Cartao>
    </Pagina>
  );
}

import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth';
import { EmpresaProvider, useEmpresa } from './empresa';
import Calculos from './paginas/Calculos';
import Compensacao from './paginas/Compensacao';
import Consumos from './paginas/Consumos';
import Empresas from './paginas/Empresas';
import Login from './paginas/Login';
import Painel from './paginas/Painel';
import Plantas from './paginas/Plantas';
import Relatorios from './paginas/Relatorios';
import Usuarios from './paginas/Usuarios';

const ROTULO_PERFIL = { ADMINISTRADOR: 'Administrador', GESTOR: 'Gestor ambiental', AUDITOR: 'Auditor' } as const;

export default function App() {
  const { sessao } = useAuth();
  if (!sessao) return <Login />;
  return (
    <EmpresaProvider>
      <Estrutura />
    </EmpresaProvider>
  );
}

function Estrutura() {
  const { sessao, ehAdmin, sair } = useAuth();
  const { empresas, empresa, selecionar } = useEmpresa();

  return (
    <div className="app">
      <aside className="lateral">
        <div className="marca">
          <img src="/logo.webp" alt="" />
          <span>CarbonCalc</span>
        </div>
        <nav>
          <NavLink to="/" end>Painel</NavLink>
          <NavLink to="/empresas">Empresas</NavLink>
          <NavLink to="/plantas">Plantas e fontes</NavLink>
          <NavLink to="/consumos">Consumos</NavLink>
          <NavLink to="/calculos">Cálculos</NavLink>
          <NavLink to="/compensacao">Compensação</NavLink>
          <NavLink to="/relatorios">Relatórios</NavLink>
          {ehAdmin && <NavLink to="/usuarios">Usuários</NavLink>}
        </nav>
        <div className="lateral-usuario">
          <strong>{sessao!.nome}</strong>
          <small>{ROTULO_PERFIL[sessao!.perfil]}</small>
          <button type="button" className="botao-secundario" onClick={sair}>
            Sair
          </button>
        </div>
      </aside>

      <div className="conteudo">
        <div className="barra-empresa">
          <label htmlFor="empresa-atual">Empresa</label>
          <select
            id="empresa-atual"
            value={empresa?.id ?? ''}
            onChange={(e) => selecionar(Number(e.target.value))}
            disabled={empresas.length === 0}
          >
            {empresas.length === 0 && <option value="">Nenhuma empresa cadastrada</option>}
            {empresas.map((e) => (
              <option key={e.id} value={e.id}>
                {e.nome}
              </option>
            ))}
          </select>
        </div>
        <main>
          <Routes>
            <Route path="/" element={<Painel />} />
            <Route path="/empresas" element={<Empresas />} />
            <Route path="/plantas" element={<Plantas />} />
            <Route path="/consumos" element={<Consumos />} />
            <Route path="/calculos" element={<Calculos />} />
            <Route path="/compensacao" element={<Compensacao />} />
            <Route path="/relatorios" element={<Relatorios />} />
            {ehAdmin && <Route path="/usuarios" element={<Usuarios />} />}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

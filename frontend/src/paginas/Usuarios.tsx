import { useState, type FormEvent } from 'react';
import { api, type Perfil, type Usuario } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, Pagina } from '../componentes';
import { useAcao, useDados } from '../hooks';

const ROTULO_PERFIL: Record<Perfil, string> = {
  ADMINISTRADOR: 'Administrador',
  GESTOR: 'Gestor ambiental',
  AUDITOR: 'Auditor',
};

export default function Usuarios() {
  const { sessao } = useAuth();
  const usuarios = useDados(() => api.usuarios(), 'usuarios');
  const acao = useAcao();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [perfil, setPerfil] = useState<Perfil>('GESTOR');

  function criar(evento: FormEvent) {
    evento.preventDefault();
    void acao.executar(async () => {
      await api.criarUsuario({ nome, email, senha, perfil });
      setNome('');
      setEmail('');
      setSenha('');
      usuarios.recarregar();
    });
  }

  function alternar(usuario: Usuario) {
    void acao.executar(async () => {
      await api.alterarAtivo(usuario.id, !usuario.ativo);
      usuarios.recarregar();
    });
  }

  return (
    <Pagina titulo="Usuários" descricao="Quem acessa o sistema e com qual perfil.">
      <Cartao titulo="Novo usuário">
        <form className="formulario" onSubmit={criar}>
          <Campo rotulo="Nome">
            <input value={nome} onChange={(e) => setNome(e.target.value)} required />
          </Campo>
          <Campo rotulo="E-mail">
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="off" />
          </Campo>
          <Campo rotulo="Senha (mínimo de 8 caracteres)">
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required minLength={8} autoComplete="new-password" />
          </Campo>
          <Campo rotulo="Perfil">
            <select value={perfil} onChange={(e) => setPerfil(e.target.value as Perfil)}>
              <option value="GESTOR">Gestor ambiental</option>
              <option value="AUDITOR">Auditor</option>
              <option value="ADMINISTRADOR">Administrador</option>
            </select>
          </Campo>
          <button type="submit" disabled={acao.enviando}>
            Criar usuário
          </button>
        </form>
        <Aviso>{acao.erro}</Aviso>
      </Cartao>

      <Cartao titulo="Usuários">
        <Aviso>{usuarios.erro}</Aviso>
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Perfil</th>
              <th>Situação</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {usuarios.dados?.map((u) => (
              <tr key={u.id}>
                <td>{u.nome}</td>
                <td>{u.email}</td>
                <td>{ROTULO_PERFIL[u.perfil]}</td>
                <td>
                  <span className={`etiqueta ${u.ativo ? 'etiqueta-ativa' : ''}`}>{u.ativo ? 'Ativo' : 'Inativo'}</span>
                </td>
                <td className="acoes">
                  {u.email !== sessao?.email && (
                    <button type="button" className="botao-secundario" disabled={acao.enviando} onClick={() => alternar(u)}>
                      {u.ativo ? 'Desativar' : 'Reativar'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Cartao>
    </Pagina>
  );
}

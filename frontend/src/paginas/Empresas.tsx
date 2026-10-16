import { useState, type FormEvent } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';
import { Aviso, Campo, Cartao, Pagina, Vazio } from '../componentes';
import { useEmpresa } from '../empresa';
import { cnpj as formatarCnpj } from '../formato';
import { useAcao } from '../hooks';

export default function Empresas() {
  const { ehAdmin } = useAuth();
  const { empresas, empresa, erro, selecionar, recarregar } = useEmpresa();
  const acao = useAcao();
  const [nome, setNome] = useState('');
  const [cnpj, setCnpj] = useState('');
  const [setor, setSetor] = useState('');

  function cadastrar(evento: FormEvent) {
    evento.preventDefault();
    void acao.executar(async () => {
      const criada = await api.criarEmpresa({ nome, cnpj: cnpj.replace(/\D/g, ''), setor });
      await recarregar();
      selecionar(criada.id);
      setNome('');
      setCnpj('');
      setSetor('');
    });
  }

  return (
    <Pagina titulo="Empresas" descricao="Cada empresa reúne suas plantas, consumos, cálculos e relatórios.">
      <Aviso>{erro}</Aviso>
      {ehAdmin && (
        <Cartao titulo="Cadastrar empresa">
          <form className="formulario" onSubmit={cadastrar}>
            <Campo rotulo="Nome">
              <input value={nome} onChange={(e) => setNome(e.target.value)} required />
            </Campo>
            <Campo rotulo="CNPJ">
              <input value={cnpj} onChange={(e) => setCnpj(e.target.value)} required placeholder="00.000.000/0000-00" inputMode="numeric" />
            </Campo>
            <Campo rotulo="Setor">
              <input value={setor} onChange={(e) => setSetor(e.target.value)} required />
            </Campo>
            <button type="submit" disabled={acao.enviando}>
              Cadastrar
            </button>
          </form>
          <Aviso>{acao.erro}</Aviso>
        </Cartao>
      )}

      <Cartao titulo="Empresas cadastradas">
        {empresas.length === 0 ? (
          <Vazio>Nenhuma empresa cadastrada.{!ehAdmin && ' Peça ao administrador para cadastrar a primeira.'}</Vazio>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>CNPJ</th>
                <th>Setor</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {empresas.map((e) => (
                <tr key={e.id}>
                  <td>{e.nome}</td>
                  <td>{formatarCnpj(e.cnpj)}</td>
                  <td>{e.setor}</td>
                  <td className="acoes">
                    {e.id === empresa?.id ? (
                      <span className="etiqueta etiqueta-ativa">Em uso</span>
                    ) : (
                      <button type="button" className="botao-secundario" onClick={() => selecionar(e.id)}>
                        Usar esta
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

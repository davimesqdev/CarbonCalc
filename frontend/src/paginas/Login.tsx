import { useState, type FormEvent } from 'react';
import { useAuth } from '../auth';
import { Aviso, Campo } from '../componentes';
import { useAcao } from '../hooks';

export default function Login() {
  const { entrar } = useAuth();
  const { enviando, erro, executar } = useAcao();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');

  function enviar(evento: FormEvent) {
    evento.preventDefault();
    void executar(() => entrar(email, senha));
  }

  return (
    <div className="login">
      <form className="login-cartao" onSubmit={enviar}>
        <img src="/logo.webp" alt="" />
        <h1>CarbonCalc</h1>
        <p>Pegada de carbono empresarial e plano de compensação.</p>
        <Campo rotulo="E-mail">
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus autoComplete="username" />
        </Campo>
        <Campo rotulo="Senha">
          <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required autoComplete="current-password" />
        </Campo>
        <Aviso>{erro}</Aviso>
        <button type="submit" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </div>
  );
}

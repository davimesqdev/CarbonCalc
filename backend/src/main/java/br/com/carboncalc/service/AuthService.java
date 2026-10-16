package br.com.carboncalc.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.NaoEncontradoException;
import br.com.carboncalc.domain.RegraNegocioException;
import br.com.carboncalc.domain.Usuario;
import br.com.carboncalc.dto.AuthDtos.LoginRequest;
import br.com.carboncalc.dto.AuthDtos.TokenResponse;
import br.com.carboncalc.dto.AuthDtos.UsuarioRequest;
import br.com.carboncalc.dto.AuthDtos.UsuarioResponse;
import br.com.carboncalc.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(request.email())
                .filter(Usuario::isAtivo)
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
        return new TokenResponse(tokens.emitir(usuario), usuario.getNome(), usuario.getEmail(),
                usuario.getPerfil());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarios.findAll(Sort.by("nome")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional
    public UsuarioResponse criarUsuario(UsuarioRequest request) {
        if (usuarios.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail " + request.email());
        }
        Usuario usuario = Usuario.criar(request.perfil(), request.nome(), request.email().toLowerCase(),
                passwordEncoder.encode(request.senha()));
        return UsuarioResponse.de(usuarios.save(usuario));
    }

    @Transactional
    public UsuarioResponse alterarAtivo(Long id, boolean ativo, String emailDoSolicitante) {
        Usuario usuario = usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário", id));
        if (!ativo && usuario.getEmail().equalsIgnoreCase(emailDoSolicitante)) {
            throw new RegraNegocioException("Você não pode desativar o próprio usuário");
        }
        usuario.setAtivo(ativo);
        return UsuarioResponse.de(usuario);
    }
}

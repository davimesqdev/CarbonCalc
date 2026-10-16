package br.com.carboncalc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.Perfil;
import br.com.carboncalc.domain.Usuario;
import br.com.carboncalc.repository.UsuarioRepository;

/** Cria um usuário de demonstração por perfil quando o banco ainda não tem usuários. */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String senha;

    public DataSeeder(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
            @Value("${carboncalc.seed.senha}") String senha) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(senha);
        usuarios.save(Usuario.criar(Perfil.ADMINISTRADOR, "Administrador", "admin@carboncalc.local", hash));
        usuarios.save(Usuario.criar(Perfil.GESTOR, "Gestor Ambiental", "gestor@carboncalc.local", hash));
        usuarios.save(Usuario.criar(Perfil.AUDITOR, "Auditor", "auditor@carboncalc.local", hash));
        log.info("Usuários de demonstração criados (senha em carboncalc.seed.senha)");
    }
}

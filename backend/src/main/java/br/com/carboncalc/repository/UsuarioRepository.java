package br.com.carboncalc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
}

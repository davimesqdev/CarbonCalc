package br.com.carboncalc.dto;

import br.com.carboncalc.domain.Perfil;
import br.com.carboncalc.domain.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {
    }

    public record TokenResponse(String token, String nome, String email, Perfil perfil) {
    }

    public record UsuarioRequest(
            @NotBlank String nome,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, message = "a senha deve ter ao menos 8 caracteres") String senha,
            @NotNull Perfil perfil) {
    }

    public record AtivoRequest(@NotNull Boolean ativo) {
    }

    public record UsuarioResponse(Long id, String nome, String email, Perfil perfil, boolean ativo) {

        public static UsuarioResponse de(Usuario usuario) {
            return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                    usuario.getPerfil(), usuario.isAtivo());
        }
    }
}

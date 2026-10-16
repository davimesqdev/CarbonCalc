package br.com.carboncalc.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.carboncalc.dto.AuthDtos.AtivoRequest;
import br.com.carboncalc.dto.AuthDtos.LoginRequest;
import br.com.carboncalc.dto.AuthDtos.TokenResponse;
import br.com.carboncalc.dto.AuthDtos.UsuarioRequest;
import br.com.carboncalc.dto.AuthDtos.UsuarioResponse;
import br.com.carboncalc.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    List<UsuarioResponse> listar() {
        return auth.listarUsuarios();
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        return auth.criarUsuario(request);
    }

    @PatchMapping("/usuarios/{id}/ativo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    UsuarioResponse alterarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return auth.alterarAtivo(id, request.ativo(), jwt.getSubject());
    }
}

package br.com.carboncalc.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.carboncalc.domain.Usuario;

@Service
public class TokenService {

    /** Claim com o perfil do usuário; a configuração de segurança o converte em ROLE_*. */
    public static final String CLAIM_PERFIL = "perfil";

    private final JwtEncoder encoder;
    private final long expiracaoMinutos;

    public TokenService(JwtEncoder encoder,
            @Value("${carboncalc.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.encoder = encoder;
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String emitir(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

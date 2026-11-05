package br.com.fiap.soat.mecanica.identity.adapters.out.security;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.security.JwtProperties;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.security.JwtTokenValidator;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.security.TokenInvalidoException;
import br.com.fiap.soat.mecanica.identity.application.auth.EmissorIndisponivelException;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenIssuerPort;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Emite o JWT RS256 do contrato jwt.md. Na inicializacao, com as duas chaves presentes, assina e valida
 * um token de amostra: um par trocado derrubaria todo token na borda, entao a aplicacao nao sobe.
 */
@Slf4j
@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final JwtProperties properties;
    private final RSAPrivateKey chavePrivada;
    private final Clock clock;

    @Autowired
    public JwtTokenIssuerAdapter(JwtProperties properties, JwtTokenValidator validator) {
        this(properties, validator, Clock.systemUTC());
    }

    JwtTokenIssuerAdapter(JwtProperties properties, JwtTokenValidator validator, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.chavePrivada = ChavePrivadaPem.carregar(properties.privateKey());
        if (chavePrivada == null) {
            log.atError().addKeyValue("event", "chave_privada_ausente")
                    .log("app.security.jwt.private-key vazio: os logins responderao 503");
        } else if (properties.publicKey() != null && !properties.publicKey().isBlank()) {
            verificarPar(validator);
        }
    }

    @Override
    public TokenEmitido emitir(UUID sub, TipoPrincipal tipo, String cargo) {
        if (chavePrivada == null) {
            throw new EmissorIndisponivelException();
        }
        Instant agora = clock.instant();
        long ttl = properties.ttl().toSeconds();
        var builder = Jwts.builder()
                .header().type("JWT").keyId(properties.kid()).and()
                .issuer(properties.issuer())
                .audience().add(properties.audience()).and()
                .subject(sub.toString())
                .claim("principal_type", tipo.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(ttl)));
        if (tipo == TipoPrincipal.FUNCIONARIO && cargo != null) {
            builder.claim("cargo", cargo);
        }
        return new TokenEmitido(builder.signWith(chavePrivada, Jwts.SIG.RS256).compact(), ttl);
    }

    private void verificarPar(JwtTokenValidator validator) {
        try {
            validator.validar(emitir(UUID.randomUUID(), TipoPrincipal.FUNCIONARIO, "ATENDENTE").accessToken());
        } catch (TokenInvalidoException | IllegalArgumentException e) {
            throw new IllegalStateException("par_jwt_inconsistente", e);
        }
    }
}

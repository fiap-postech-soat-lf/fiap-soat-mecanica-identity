package br.com.fiap.soat.mecanica.identity.adapters.in.web.security;

import br.com.fiap.soat.mecanica.identity.adapters.out.security.JwtTokenIssuerAdapter;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import br.com.fiap.soat.mecanica.identity.support.JwtTestTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Prova local de que a borda vai aceitar o token: o validador aplica as mesmas regras do authorizer. */
class TokenIdaEVoltaTest {

    private final JwtProperties props = new JwtProperties(JwtTestTokens.chavePublicaPem(), JwtTestTokens.ISSUER,
            JwtTestTokens.AUDIENCE, JwtTestTokens.chavePrivadaPem(), null, null);
    private final JwtTokenValidator validator = new JwtTokenValidator(props);
    private final JwtTokenIssuerAdapter issuer = new JwtTokenIssuerAdapter(props, validator);

    @Test
    @DisplayName("token de cada cargo emitido é aceito pelo validador")
    void funcionarios() {
        for (String cargo : new String[]{"ATENDENTE", "MECANICO", "ALMOXARIFE"}) {
            UUID sub = UUID.randomUUID();
            UsuarioAutenticado u = validator.validar(issuer.emitir(sub, TipoPrincipal.FUNCIONARIO, cargo).accessToken());
            assertThat(u).isEqualTo(new UsuarioAutenticado(sub, UsuarioAutenticado.TipoPrincipal.FUNCIONARIO, cargo));
        }
    }

    @Test
    @DisplayName("token de cliente é aceito sem cargo")
    void cliente() {
        UUID sub = UUID.randomUUID();
        UsuarioAutenticado u = validator.validar(issuer.emitir(sub, TipoPrincipal.CLIENTE, null).accessToken());
        assertThat(u).isEqualTo(new UsuarioAutenticado(sub, UsuarioAutenticado.TipoPrincipal.CLIENTE, null));
    }
}

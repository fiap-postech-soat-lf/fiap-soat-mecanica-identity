package br.com.fiap.soat.mecanica.identity.adapters.in.web.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioAutenticadoTest {

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void atualDevolveOUsuarioDaRequisicao() {
        UsuarioAutenticado usuario =
                new UsuarioAutenticado(UUID.randomUUID(), UsuarioAutenticado.TipoPrincipal.FUNCIONARIO, "MECANICO");
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(usuario, null));

        assertThat(UsuarioAutenticado.atual()).contains(usuario);
        assertThat(usuario.isCliente()).isFalse();
    }

    @Test
    void atualVazioSemAutenticacaoOuComOutroPrincipal() {
        assertThat(UsuarioAutenticado.atual()).isEmpty();

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("anonimo", null));

        assertThat(UsuarioAutenticado.atual()).isEmpty();
    }

    @Test
    void clienteNaoTemCargo() {
        UsuarioAutenticado cliente =
                new UsuarioAutenticado(UUID.randomUUID(), UsuarioAutenticado.TipoPrincipal.CLIENTE, null);

        assertThat(cliente.isCliente()).isTrue();
        assertThat(cliente.cargo()).isNull();
    }
}

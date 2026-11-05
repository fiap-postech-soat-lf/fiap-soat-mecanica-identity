package br.com.fiap.soat.mecanica.identity.application.usuario.usecase;

import br.com.fiap.soat.mecanica.identity.application.auth.EmissorIndisponivelException;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenIssuerPort;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import br.com.fiap.soat.mecanica.identity.domain.enums.StatusRecursoEnum;
import br.com.fiap.soat.mecanica.identity.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.soat.mecanica.identity.domain.usuario.Usuario;
import br.com.fiap.soat.mecanica.identity.domain.usuario.UsuarioRepository;
import br.com.fiap.soat.mecanica.identity.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoderPort encoder;
    @Mock
    private TokenIssuerPort issuer;
    @InjectMocks
    private AutenticarUsuarioUseCase useCase;

    @Test
    @DisplayName("emite token de FUNCIONARIO com o cargo do usuário")
    void emite() {
        Usuario usuario = TestDataFactory.criarUsuarioMecanico();
        TokenEmitido token = new TokenEmitido("t", 3600);
        when(repository.buscarPorEmail("teste@email.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("Senha@123", usuario.getSenha())).thenReturn(true);
        when(issuer.emitir(usuario.getId(), TipoPrincipal.FUNCIONARIO, "MECANICO")).thenReturn(token);

        assertThat(useCase.executar("teste@email.com", "Senha@123")).isSameAs(token);
    }

    @Test
    @DisplayName("e-mail inexistente -> CredenciaisInvalidas")
    void emailInexistente() {
        when(repository.buscarPorEmail("teste@email.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.executar("teste@email.com", "x"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(issuer);
    }

    @Test
    @DisplayName("e-mail malformado -> CredenciaisInvalidas (sem pista)")
    void emailMalformado() {
        assertThatThrownBy(() -> useCase.executar("nao-e-email", "x"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("senha errada -> CredenciaisInvalidas")
    void senhaErrada() {
        Usuario usuario = TestDataFactory.criarUsuarioMecanico();
        when(repository.buscarPorEmail("teste@email.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("errada", usuario.getSenha())).thenReturn(false);
        assertThatThrownBy(() -> useCase.executar("teste@email.com", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verifyNoInteractions(issuer);
    }

    @Test
    @DisplayName("usuário inativo -> CredenciaisInvalidas")
    void inativo() {
        Usuario usuario = TestDataFactory.criarUsuarioMecanico();
        usuario.inativar();
        when(repository.buscarPorEmail("teste@email.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("Senha@123", usuario.getSenha())).thenReturn(true);
        assertThatThrownBy(() -> useCase.executar("teste@email.com", "Senha@123"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    @DisplayName("propaga EmissorIndisponivelException")
    void semEmissor() {
        Usuario usuario = TestDataFactory.criarUsuarioMecanico();
        when(repository.buscarPorEmail("teste@email.com")).thenReturn(Optional.of(usuario));
        when(encoder.matches("Senha@123", usuario.getSenha())).thenReturn(true);
        when(issuer.emitir(any(), any(), any())).thenThrow(new EmissorIndisponivelException());
        assertThatThrownBy(() -> useCase.executar("teste@email.com", "Senha@123"))
                .isInstanceOf(EmissorIndisponivelException.class);
    }
}

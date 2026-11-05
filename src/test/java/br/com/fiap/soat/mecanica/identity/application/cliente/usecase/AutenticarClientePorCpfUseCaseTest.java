package br.com.fiap.soat.mecanica.identity.application.cliente.usecase;

import br.com.fiap.soat.mecanica.identity.application.auth.AcessoNegadoException;
import br.com.fiap.soat.mecanica.identity.application.auth.CpfInvalidoException;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenIssuerPort;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.CPF;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarClientePorCpfUseCaseTest {

    @Mock
    private ClienteRepository repository;
    @Mock
    private TokenIssuerPort issuer;
    @InjectMocks
    private AutenticarClientePorCpfUseCase useCase;

    @Test
    @DisplayName("emite token de CLIENTE sem cargo, com ou sem máscara")
    void emite() {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        TokenEmitido token = new TokenEmitido("t", 3600);
        when(repository.buscarPorCpf(any(CPF.class))).thenReturn(Optional.of(cliente));
        when(issuer.emitir(cliente.getId(), TipoPrincipal.CLIENTE, null)).thenReturn(token);

        assertThat(useCase.executar("529.982.247-25")).isSameAs(token);
        assertThat(useCase.executar("52998224725")).isSameAs(token);
    }

    @Test
    @DisplayName("CPF malformado ou com dígito errado -> CpfInvalido")
    void cpfInvalido() {
        assertThatThrownBy(() -> useCase.executar("123")).isInstanceOf(CpfInvalidoException.class);
        assertThatThrownBy(() -> useCase.executar("52998224724")).isInstanceOf(CpfInvalidoException.class);
        assertThatThrownBy(() -> useCase.executar(null)).isInstanceOf(CpfInvalidoException.class);
        verifyNoInteractions(repository, issuer);
    }

    @Test
    @DisplayName("cliente inexistente -> AcessoNegado")
    void inexistente() {
        when(repository.buscarPorCpf(any(CPF.class))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.executar("52998224725")).isInstanceOf(AcessoNegadoException.class);
        verifyNoInteractions(issuer);
    }

    @Test
    @DisplayName("cliente inativo -> AcessoNegado")
    void inativo() {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        cliente.inativar();
        when(repository.buscarPorCpf(any(CPF.class))).thenReturn(Optional.of(cliente));
        assertThatThrownBy(() -> useCase.executar("52998224725")).isInstanceOf(AcessoNegadoException.class);
    }
}

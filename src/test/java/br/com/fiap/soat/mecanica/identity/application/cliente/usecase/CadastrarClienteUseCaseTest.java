package br.com.fiap.soat.mecanica.identity.application.cliente.usecase;

import br.com.fiap.soat.mecanica.identity.application.security.UsuarioAutenticadoPort;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.domain.exception.RegraNegocioException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarClienteUseCaseTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioAutenticadoPort usuarioAutenticado;
    @InjectMocks
    private CadastrarClienteUseCase useCase;

    @Test
    @DisplayName("Deve cadastrar cliente com CPF com sucesso")
    void deveCadastrar_quandoComCpf() {
        // Arrange
        when(usuarioAutenticado.sub()).thenReturn(java.util.UUID.randomUUID());
        when(clienteRepository.buscarPorCpf(any())).thenReturn(Optional.empty());
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(clienteRepository.salvar(any())).thenReturn(cliente);

        // Act
        Cliente resultado = useCase.executar("Cliente", "52998224725", null, "c@c.com", "11999887766");

        // Assert
        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve cadastrar cliente com CNPJ com sucesso")
    void deveCadastrar_quandoComCnpj() {
        // Arrange
        when(usuarioAutenticado.sub()).thenReturn(java.util.UUID.randomUUID());
        when(clienteRepository.buscarPorCnpj(any())).thenReturn(Optional.empty());
        Cliente cliente = TestDataFactory.criarClienteComCnpj();
        when(clienteRepository.salvar(any())).thenReturn(cliente);

        // Act
        Cliente resultado = useCase.executar("Cliente", null, "11222333000181", "c@c.com", null);

        // Assert
        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando CPF já existente")
    void deveLancarExcecao_quandoCpfDuplicado() {
        // Arrange
        Cliente existente = TestDataFactory.criarClienteComCpf();
        when(clienteRepository.buscarPorCpf(any())).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThatThrownBy(() -> useCase.executar("Cliente", "52998224725", null, "c@c.com", null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("CPF já existente");
    }

    @Test
    @DisplayName("Deve lançar exceção quando CNPJ já existente")
    void deveLancarExcecao_quandoCnpjDuplicado() {
        // Arrange
        Cliente existente = TestDataFactory.criarClienteComCnpj();
        when(clienteRepository.buscarPorCnpj(any())).thenReturn(Optional.of(existente));

        // Act & Assert
        assertThatThrownBy(() -> useCase.executar("Cliente", null, "11222333000181", "c@c.com", null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("CNPJ já existente");
    }
}

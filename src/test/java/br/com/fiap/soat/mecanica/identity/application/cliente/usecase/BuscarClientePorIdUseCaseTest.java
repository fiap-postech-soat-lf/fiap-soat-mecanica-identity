package br.com.fiap.soat.mecanica.identity.application.cliente.usecase;

import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.soat.mecanica.identity.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarClientePorIdUseCaseTest {

    @Mock
    private ClienteRepository repository;
    @InjectMocks
    private BuscarClientePorIdUseCase useCase;

    @Test
    void retornaOClienteQuandoExiste() {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(repository.buscarPorId(cliente.getId())).thenReturn(Optional.of(cliente));

        assertThat(useCase.executar(cliente.getId())).isEqualTo(cliente);
    }

    @Test
    void lancaQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(id)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}

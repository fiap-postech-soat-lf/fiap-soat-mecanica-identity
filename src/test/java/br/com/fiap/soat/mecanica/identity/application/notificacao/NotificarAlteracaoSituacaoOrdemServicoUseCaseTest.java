package br.com.fiap.soat.mecanica.identity.application.notificacao;

import br.com.fiap.soat.mecanica.identity.application.notificacao.dto.NotificacaoOrdemServico;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.support.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class NotificarAlteracaoSituacaoOrdemServicoUseCaseTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private NotificacaoOrdemServicoPort port;
    @InjectMocks
    private NotificarAlteracaoSituacaoOrdemServicoUseCase useCase;

    @Test
    @DisplayName("busca o cliente pelo clienteId e envia a notificação")
    void envia() {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        UUID os = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        when(clienteRepository.buscarPorId(clienteId)).thenReturn(Optional.of(cliente));

        useCase.executar(os, clienteId, "A", "B");

        verify(port).enviar(new NotificacaoOrdemServico(os, clienteId, cliente.getNome(),
                cliente.getEmail().getValue(), "A", "B"));
    }

    @Test
    @DisplayName("cliente inexistente: loga notificacao_sem_destinatario e conclui sem lançar")
    void clienteInexistente(CapturedOutput output) {
        UUID clienteId = UUID.randomUUID();
        when(clienteRepository.buscarPorId(clienteId)).thenReturn(Optional.empty());

        useCase.executar(UUID.randomUUID(), clienteId, null, "B");

        verifyNoInteractions(port);
        assertThat(output).contains("notificacao_sem_destinatario").contains(clienteId.toString());
    }

    @Test
    @DisplayName("cliente sem e-mail também é descartado")
    void semEmail() {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        Cliente semEmail = Cliente.reconstruir(cliente.getId(), cliente.getStatus(), cliente.getNome(),
                cliente.getCpf(), null, null, null, cliente.getUsuarioId());
        when(clienteRepository.buscarPorId(any())).thenReturn(Optional.of(semEmail));

        useCase.executar(UUID.randomUUID(), UUID.randomUUID(), null, "B");

        verifyNoInteractions(port);
    }
}

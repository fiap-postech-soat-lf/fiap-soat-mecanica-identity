package br.com.fiap.soat.mecanica.identity.application.notificacao;

import br.com.fiap.soat.mecanica.identity.application.notificacao.dto.NotificacaoOrdemServico;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Notifica o cliente da OS. Roda dentro da transacao do ProcessadorMensagem. Cliente inexistente ou sem
 * e-mail e erro permanente: loga e conclui a mensagem, sem mandar para a DLQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificarAlteracaoSituacaoOrdemServicoUseCase {

    private final ClienteRepository clienteRepository;
    private final NotificacaoOrdemServicoPort notificacaoPort;

    public void executar(UUID osId, UUID clienteId, String de, String para) {
        Cliente cliente = clienteRepository.buscarPorId(clienteId).orElse(null);
        if (cliente == null || cliente.getEmail() == null) {
            log.atWarn().addKeyValue("event", "notificacao_sem_destinatario")
                    .addKeyValue("ordemServicoId", String.valueOf(osId))
                    .addKeyValue("clienteId", String.valueOf(clienteId))
                    .log("Cliente sem destinatario; notificacao descartada");
            return;
        }
        notificacaoPort.enviar(new NotificacaoOrdemServico(
                osId, clienteId, cliente.getNome(), cliente.getEmail().getValue(), de, para));
    }
}

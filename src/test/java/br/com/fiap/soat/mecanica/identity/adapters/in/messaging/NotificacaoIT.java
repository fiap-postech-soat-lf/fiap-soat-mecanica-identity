package br.com.fiap.soat.mecanica.identity.adapters.in.messaging;

import br.com.fiap.soat.mecanica.identity.support.IntegracaoTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.ClassPathResource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/** Exemplos do produtor (OS) entregues na fila do consumidor: log uma vez so, mesmo com reentrega. */
@ExtendWith(OutputCaptureExtension.class)
class NotificacaoIT extends IntegracaoTest {

    private static final String CLIENTE_SEED = "33333333-3333-3333-3333-333333333333";

    @Autowired
    private ObjectMapper objectMapper;

    private String exemplo(String nome, UUID messageId, String clienteId) throws Exception {
        ObjectNode envelope = (ObjectNode) objectMapper.readTree(
                new ClassPathResource("contratos-consumidos/exemplos/" + nome).getInputStream());
        envelope.put("messageId", messageId.toString());
        ((ObjectNode) envelope.get("payload")).put("clienteId", clienteId);
        return objectMapper.writeValueAsString(envelope);
    }

    private void enviar(String corpo) {
        SQS.sendMessage(r -> r.queueUrl(MENSAGERIA.filaEntrada()).messageBody(corpo));
    }

    private static long ocorrencias(String texto, String trecho) {
        return texto.split(java.util.regex.Pattern.quote(trecho), -1).length - 1L;
    }

    @Test
    @DisplayName("SituacaoOrdemServicoAlterada gera um log; o mesmo messageId de novo não gera outro")
    void idempotente(CapturedOutput output) throws Exception {
        UUID messageId = UUID.randomUUID();
        String corpo = exemplo("SituacaoOrdemServicoAlterada.v1.json", messageId, CLIENTE_SEED);
        enviar(corpo);
        await().until(() -> ocorrencias(output.getAll(), "notificacao_os_enviada") == 1);

        enviar(corpo);
        await().until(() -> output.getAll().contains("mensagem_duplicada") && mensagensNaFila(MENSAGERIA.filaEntrada()) == 0);

        assertThat(ocorrencias(output.getAll(), "notificacao_os_enviada")).isEqualTo(1);
        assertThat(output.getAll()).contains("c***@");
    }

    @Test
    @DisplayName("OrdemServicoCancelada notifica com para = CANCELADA")
    void cancelada(CapturedOutput output) throws Exception {
        enviar(exemplo("OrdemServicoCancelada.v1.json", UUID.randomUUID(), CLIENTE_SEED));
        await().until(() -> output.getAll().contains("CANCELADA"));
    }

    @Test
    @DisplayName("cliente inexistente: notificacao_sem_destinatario e a mensagem sai da fila (sem DLQ)")
    void clienteInexistente(CapturedOutput output) throws Exception {
        int antesNaDlq = mensagensNaFila(MENSAGERIA.dlq());
        String clienteId = UUID.randomUUID().toString();
        enviar(exemplo("SituacaoOrdemServicoAlterada.v1.json", UUID.randomUUID(), clienteId));

        await().until(() -> output.getAll().contains("notificacao_sem_destinatario")
                && mensagensNaFila(MENSAGERIA.filaEntrada()) == 0);
        assertThat(mensagensNaFila(MENSAGERIA.dlq())).isEqualTo(antesNaDlq);
    }

    @Test
    @DisplayName("type desconhecido é ignorado")
    void tipoDesconhecido(CapturedOutput output) throws Exception {
        String corpo = exemplo("SituacaoOrdemServicoAlterada.v1.json", UUID.randomUUID(), CLIENTE_SEED)
                .replace("\"SituacaoOrdemServicoAlterada\"", "\"TipoQueNaoExiste\"");
        enviar(corpo);
        await().until(() -> output.getAll().contains("mensagem_ignorada"));
    }
}

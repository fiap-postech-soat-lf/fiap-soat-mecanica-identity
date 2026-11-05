package br.com.fiap.soat.mecanica.identity.adapters.in.messaging;

import br.com.fiap.soat.mecanica.identity.application.mensageria.MensagemEnvelope;
import br.com.fiap.soat.mecanica.identity.application.notificacao.NotificarAlteracaoSituacaoOrdemServicoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Os handlers entendem os exemplos copiados do produtor (contratos-consumidos/). */
class HandlersNotificacaoTest {

    private static final UUID OS = UUID.fromString("7d1c2a4e-3b5f-4c6d-8e9f-0a1b2c3d4e5f");
    private static final UUID CLIENTE = UUID.fromString("5a4b3c2d-1e0f-4a9b-8c7d-6e5f4a3b2c1d");

    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
    private final NotificarAlteracaoSituacaoOrdemServicoUseCase useCase =
            mock(NotificarAlteracaoSituacaoOrdemServicoUseCase.class);

    private MensagemEnvelope exemplo(String nome) throws IOException {
        return mapper.readValue(new ClassPathResource("contratos-consumidos/exemplos/" + nome).getInputStream(),
                MensagemEnvelope.class);
    }

    @Test
    @DisplayName("SituacaoOrdemServicoAlterada.v1 chama o caso de uso com de e para")
    void situacaoAlterada() throws Exception {
        var handler = new SituacaoOrdemServicoAlteradaHandler(useCase);
        MensagemEnvelope envelope = exemplo("SituacaoOrdemServicoAlterada.v1.json");
        assertThat(handler.tipo()).isEqualTo(envelope.type());

        handler.processar(envelope);

        verify(useCase).executar(OS, CLIENTE, "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO");
    }

    @Test
    @DisplayName("de ausente ou nulo (abertura da OS) chega como null")
    void deNulo() throws Exception {
        var handler = new SituacaoOrdemServicoAlteradaHandler(useCase);
        var envelope = mapper.readValue("""
                {"messageId":"00000008-0000-4000-8000-000000000008","type":"SituacaoOrdemServicoAlterada","version":1,
                 "occurredAt":"2026-11-03T14:20:00Z",
                 "payload":{"osId":"%s","clienteId":"%s","de":null,"para":"RECEBIDA"}}
                """.formatted(OS, CLIENTE), MensagemEnvelope.class);

        handler.processar(envelope);

        verify(useCase).executar(OS, CLIENTE, null, "RECEBIDA");
    }

    @Test
    @DisplayName("OrdemServicoCancelada.v1 chama o caso de uso com para = CANCELADA")
    void cancelada() throws Exception {
        var handler = new OrdemServicoCanceladaHandler(useCase);
        MensagemEnvelope envelope = exemplo("OrdemServicoCancelada.v1.json");
        assertThat(handler.tipo()).isEqualTo(envelope.type());

        handler.processar(envelope);

        verify(useCase).executar(OS, CLIENTE, null, "CANCELADA");
    }
}

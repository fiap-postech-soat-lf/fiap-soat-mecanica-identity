package br.com.fiap.soat.mecanica.identity.adapters.in.messaging;

import br.com.fiap.soat.mecanica.identity.application.mensageria.MensagemEnvelope;
import br.com.fiap.soat.mecanica.identity.application.mensageria.MensagemHandler;
import br.com.fiap.soat.mecanica.identity.application.notificacao.NotificarAlteracaoSituacaoOrdemServicoUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SituacaoOrdemServicoAlteradaHandler implements MensagemHandler {

    private final NotificarAlteracaoSituacaoOrdemServicoUseCase useCase;

    @Override
    public String tipo() {
        return "SituacaoOrdemServicoAlterada";
    }

    @Override
    public void processar(MensagemEnvelope envelope) {
        JsonNode p = envelope.payload();
        useCase.executar(uuid(p, "osId"), uuid(p, "clienteId"), texto(p, "de"), texto(p, "para"));
    }

    static UUID uuid(JsonNode payload, String campo) {
        return UUID.fromString(payload.path(campo).asText());
    }

    static String texto(JsonNode payload, String campo) {
        JsonNode no = payload.path(campo);
        return no.isMissingNode() || no.isNull() ? null : no.asText();
    }
}

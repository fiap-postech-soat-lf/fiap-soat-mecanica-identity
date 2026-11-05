package br.com.fiap.soat.mecanica.identity.adapters.in.messaging;

import br.com.fiap.soat.mecanica.identity.application.mensageria.MensagemEnvelope;
import br.com.fiap.soat.mecanica.identity.application.mensageria.MensagemHandler;
import br.com.fiap.soat.mecanica.identity.application.notificacao.NotificarAlteracaoSituacaoOrdemServicoUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdemServicoCanceladaHandler implements MensagemHandler {

    private final NotificarAlteracaoSituacaoOrdemServicoUseCase useCase;

    @Override
    public String tipo() {
        return "OrdemServicoCancelada";
    }

    @Override
    public void processar(MensagemEnvelope envelope) {
        JsonNode p = envelope.payload();
        useCase.executar(SituacaoOrdemServicoAlteradaHandler.uuid(p, "osId"),
                SituacaoOrdemServicoAlteradaHandler.uuid(p, "clienteId"), null, "CANCELADA");
    }
}

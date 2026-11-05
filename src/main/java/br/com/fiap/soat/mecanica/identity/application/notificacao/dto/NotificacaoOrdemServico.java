package br.com.fiap.soat.mecanica.identity.application.notificacao.dto;

import java.util.UUID;

/** Situacoes como texto: o enum pertence ao servico de OS. {@code de} e nulo na abertura. */
public record NotificacaoOrdemServico(
        UUID ordemServicoId,
        UUID clienteId,
        String nomeCliente,
        String emailCliente,
        String situacaoAnterior,
        String novaSituacao
) {
}

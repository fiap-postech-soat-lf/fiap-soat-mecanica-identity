package br.com.fiap.soat.mecanica.identity.application.notificacao;

import br.com.fiap.soat.mecanica.identity.application.notificacao.dto.NotificacaoOrdemServico;

public interface NotificacaoOrdemServicoPort {

    void enviar(NotificacaoOrdemServico notificacao);
}

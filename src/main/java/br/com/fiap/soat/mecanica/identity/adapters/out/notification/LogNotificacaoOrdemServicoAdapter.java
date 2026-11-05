package br.com.fiap.soat.mecanica.identity.adapters.out.notification;

import br.com.fiap.soat.mecanica.identity.application.notificacao.NotificacaoOrdemServicoPort;
import br.com.fiap.soat.mecanica.identity.application.notificacao.dto.NotificacaoOrdemServico;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Corte 2: o "envio" e um log com o e-mail mascarado. SMTP fica como evolucao (ADR-F4-12). */
@Slf4j
@Component
public class LogNotificacaoOrdemServicoAdapter implements NotificacaoOrdemServicoPort {

    @Override
    public void enviar(NotificacaoOrdemServico n) {
        log.atInfo().addKeyValue("event", "notificacao_os_enviada")
                .addKeyValue("ordemServicoId", String.valueOf(n.ordemServicoId())).addKeyValue("clienteId", String.valueOf(n.clienteId()))
                .addKeyValue("de", n.situacaoAnterior()).addKeyValue("para", n.novaSituacao())
                .addKeyValue("canal", "log").addKeyValue("email", mascarar(n.emailCliente()))
                .log("Notificacao da OS enviada");
    }

    static String mascarar(String email) {
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(arroba);
    }
}

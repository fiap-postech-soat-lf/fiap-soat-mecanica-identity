package br.com.fiap.soat.mecanica.identity.adapters.out.notification;

import br.com.fiap.soat.mecanica.identity.application.notificacao.dto.NotificacaoOrdemServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LogNotificacaoOrdemServicoAdapterTest {

    @Test
    @DisplayName("loga notificacao_os_enviada com o e-mail mascarado")
    void mascaraOEmail(CapturedOutput output) {
        new LogNotificacaoOrdemServicoAdapter().enviar(new NotificacaoOrdemServico(
                UUID.randomUUID(), UUID.randomUUID(), "Cliente", "cliente@mecanica.test", "A", "B"));

        assertThat(output).contains("notificacao_os_enviada").contains("c***@mecanica.test")
                .contains("canal").doesNotContain("cliente@mecanica.test");
    }

    @Test
    @DisplayName("e-mail sem parte local vira ***")
    void emailAtipico() {
        assertThat(LogNotificacaoOrdemServicoAdapter.mascarar("@x.com")).isEqualTo("***");
        assertThat(LogNotificacaoOrdemServicoAdapter.mascarar("semarroba")).isEqualTo("***");
    }
}

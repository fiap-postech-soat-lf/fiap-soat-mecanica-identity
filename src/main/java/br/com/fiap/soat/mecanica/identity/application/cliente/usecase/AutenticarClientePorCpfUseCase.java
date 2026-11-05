package br.com.fiap.soat.mecanica.identity.application.cliente.usecase;

import br.com.fiap.soat.mecanica.identity.application.auth.AcessoNegadoException;
import br.com.fiap.soat.mecanica.identity.application.auth.CpfInvalidoException;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenIssuerPort;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.domain.exception.RegraNegocioException;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.CPF;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticarClientePorCpfUseCase {

    private final ClienteRepository repository;
    private final TokenIssuerPort tokenIssuer;

    public TokenEmitido executar(String documento) {
        CPF cpf = parse(documento);
        Cliente cliente = repository.buscarPorCpf(cpf)
                .filter(Cliente::isAtivo)
                .orElseThrow(() -> {
                    log.atInfo().addKeyValue("event", "login_negado").addKeyValue("principalType", "CLIENTE")
                            .addKeyValue("motivo", "cliente_nao_elegivel").log("Login negado");
                    return new AcessoNegadoException();
                });
        TokenEmitido token = tokenIssuer.emitir(cliente.getId(), TipoPrincipal.CLIENTE, null);
        log.atInfo().addKeyValue("event", "login_aceito").addKeyValue("principalType", "CLIENTE")
                .log("Login aceito");
        return token;
    }

    private CPF parse(String documento) {
        try {
            return new CPF(documento);
        } catch (RegraNegocioException e) {
            log.atInfo().addKeyValue("event", "login_negado").addKeyValue("principalType", "CLIENTE")
                    .addKeyValue("motivo", "cpf_invalido").log("Login negado");
            throw new CpfInvalidoException();
        }
    }
}

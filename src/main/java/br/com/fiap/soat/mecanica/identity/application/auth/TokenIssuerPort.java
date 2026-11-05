package br.com.fiap.soat.mecanica.identity.application.auth;

import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;

import java.util.UUID;

/** Emite o JWT RS256 do contrato jwt.md. */
public interface TokenIssuerPort {

    /**
     * @param cargo cargo do funcionario; ignorado (null) para cliente
     * @throws EmissorIndisponivelException se nao ha chave privada configurada
     */
    TokenEmitido emitir(UUID sub, TipoPrincipal tipo, String cargo);
}

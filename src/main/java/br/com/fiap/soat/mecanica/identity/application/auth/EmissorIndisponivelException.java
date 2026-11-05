package br.com.fiap.soat.mecanica.identity.application.auth;

/** Servico sem chave privada: nao consegue assinar tokens (responde 503). */
public class EmissorIndisponivelException extends RuntimeException {
    public EmissorIndisponivelException() {
        super("Emissor de tokens indisponível");
    }
}

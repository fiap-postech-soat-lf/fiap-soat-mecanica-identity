package br.com.fiap.soat.mecanica.identity.application.auth;

/** Cliente inexistente, inativo ou PJ no login por CPF (responde 401). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Acesso negado");
    }
}

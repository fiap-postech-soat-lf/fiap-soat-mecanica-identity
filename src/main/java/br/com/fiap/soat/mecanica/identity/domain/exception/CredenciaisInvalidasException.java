package br.com.fiap.soat.mecanica.identity.domain.exception;

/** E-mail inexistente, senha errada ou usuario inativo. Sempre a mesma mensagem, para nao enumerar e-mails. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}

package br.com.fiap.soat.mecanica.identity.application.auth;

/** CPF com formato ou digito verificador invalido no login (responde 400). */
public class CpfInvalidoException extends RuntimeException {
    public CpfInvalidoException() {
        super("CPF inválido");
    }
}

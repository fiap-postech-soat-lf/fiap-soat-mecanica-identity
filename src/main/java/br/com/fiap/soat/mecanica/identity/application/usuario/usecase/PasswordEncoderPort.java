package br.com.fiap.soat.mecanica.identity.application.usuario.usecase;

public interface PasswordEncoderPort {

    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}

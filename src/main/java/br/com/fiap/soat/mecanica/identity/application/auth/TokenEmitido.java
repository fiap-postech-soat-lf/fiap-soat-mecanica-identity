package br.com.fiap.soat.mecanica.identity.application.auth;

/** Token assinado e sua validade em segundos. */
public record TokenEmitido(String accessToken, long expiresIn) {
}

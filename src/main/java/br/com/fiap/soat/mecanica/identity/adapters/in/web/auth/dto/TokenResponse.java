package br.com.fiap.soat.mecanica.identity.adapters.in.web.auth.dto;

import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    public static TokenResponse de(TokenEmitido token) {
        return new TokenResponse(token.accessToken(), "Bearer", token.expiresIn());
    }
}

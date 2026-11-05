package br.com.fiap.soat.mecanica.identity.adapters.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String senha) {
}

package br.com.fiap.soat.mecanica.identity.adapters.in.web.usuario.dto;

import br.com.fiap.soat.mecanica.identity.domain.enums.CargoEnum;
import br.com.fiap.soat.mecanica.identity.domain.enums.StatusRecursoEnum;

import java.util.UUID;

public record UsuarioResponse (
        UUID id,
        StatusRecursoEnum statusRecursoEnum,
        String nome,
        String email,
        CargoEnum cargoEnum
) {}

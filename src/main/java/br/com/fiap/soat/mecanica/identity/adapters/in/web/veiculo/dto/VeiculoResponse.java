package br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.dto;

import br.com.fiap.soat.mecanica.identity.domain.enums.StatusRecursoEnum;

import java.util.UUID;

public record VeiculoResponse(

        UUID id,
        StatusRecursoEnum status,
        String placa,
        String marca,
        String modelo,
        String ano,
        Integer quantidadeEixos,
        UUID clienteId
) {}

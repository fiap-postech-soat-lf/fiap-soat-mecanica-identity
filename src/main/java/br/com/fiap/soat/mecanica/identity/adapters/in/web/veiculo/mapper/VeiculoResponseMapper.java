package br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.mapper;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.dto.VeiculoResponse;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;

public class VeiculoResponseMapper {

    public static VeiculoResponse toResponse(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getStatus(), v.getPlaca().getValue(), v.getMarca(),
                v.getModelo(), v.getAno(), v.getQuantidadeEixos(), v.getClienteId());
    }
}
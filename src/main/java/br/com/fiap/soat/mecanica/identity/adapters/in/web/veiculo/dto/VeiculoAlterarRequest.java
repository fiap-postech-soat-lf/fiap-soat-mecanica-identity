package br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VeiculoAlterarRequest(

        @NotBlank(message = "A marca não pode ser nula ou vazia")
        String marca,

        @NotBlank(message = "O modelo não pode ser nulo ou vazio")
        String modelo,

        @NotBlank(message = "O ano não pode ser nulo ou vazio")
        @Pattern(regexp = "\\d{4}", message = "O ano deve ter 4 dígitos")
        String ano,

        @NotNull(message = "A quantidade de eixos não pode ser nula")
        Integer quantidadeEixos
) {
}

package br.com.fiap.soat.mecanica.identity.application.veiculo.usecase;

import br.com.fiap.soat.mecanica.identity.domain.exception.RegraNegocioException;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.Placa;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CadastrarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;

    public Veiculo executar(String placa, String marca, String modelo, String ano, Integer quantidadeEixos, UUID clienteId) {

        veiculoRepository.buscarPorPlaca(placa)
                .ifPresent(veiculo -> {
                    throw new RegraNegocioException("Placa já cadastrada");
                });

        Veiculo veiculo = new Veiculo(new Placa(placa), marca, modelo,
                ano, quantidadeEixos, clienteId);
        return veiculoRepository.salvar(veiculo);
    }
}

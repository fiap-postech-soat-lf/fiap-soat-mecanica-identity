package br.com.fiap.soat.mecanica.identity.application.veiculo.usecase;

import br.com.fiap.soat.mecanica.identity.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarVeiculoPorPlacaUseCase {

    private final VeiculoRepository veiculoRepository;

    public Veiculo executar(String placa) {
        return veiculoRepository.buscarPorPlaca(placa)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }
}

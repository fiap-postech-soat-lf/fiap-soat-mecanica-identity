package br.com.fiap.soat.mecanica.identity.application.veiculo.usecase;

import br.com.fiap.soat.mecanica.identity.domain.veiculo.VeiculoRepository;
import br.com.fiap.soat.mecanica.identity.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuscarVeiculoPorIdUseCase {

    private final VeiculoRepository veiculoRepository;

    public Veiculo executar(UUID id) {
        return veiculoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado"));
    }
}

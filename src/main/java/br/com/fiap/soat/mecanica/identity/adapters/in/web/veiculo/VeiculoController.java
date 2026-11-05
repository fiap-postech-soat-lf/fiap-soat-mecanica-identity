package br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.dto.VeiculoAlterarRequest;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.dto.VeiculoIncluirRequest;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.dto.VeiculoResponse;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo.mapper.VeiculoResponseMapper;
import br.com.fiap.soat.mecanica.identity.application.veiculo.usecase.AlterarVeiculoUseCase;
import br.com.fiap.soat.mecanica.identity.application.veiculo.usecase.BuscarTodosVeiculosPorClienteIdUseCase;
import br.com.fiap.soat.mecanica.identity.application.veiculo.usecase.BuscarVeiculoPorIdUseCase;
import br.com.fiap.soat.mecanica.identity.application.veiculo.usecase.BuscarVeiculoPorPlacaUseCase;
import br.com.fiap.soat.mecanica.identity.application.veiculo.usecase.CadastrarVeiculoUseCase;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final CadastrarVeiculoUseCase cadastrarVeiculoUseCase;
    private final AlterarVeiculoUseCase alterarVeiculoUseCase;
    private final BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;
    private final BuscarVeiculoPorPlacaUseCase buscarVeiculoPorPlacaUseCase;
    private final BuscarTodosVeiculosPorClienteIdUseCase buscarTodosVeiculosPorClienteIdUseCase;

    @PostMapping
    @Operation(summary = "Cadastrar um veículo")
    @PreAuthorize("hasRole('ATENDENTE')")
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoIncluirRequest request) {
        Veiculo veiculo = cadastrarVeiculoUseCase.executar(request.placa(), request.marca(), request.modelo(),
                request.ano(), request.quantidadeEixos(), request.clienteId());
        return ResponseEntity.ok(VeiculoResponseMapper.toResponse(veiculo));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Alterar um veículo")
    @PreAuthorize("hasRole('ATENDENTE')")
    public ResponseEntity<VeiculoResponse> alterar(@PathVariable UUID id,
                                                   @Valid @RequestBody VeiculoAlterarRequest request) {
        Veiculo veiculo = alterarVeiculoUseCase.executar(id, request.marca(), request.modelo(),
                request.ano(), request.quantidadeEixos());
        return ResponseEntity.ok(VeiculoResponseMapper.toResponse(veiculo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um veículo por ID (ATENDENTE e MECANICO, os cargos que abrem OS)")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'MECANICO')")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable UUID id) {
        Veiculo veiculo = buscarVeiculoPorIdUseCase.executar(id);
        return ResponseEntity.ok(VeiculoResponseMapper.toResponse(veiculo));
    }

    @GetMapping("/por-placa/{placa}")
    @Operation(summary = "Buscar um veículo por placa")
    @PreAuthorize("hasRole('ATENDENTE')")
    public ResponseEntity<VeiculoResponse> buscarPorPlaca(@PathVariable String placa) {
        Veiculo veiculo = buscarVeiculoPorPlacaUseCase.executar(placa);
        return ResponseEntity.ok(VeiculoResponseMapper.toResponse(veiculo));
    }

    @GetMapping("/por-cliente/{clienteId}")
    @PreAuthorize("hasRole('ATENDENTE')")
    @Operation(summary = "Buscar veículos por cliente ID")
    public ResponseEntity<List<VeiculoResponse>> buscarPorClienteId(
            @PathVariable UUID clienteId) {

        List<Veiculo> veiculos = buscarTodosVeiculosPorClienteIdUseCase.executar(clienteId);

        List<VeiculoResponse> response = veiculos.stream()
                .map(VeiculoResponseMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}

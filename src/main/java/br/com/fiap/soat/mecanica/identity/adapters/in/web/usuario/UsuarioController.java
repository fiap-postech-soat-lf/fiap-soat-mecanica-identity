package br.com.fiap.soat.mecanica.identity.adapters.in.web.usuario;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.usuario.dto.UsuarioIncluirRequest;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.usuario.dto.UsuarioResponse;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.usuario.mapper.UsuarioResponseMapper;
import br.com.fiap.soat.mecanica.identity.application.usuario.usecase.CadastrarUsuarioUseCase;
import br.com.fiap.soat.mecanica.identity.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    @PostMapping
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @Operation(summary = "Cadastrar um usuário (qualquer funcionário autenticado)")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioIncluirRequest request) {
        Usuario usuario = cadastrarUsuarioUseCase.executar(request.senha(), request.nome(), request.email(), request.cargoEnum());
        return ResponseEntity.ok(UsuarioResponseMapper.toResponse(usuario));
    }
}
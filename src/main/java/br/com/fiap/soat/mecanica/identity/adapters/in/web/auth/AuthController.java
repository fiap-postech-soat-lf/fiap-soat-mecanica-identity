package br.com.fiap.soat.mecanica.identity.adapters.in.web.auth;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.auth.dto.CpfLoginRequest;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.auth.dto.LoginRequest;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.auth.dto.TokenResponse;
import br.com.fiap.soat.mecanica.identity.application.cliente.usecase.AutenticarClientePorCpfUseCase;
import br.com.fiap.soat.mecanica.identity.application.usuario.usecase.AutenticarUsuarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final AutenticarClientePorCpfUseCase autenticarClientePorCpfUseCase;

    @PostMapping("/login")
    @Operation(summary = "Login de funcionário (e-mail e senha)")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return resposta(TokenResponse.de(autenticarUsuarioUseCase.executar(request.email(), request.senha())));
    }

    @PostMapping("/cpf")
    @Operation(summary = "Login de cliente pessoa física (CPF)")
    public ResponseEntity<TokenResponse> loginPorCpf(@Valid @RequestBody CpfLoginRequest request) {
        return resposta(TokenResponse.de(autenticarClientePorCpfUseCase.executar(request.cpf())));
    }

    private static ResponseEntity<TokenResponse> resposta(TokenResponse token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(token);
    }
}

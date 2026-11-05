package br.com.fiap.soat.mecanica.identity.application.usuario.usecase;

import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenIssuerPort;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import br.com.fiap.soat.mecanica.identity.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.soat.mecanica.identity.domain.exception.RegraNegocioException;
import br.com.fiap.soat.mecanica.identity.domain.usuario.Usuario;
import br.com.fiap.soat.mecanica.identity.domain.usuario.UsuarioRepository;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticarUsuarioUseCase {

    private final UsuarioRepository repository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenIssuerPort tokenIssuer;

    public TokenEmitido executar(String email, String senha) {
        Usuario usuario = buscar(email)
                .filter(u -> senha != null && passwordEncoder.matches(senha, u.getSenha()))
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> negar("credenciais"));
        TokenEmitido token = tokenIssuer.emitir(usuario.getId(), TipoPrincipal.FUNCIONARIO,
                usuario.getCargoEnum().name());
        log.atInfo().addKeyValue("event", "login_aceito").addKeyValue("principalType", "FUNCIONARIO")
                .log("Login aceito");
        return token;
    }

    private java.util.Optional<Usuario> buscar(String email) {
        try {
            return repository.buscarPorEmail(new Email(email).getValue());
        } catch (RegraNegocioException e) {
            return java.util.Optional.empty();
        }
    }

    private CredenciaisInvalidasException negar(String motivo) {
        log.atInfo().addKeyValue("event", "login_negado").addKeyValue("principalType", "FUNCIONARIO")
                .addKeyValue("motivo", motivo).log("Login negado");
        return new CredenciaisInvalidasException();
    }
}

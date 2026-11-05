package br.com.fiap.soat.mecanica.identity.support;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.security.JwtTokenValidator;
import br.com.fiap.soat.mecanica.identity.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-anotacao dos testes de controller: carrega a seguranca real (SecurityConfig + JwtTokenValidator).
 * Os tokens sao assinados de verdade por {@link JwtTestTokens}; a chave publica vem de {@link ChavePublicaDeTeste}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SecurityConfig.class, JwtTokenValidator.class, ChavePublicaDeTeste.class})
@TestPropertySource(properties = {
        "app.security.jwt.issuer=" + JwtTestTokens.ISSUER,
        "app.security.jwt.audience=" + JwtTestTokens.AUDIENCE
})
public @interface WebMvcSeguro {
}

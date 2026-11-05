package br.com.fiap.soat.mecanica.identity.adapters.in.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;

/**
 * Configuracao do JWT RS256 emitido pelo Identity (contrato em docs/contratos/jwt.md do -k8s).
 *
 * @param publicKey  chave publica em PEM (X.509 SubjectPublicKeyInfo) ou "file:&lt;caminho&gt;" para ler de arquivo
 * @param issuer     valor exigido no claim iss
 * @param audience   valor que o claim aud precisa conter
 * @param privateKey chave privada em PEM (PKCS#8), PEM com \n escapado ou "file:&lt;caminho&gt;"; vazia = sem emissao
 * @param kid        key id do header (padrao mecanica-identity-v1)
 * @param ttl        validade do token emitido (padrao 1 hora)
 */
@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String publicKey, String issuer, String audience, String privateKey, String kid,
                            Duration ttl) {

    @ConstructorBinding
    public JwtProperties(String publicKey, String issuer, String audience, String privateKey, String kid,
                         Duration ttl) {
        this.publicKey = publicKey;
        this.issuer = issuer;
        this.audience = audience;
        this.privateKey = privateKey;
        this.kid = kid == null || kid.isBlank() ? "mecanica-identity-v1" : kid;
        this.ttl = ttl == null ? Duration.ofHours(1) : ttl;
    }

    public JwtProperties(String publicKey, String issuer, String audience) {
        this(publicKey, issuer, audience, null, null, null);
    }
}

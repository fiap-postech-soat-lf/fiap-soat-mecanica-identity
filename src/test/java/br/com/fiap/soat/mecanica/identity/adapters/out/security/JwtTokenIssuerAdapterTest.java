package br.com.fiap.soat.mecanica.identity.adapters.out.security;

import br.com.fiap.soat.mecanica.identity.adapters.in.web.security.JwtProperties;
import br.com.fiap.soat.mecanica.identity.adapters.in.web.security.JwtTokenValidator;
import br.com.fiap.soat.mecanica.identity.application.auth.EmissorIndisponivelException;
import br.com.fiap.soat.mecanica.identity.application.security.TipoPrincipal;
import br.com.fiap.soat.mecanica.identity.support.JwtTestTokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenIssuerAdapterTest {

    private static final UUID SUB = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static JwtProperties props(String publica, String privada) {
        return new JwtProperties(publica, JwtTestTokens.ISSUER, JwtTestTokens.AUDIENCE, privada, null, null);
    }

    private static JwtTokenIssuerAdapter adapter(String privada) {
        JwtProperties p = props(JwtTestTokens.chavePublicaPem(), privada);
        return new JwtTokenIssuerAdapter(p, new JwtTokenValidator(p));
    }

    private static Claims claims(String token) throws Exception {
        String base64 = JwtTestTokens.chavePublicaPem().replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
        PublicKey chave = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }

    @Test
    @DisplayName("emite header RS256/JWT/kid e os claims do jwt.md para funcionário")
    void emiteFuncionario() throws Exception {
        String token = adapter(JwtTestTokens.chavePrivadaPem()).emitir(SUB, TipoPrincipal.FUNCIONARIO, "MECANICO")
                .accessToken();

        var jws = Jwts.parser().verifyWith(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(JwtTestTokens.chavePublicaPem()
                        .replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", ""))))).build().parseSignedClaims(token);
        assertThat(jws.getHeader().getAlgorithm()).isEqualTo("RS256");
        assertThat(jws.getHeader().getType()).isEqualTo("JWT");
        assertThat(jws.getHeader().getKeyId()).isEqualTo("mecanica-identity-v1");
        Claims c = jws.getPayload();
        assertThat(c.getIssuer()).isEqualTo(JwtTestTokens.ISSUER);
        assertThat(c.getAudience()).containsExactly(JwtTestTokens.AUDIENCE);
        assertThat(c.getSubject()).isEqualTo(SUB.toString());
        assertThat(c.get("principal_type")).isEqualTo("FUNCIONARIO");
        assertThat(c.get("cargo")).isEqualTo("MECANICO");
        assertThat(c.getId()).isNotBlank();
    }

    @Test
    @DisplayName("cliente não leva cargo; exp - iat = 3600 com o relógio injetado")
    void clienteSemCargoComTtl() throws Exception {
        Instant agora = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        JwtProperties p = props(JwtTestTokens.chavePublicaPem(), JwtTestTokens.chavePrivadaPem());
        var adapter = new JwtTokenIssuerAdapter(p, new JwtTokenValidator(p), Clock.fixed(agora, ZoneOffset.UTC));

        var emitido = adapter.emitir(SUB, TipoPrincipal.CLIENTE, "MECANICO");
        Claims c = claims(emitido.accessToken());

        assertThat(emitido.expiresIn()).isEqualTo(3600);
        assertThat(c.get("cargo")).isNull();
        assertThat(c.getIssuedAt().toInstant()).isEqualTo(agora);
        assertThat(c.getExpiration().toInstant()).isEqualTo(agora.plusSeconds(3600));
    }

    @Test
    @DisplayName("jti diferente a cada emissão")
    void jtiUnico() throws Exception {
        var a = adapter(JwtTestTokens.chavePrivadaPem());
        assertThat(claims(a.emitir(SUB, TipoPrincipal.CLIENTE, null).accessToken()).getId())
                .isNotEqualTo(claims(a.emitir(SUB, TipoPrincipal.CLIENTE, null).accessToken()).getId());
    }

    @Test
    @DisplayName("aceita a chave em PEM com \\n escapado")
    void pemEscapado() {
        String escapado = JwtTestTokens.chavePrivadaPem().replace("\n", "\\n");
        assertThat(adapter(escapado).emitir(SUB, TipoPrincipal.CLIENTE, null).accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("aceita a chave em file:<caminho>")
    void arquivo(@TempDir Path dir) throws Exception {
        Path pem = Files.writeString(dir.resolve("priv.pem"), JwtTestTokens.chavePrivadaPem());
        assertThat(adapter("file:" + pem).emitir(SUB, TipoPrincipal.CLIENTE, null).accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("sem chave privada o serviço sobe e emitir lança EmissorIndisponivelException")
    void semChave() {
        var adapter = adapter("");
        assertThatThrownBy(() -> adapter.emitir(SUB, TipoPrincipal.CLIENTE, null))
                .isInstanceOf(EmissorIndisponivelException.class);
    }

    @Test
    @DisplayName("par trocado falha na inicialização com par_jwt_inconsistente")
    void parTrocado() {
        JwtProperties p = props(JwtTestTokens.chavePublicaDeOutroParPem(), JwtTestTokens.chavePrivadaPem());
        var validator = new JwtTokenValidator(p);
        assertThatThrownBy(() -> new JwtTokenIssuerAdapter(p, validator))
                .isInstanceOf(IllegalStateException.class).hasMessage("par_jwt_inconsistente");
    }

    @Test
    @DisplayName("chave privada malformada ou arquivo inexistente falham na inicialização")
    void chaveInvalida() {
        assertThatThrownBy(() -> adapter("lixo")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> adapter("file:/nao/existe.pem")).isInstanceOf(IllegalStateException.class);
    }
}

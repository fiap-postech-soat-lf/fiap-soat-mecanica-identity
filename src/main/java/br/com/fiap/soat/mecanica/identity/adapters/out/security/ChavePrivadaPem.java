package br.com.fiap.soat.mecanica.identity.adapters.out.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/** Le a chave privada RSA (PKCS#8): PEM, PEM com \n escapado ou file:&lt;caminho&gt;. */
final class ChavePrivadaPem {

    private ChavePrivadaPem() {
    }

    /** @return a chave, ou null se o valor estiver vazio */
    static RSAPrivateKey carregar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String pem = valor.startsWith("file:") ? lerArquivo(valor.substring("file:".length())) : valor;
        String base64 = pem.replace("\\n", "\n")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (GeneralSecurityException | IllegalArgumentException | ClassCastException e) {
            throw new IllegalStateException("app.security.jwt.private-key nao e uma chave RSA PKCS#8 em PEM", e);
        }
    }

    private static String lerArquivo(String caminho) {
        try {
            return Files.readString(Path.of(caminho));
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler a chave privada em " + caminho, e);
        }
    }
}

package br.com.fiap.soat.mecanica.identity;

import br.com.fiap.soat.mecanica.identity.support.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/** O Flyway aplica V1-V5 e o seed (usuarios, clientes e veiculos) existe. */
class MigracoesIT extends IntegracaoTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void flywayAplicaDoV1AoV5() {
        Integer aplicadas = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success AND version IN ('1','2','3','4','5')",
                Integer.class);
        assertThat(aplicadas).isEqualTo(5);
    }

    @Test
    void seedTemOsSeteRegistros() {
        assertThat(contar("usuarios")).isEqualTo(3);
        assertThat(contar("clientes")).isEqualTo(2);
        assertThat(contar("veiculos")).isEqualTo(2);
    }

    @Test
    void seedTemOsUuidsEOsDocumentosCombinados() {
        assertThat(jdbc.queryForObject(
                "SELECT cargo_enum FROM usuarios WHERE id = '22222222-2222-2222-2222-222222222222'", String.class))
                .isEqualTo("MECANICO");
        assertThat(jdbc.queryForObject(
                "SELECT cpf FROM clientes WHERE id = '33333333-3333-3333-3333-333333333333'", String.class))
                .isEqualTo("52998224725");
        assertThat(jdbc.queryForObject(
                "SELECT cnpj FROM clientes WHERE id = '33333333-3333-3333-3333-333333333334'", String.class))
                .isEqualTo("11222333000181");
        assertThat(jdbc.queryForObject(
                "SELECT placa FROM veiculos WHERE id = '44444444-4444-4444-4444-444444444444'", String.class))
                .isEqualTo("ABC1D23");
    }

    private int contar(String tabela) {
        // O seed e fixo; os ITs de repositorio rodam em transacao com rollback.
        return jdbc.queryForObject("SELECT count(*) FROM " + tabela, Integer.class);
    }
}

package br.com.fiap.soat.mecanica.identity.adapters.in.web.security;

import br.com.fiap.soat.mecanica.identity.support.IntegracaoTest;
import br.com.fiap.soat.mecanica.identity.support.JwtTestTokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matriz de perfis (tabela 3.2 da F3): para cada rota, sem token -> 401, perfil errado -> 403 e perfil certo
 * passa da seguranca (2xx/404/422, nunca 401, 403 ou 5xx). Roda no contexto completo com o seed e em
 * transacao com rollback, entao as rotas de escrita nao deixam dados.
 */
@Transactional
class PerfisIT extends IntegracaoTest {

    static final String CLIENTE_PF = "33333333-3333-3333-3333-333333333333";
    static final String VEICULO_PF = "44444444-4444-4444-4444-444444444444";
    static final String ATENDENTE_ID = "11111111-1111-1111-1111-111111111111";

    static final String CLIENTE = "CLIENTE";
    static final String ATENDENTE = "ATENDENTE";
    static final String MECANICO = "MECANICO";
    static final String ALMOXARIFE = "ALMOXARIFE";
    static final List<String> PERFIS = List.of(CLIENTE, ATENDENTE, MECANICO, ALMOXARIFE);

    record Rota(HttpMethod metodo, String caminho, String corpo, Set<String> permitidos) {
        @Override
        public String toString() {
            return metodo + " " + caminho + " -> " + permitidos;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    static Stream<Rota> rotas() {
        String novoCliente = """
                {"nome":"Cliente Novo","cpf":"11144477735","email":"novo@mecanica.test","telefone":"11999887766"}
                """;
        String novoVeiculo = """
                {"placa":"DEF5G67","marca":"Honda","modelo":"Civic","ano":"2022","quantidadeEixos":2,"clienteId":"%s"}
                """.formatted(CLIENTE_PF);
        String veiculoAlterado = """
                {"marca":"Honda","modelo":"Civic","ano":"2022","quantidadeEixos":2}
                """;
        String novoUsuario = """
                {"nome":"Novo Funcionario","email":"novo.func@mecanica.test","senha":"Senha@123","cargoEnum":"MECANICO"}
                """;
        Set<String> soAtendente = Set.of(ATENDENTE);
        return Stream.of(
                new Rota(HttpMethod.GET, "/clientes/me", null, Set.of(CLIENTE)),
                new Rota(HttpMethod.POST, "/clientes", novoCliente, soAtendente),
                new Rota(HttpMethod.PATCH, "/clientes/" + CLIENTE_PF, "{\"nome\":\"Cliente Alterado\"}", soAtendente),
                new Rota(HttpMethod.GET, "/clientes/" + CLIENTE_PF, null, soAtendente),
                new Rota(HttpMethod.GET, "/clientes/por-cpf/52998224725", null, soAtendente),
                new Rota(HttpMethod.GET, "/clientes/por-cnpj/11222333000181", null, soAtendente),
                new Rota(HttpMethod.GET, "/clientes/por-usuario/" + ATENDENTE_ID, null, soAtendente),
                new Rota(HttpMethod.POST, "/veiculos", novoVeiculo, soAtendente),
                new Rota(HttpMethod.PATCH, "/veiculos/" + VEICULO_PF, veiculoAlterado, soAtendente),
                new Rota(HttpMethod.GET, "/veiculos/por-placa/ABC1D23", null, soAtendente),
                new Rota(HttpMethod.GET, "/veiculos/por-cliente/" + CLIENTE_PF, null, soAtendente),
                new Rota(HttpMethod.GET, "/veiculos/" + VEICULO_PF, null, Set.of(ATENDENTE, MECANICO)),
                new Rota(HttpMethod.POST, "/usuarios", novoUsuario, Set.of(ATENDENTE, MECANICO, ALMOXARIFE))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rotas")
    @DisplayName("sem token responde 401")
    void semTokenResponde401(Rota rota) throws Exception {
        mockMvc.perform(requisicao(rota, null)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rotas")
    @DisplayName("perfil errado responde 403")
    void perfilErradoResponde403(Rota rota) throws Exception {
        for (String perfil : PERFIS) {
            if (!rota.permitidos().contains(perfil)) {
                mockMvc.perform(requisicao(rota, perfil))
                        .andExpect(status().isForbidden());
            }
        }
    }

    /** Um caso por (rota, perfil permitido): cada um roda na sua transacao, entao POSTs repetidos nao colidem. */
    static Stream<Arguments> rotasEPerfisPermitidos() {
        return rotas().flatMap(rota -> rota.permitidos().stream().sorted()
                .map(perfil -> Arguments.of(Named.of(rota.metodo() + " " + rota.caminho(), rota), perfil)));
    }

    @ParameterizedTest(name = "{0} com {1}")
    @MethodSource("rotasEPerfisPermitidos")
    @DisplayName("perfil certo passa da seguranca")
    void perfilCertoPassa(Rota rota, String perfil) throws Exception {
        int status = mockMvc.perform(requisicao(rota, perfil)).andReturn().getResponse().getStatus();
        assertThat(status).as("%s com %s", rota, perfil).isBetween(200, 299);
    }

    @Test
    @DisplayName("GET /veiculos/{id} com MECANICO devolve placa, clienteId e status (o que o OS guarda)")
    void mecanicoLeVeiculoComOsCamposDoOs() throws Exception {
        mockMvc.perform(get("/veiculos/" + VEICULO_PF).header("Authorization", "Bearer " + token(MECANICO)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.placa").value("ABC1D23"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.clienteId").value(CLIENTE_PF))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.status").value("ATIVO"));
    }

    private static MockHttpServletRequestBuilder requisicao(Rota rota, String perfil) {
        MockHttpServletRequestBuilder builder = request(rota.metodo(), rota.caminho());
        if (rota.corpo() != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(rota.corpo());
        }
        if (perfil != null) {
            builder.header("Authorization", "Bearer " + token(perfil));
        }
        return builder;
    }

    private static String token(String perfil) {
        if (CLIENTE.equals(perfil)) {
            return JwtTestTokens.cliente(UUID.fromString(CLIENTE_PF));
        }
        return JwtTestTokens.funcionario(UUID.fromString(ATENDENTE_ID), perfil);
    }
}

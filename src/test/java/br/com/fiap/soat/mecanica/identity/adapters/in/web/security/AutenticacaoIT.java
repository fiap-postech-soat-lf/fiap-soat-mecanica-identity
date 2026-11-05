package br.com.fiap.soat.mecanica.identity.adapters.in.web.security;

import br.com.fiap.soat.mecanica.identity.support.IntegracaoTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo de ponta a ponta com o seed: o token emitido pelo proprio servico abre as rotas. */
@Transactional
class AutenticacaoIT extends IntegracaoTest {

    private static final String CLIENTE_PF = "33333333-3333-3333-3333-333333333333";
    private static final String VEICULO_PF = "44444444-4444-4444-4444-444444444444";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String token(String caminho, String corpo) throws Exception {
        String json = mockMvc.perform(post(caminho).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode no = objectMapper.readTree(json);
        return "Bearer " + no.get("accessToken").asText();
    }

    @Test
    @DisplayName("cliente: /auth/cpf -> /clientes/me devolve o cliente do seed")
    void clienteLogaEConsultaPerfil() throws Exception {
        String bearer = token("/auth/cpf", "{\"cpf\":\"529.982.247-25\"}");
        mockMvc.perform(get("/clientes/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CLIENTE_PF));
    }

    @Test
    @DisplayName("mecânico: /auth/login -> GET /veiculos/{id} 200 e POST /clientes 403")
    void mecanicoLeVeiculoMasNaoCadastraCliente() throws Exception {
        String bearer = token("/auth/login", "{\"email\":\"mecanico@mecanica.test\",\"senha\":\"Senha@123\"}");
        mockMvc.perform(get("/veiculos/" + VEICULO_PF).header("Authorization", bearer))
                .andExpect(status().isOk());
        mockMvc.perform(post("/clientes").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Cliente Novo\",\"cpf\":\"11144477735\",\"email\":\"x@mecanica.test\",\"telefone\":\"11999887766\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /usuarios sem token -> 401")
    void cadastroDeUsuarioExigeToken() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("erros de login: senha errada e e-mail inexistente dão o mesmo 401; CPF desconhecido 401; CPF malformado 400")
    void errosDeLogin() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mecanico@mecanica.test\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value("CREDENCIAIS_INVALIDAS"));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@mecanica.test\",\"senha\":\"x\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value("CREDENCIAIS_INVALIDAS"));
        mockMvc.perform(post("/auth/cpf").contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"11144477735\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value("ACESSO_NEGADO"));
        mockMvc.perform(post("/auth/cpf").contentType(MediaType.APPLICATION_JSON).content("{\"cpf\":\"123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("CPF_INVALIDO"));
    }
}

package br.com.fiap.soat.mecanica.identity.adapters.in.web.veiculo;

import br.com.fiap.soat.mecanica.identity.application.veiculo.usecase.*;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;
import br.com.fiap.soat.mecanica.identity.support.JwtTestTokens;
import br.com.fiap.soat.mecanica.identity.support.TestDataFactory;
import br.com.fiap.soat.mecanica.identity.support.WebMvcSeguro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcSeguro
@WebMvcTest(value = VeiculoController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CadastrarVeiculoUseCase cadastrarUseCase;
    @MockitoBean
    private AlterarVeiculoUseCase alterarUseCase;
    @MockitoBean
    private BuscarVeiculoPorIdUseCase buscarPorIdUseCase;
    @MockitoBean
    private BuscarVeiculoPorPlacaUseCase buscarPorPlacaUseCase;
    @MockitoBean
    private BuscarTodosVeiculosPorClienteIdUseCase buscarTodosPorClienteUseCase;

    private static String token(String cargo) {
        return "Bearer " + JwtTestTokens.funcionario(UUID.randomUUID(), cargo);
    }

    @Test
    @DisplayName("Deve cadastrar veículo com role ATENDENTE")
    void deveCadastrar_quandoAtendente() throws Exception {
        Veiculo veiculo = TestDataFactory.criarVeiculoValido();
        when(cadastrarUseCase.executar(anyString(), anyString(), anyString(), anyString(), any(Integer.class), any())).thenReturn(veiculo);

        String json = """
                {"placa":"ABC1234","marca":"Toyota","modelo":"Corolla","ano":"2023","quantidadeEixos":2,"clienteId":"%s"}
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).header("Authorization", token("ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar veículo por placa")
    void deveBuscarPorPlaca() throws Exception {
        Veiculo veiculo = TestDataFactory.criarVeiculoValido();
        when(buscarPorPlacaUseCase.executar(anyString())).thenReturn(veiculo);

        mockMvc.perform(get("/veiculos/por-placa/{placa}", "ABC1234").header("Authorization", token("ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar veiculo por ID")
    void deveBuscarPorId() throws Exception {
        Veiculo veiculo = TestDataFactory.criarVeiculoValido();
        when(buscarPorIdUseCase.executar(any())).thenReturn(veiculo);

        mockMvc.perform(get("/veiculos/{id}", veiculo.getId()).header("Authorization", token("ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar veiculos por cliente ID")
    void deveBuscarPorClienteId() throws Exception {
        Veiculo veiculo = TestDataFactory.criarVeiculoValido();
        when(buscarTodosPorClienteUseCase.executar(any())).thenReturn(List.of(veiculo));

        mockMvc.perform(get("/veiculos/por-cliente/{clienteId}", UUID.randomUUID()).header("Authorization", token("ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MECANICO busca veículo por ID (F3-a: o OS repassa o JWT de quem abre a OS)")
    void mecanicoBuscaPorId() throws Exception {
        Veiculo veiculo = TestDataFactory.criarVeiculoValido();
        when(buscarPorIdUseCase.executar(any())).thenReturn(veiculo);

        mockMvc.perform(get("/veiculos/{id}", veiculo.getId()).header("Authorization", token("MECANICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(veiculo.getClienteId().toString()))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("ALMOXARIFE não busca veículo por ID")
    void almoxarifeNaoBuscaPorId() throws Exception {
        mockMvc.perform(get("/veiculos/{id}", UUID.randomUUID()).header("Authorization", token("ALMOXARIFE")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MECANICO não cadastra veículo")
    void mecanicoNaoCadastra() throws Exception {
        mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"ABC1234","marca":"Toyota","modelo":"Corolla","ano":"2023","quantidadeEixos":2,"clienteId":"%s"}
                                """.formatted(UUID.randomUUID()))
                        .header("Authorization", token("MECANICO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve alterar veículo com role ATENDENTE")
    void deveAlterar() throws Exception {
        Veiculo veiculo = TestDataFactory.criarVeiculoValido();
        when(alterarUseCase.executar(any(), anyString(), anyString(), anyString(), any(Integer.class))).thenReturn(veiculo);

        String json = """
                {"marca":"Honda","modelo":"Civic","ano":"2022","quantidadeEixos":2}
                """;

        mockMvc.perform(patch("/veiculos/{id}", veiculo.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(json)
                        .header("Authorization", token("ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve rejeitar alteração com ano inválido")
    void deveRejeitarAnoInvalido() throws Exception {
        String json = """
                {"marca":"Honda","modelo":"Civic","ano":"22","quantidadeEixos":2}
                """;

        mockMvc.perform(patch("/veiculos/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(json)
                        .header("Authorization", token("ATENDENTE")))
                .andExpect(status().isBadRequest());
    }
}

package br.com.fiap.soat.mecanica.identity.adapters.in.web.cliente;

import br.com.fiap.soat.mecanica.identity.application.cliente.usecase.*;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
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

@WebMvcTest(value = ClienteController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@WebMvcSeguro
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CadastrarClienteUseCase cadastrarUseCase;
    @MockitoBean
    private AlterarClienteUseCase alterarUseCase;
    @MockitoBean
    private BuscarClientePorCpfUseCase buscarCpfUseCase;
    @MockitoBean
    private BuscarClientePorCnpjUseCase buscarCnpjUseCase;
    @MockitoBean
    private BuscarClientePorUsuarioIdUseCase buscarUsuarioIdUseCase;
    @MockitoBean
    private BuscarClienteAutenticadoUseCase buscarClienteAutenticadoUseCase;
    @MockitoBean
    private BuscarClientePorIdUseCase buscarIdUseCase;

    private static String atendente() {
        return "Bearer " + JwtTestTokens.funcionario(UUID.randomUUID(), "ATENDENTE");
    }

    private static String cliente() {
        return "Bearer " + JwtTestTokens.cliente(UUID.randomUUID());
    }

    @Test
    @DisplayName("Deve cadastrar cliente com role ATENDENTE")
    void deveCadastrar_quandoAtendente() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(cadastrarUseCase.executar(anyString(), anyString(), any(), anyString(), any())).thenReturn(cliente);

        String json = """
                {"nome":"Teste","cpf":"52998224725","email":"c@c.com"}
                """;

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).header("Authorization", atendente()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF")
    void deveBuscarPorCpf() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(buscarCpfUseCase.executar(anyString())).thenReturn(cliente);

        mockMvc.perform(get("/clientes/por-cpf/{cpf}", "52998224725").header("Authorization", atendente()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve alterar cliente")
    void deveAlterar() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(alterarUseCase.executar(any(), anyString(), anyString())).thenReturn(cliente);

        String json = """
                {"nome":"Cliente Alterado","telefone":"11999887766"}
                """;

        mockMvc.perform(patch("/clientes/{id}", cliente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).header("Authorization", atendente()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar cliente por CNPJ")
    void deveBuscarPorCnpj() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCnpj();
        when(buscarCnpjUseCase.executar(anyString())).thenReturn(cliente);

        mockMvc.perform(get("/clientes/por-cnpj/{cnpj}", "11222333000181").header("Authorization", atendente()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar clientes por usuario ID")
    void deveBuscarTodosPorUsuarioId() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(buscarUsuarioIdUseCase.executar(any())).thenReturn(List.of(cliente));

        mockMvc.perform(get("/clientes/por-usuario/{usuarioId}", UUID.randomUUID()).header("Authorization", atendente()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve buscar dados do cliente autenticado com role CLIENTE")
    void deveBuscarAutenticado_quandoRoleCliente() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(buscarClienteAutenticadoUseCase.executar()).thenReturn(cliente);

        mockMvc.perform(get("/clientes/me").header("Authorization", cliente()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve negar acesso a /clientes/me quando role não é CLIENTE")
    void deveNegarAutenticado_quandoRoleDiferente() throws Exception {
        mockMvc.perform(get("/clientes/me").header("Authorization", atendente()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com role ATENDENTE")
    void deveBuscarPorId() throws Exception {
        Cliente cliente = TestDataFactory.criarClienteComCpf();
        when(buscarIdUseCase.executar(cliente.getId())).thenReturn(cliente);

        mockMvc.perform(get("/clientes/{id}", cliente.getId()).header("Authorization", atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cliente.getId().toString()));
    }

    @Test
    @DisplayName("Deve responder 401 sem token")
    void deveResponder401_semToken() throws Exception {
        mockMvc.perform(get("/clientes/me"))
                .andExpect(status().isUnauthorized());
    }
}

package br.com.fiap.soat.mecanica.identity.adapters.in.web.usuario;

import br.com.fiap.soat.mecanica.identity.application.usuario.usecase.CadastrarUsuarioUseCase;
import br.com.fiap.soat.mecanica.identity.domain.usuario.Usuario;
import br.com.fiap.soat.mecanica.identity.support.JwtTestTokens;
import br.com.fiap.soat.mecanica.identity.support.TestDataFactory;
import br.com.fiap.soat.mecanica.identity.support.WebMvcSeguro;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcSeguro
@WebMvcTest(value = UsuarioController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CadastrarUsuarioUseCase cadastrarUseCase;

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso")
    void deveCadastrar_quandoDadosValidos() throws Exception {
        // Arrange
        Usuario usuario = TestDataFactory.criarUsuarioMecanico();
        when(cadastrarUseCase.executar(anyString(), anyString(), anyString(), any())).thenReturn(usuario);

        String json = """
                {"senha":"Senha@123","nome":"Teste","email":"teste@email.com","cargoEnum":"MECANICO"}
                """;

        // Act & Assert
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json).header("Authorization", "Bearer " + JwtTestTokens.funcionario(UUID.randomUUID(), "MECANICO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(usuario.getNome()));
    }

    @Test
    @DisplayName("Deve responder 401 sem token (POST /usuarios deixou de ser público)")
    void deveResponder401_semToken() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve responder 403 para cliente")
    void deveResponder403_paraCliente() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senha":"Senha@123","nome":"Teste","email":"teste@email.com","cargoEnum":"MECANICO"}
                                """)
                        .header("Authorization", "Bearer " + JwtTestTokens.cliente(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}

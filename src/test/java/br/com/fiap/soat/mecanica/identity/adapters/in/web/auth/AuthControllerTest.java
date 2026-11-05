package br.com.fiap.soat.mecanica.identity.adapters.in.web.auth;

import br.com.fiap.soat.mecanica.identity.application.auth.AcessoNegadoException;
import br.com.fiap.soat.mecanica.identity.application.auth.CpfInvalidoException;
import br.com.fiap.soat.mecanica.identity.application.auth.EmissorIndisponivelException;
import br.com.fiap.soat.mecanica.identity.application.auth.TokenEmitido;
import br.com.fiap.soat.mecanica.identity.application.cliente.usecase.AutenticarClientePorCpfUseCase;
import br.com.fiap.soat.mecanica.identity.application.usuario.usecase.AutenticarUsuarioUseCase;
import br.com.fiap.soat.mecanica.identity.domain.exception.CredenciaisInvalidasException;
import br.com.fiap.soat.mecanica.identity.support.WebMvcSeguro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcSeguro
@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
class AuthControllerTest {

    private static final String LOGIN = "{\"email\":\"a@mecanica.test\",\"senha\":\"Senha@123\"}";
    private static final String CPF = "{\"cpf\":\"529.982.247-25\"}";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AutenticarUsuarioUseCase autenticarUsuario;
    @MockitoBean
    private AutenticarClientePorCpfUseCase autenticarCliente;

    @Test
    @DisplayName("POST /auth/login sem token responde 200 com accessToken, Bearer e 3600, sem cache")
    void loginAceito() throws Exception {
        when(autenticarUsuario.executar("a@mecanica.test", "Senha@123")).thenReturn(new TokenEmitido("tok", 3600));
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.accessToken").value("tok"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("POST /auth/cpf responde 200 com o mesmo corpo")
    void cpfAceito() throws Exception {
        when(autenticarCliente.executar("529.982.247-25")).thenReturn(new TokenEmitido("tok", 3600));
        mockMvc.perform(post("/auth/cpf").contentType(MediaType.APPLICATION_JSON).content(CPF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("corpo sem campos obrigatórios responde 400")
    void corpoInvalido() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/auth/cpf").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("credenciais inválidas -> 401 CREDENCIAIS_INVALIDAS")
    void credenciais() throws Exception {
        when(autenticarUsuario.executar("a@mecanica.test", "Senha@123")).thenThrow(new CredenciaisInvalidasException());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    @DisplayName("CPF inválido -> 400 CPF_INVALIDO; cliente não elegível -> 401 ACESSO_NEGADO")
    void erroDeCpf() throws Exception {
        when(autenticarCliente.executar("529.982.247-25")).thenThrow(new CpfInvalidoException());
        mockMvc.perform(post("/auth/cpf").contentType(MediaType.APPLICATION_JSON).content(CPF))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("CPF_INVALIDO"));

        org.mockito.Mockito.reset(autenticarCliente);
        when(autenticarCliente.executar("529.982.247-25")).thenThrow(new AcessoNegadoException());
        mockMvc.perform(post("/auth/cpf").contentType(MediaType.APPLICATION_JSON).content(CPF))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("sem chave privada -> 503 EMISSOR_INDISPONIVEL")
    void semEmissor() throws Exception {
        when(autenticarUsuario.executar("a@mecanica.test", "Senha@123")).thenThrow(new EmissorIndisponivelException());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("EMISSOR_INDISPONIVEL"));
    }
}

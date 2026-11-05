package br.com.fiap.soat.mecanica.identity.support;

import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.enums.CargoEnum;
import br.com.fiap.soat.mecanica.identity.domain.enums.StatusRecursoEnum;
import br.com.fiap.soat.mecanica.identity.domain.usuario.Usuario;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.*;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;

import java.util.UUID;

public class TestDataFactory {

    public static final String CPF_VALIDO = "52998224725";
    public static final String CNPJ_VALIDO = "11222333000181";
    public static final String EMAIL_VALIDO = "teste@email.com";
    public static final String TELEFONE_VALIDO = "11999887766";
    public static final String PLACA_ANTIGA = "ABC1234";
    public static final String PLACA_MERCOSUL = "ABC1D23";
    public static final String SENHA_VALIDA = "Senha@123";

    public static Usuario criarUsuarioMecanico() {
        return Usuario.reconstruir(
                UUID.randomUUID(),
                StatusRecursoEnum.ATIVO,
                "Mecânico Teste",
                "$2a$10$encodedPassword",
                new Email(EMAIL_VALIDO),
                CargoEnum.MECANICO
        );
    }

    public static Usuario criarUsuarioAtendente() {
        return Usuario.reconstruir(
                UUID.randomUUID(),
                StatusRecursoEnum.ATIVO,
                "Atendente Teste",
                "$2a$10$encodedPassword",
                new Email("atendente@email.com"),
                CargoEnum.ATENDENTE
        );
    }

    public static Usuario criarUsuarioAlmoxarife() {
        return Usuario.reconstruir(
                UUID.randomUUID(),
                StatusRecursoEnum.ATIVO,
                "Almoxarife Teste",
                "$2a$10$encodedPassword",
                new Email("almoxarife@email.com"),
                CargoEnum.ALMOXARIFE
        );
    }

    public static Cliente criarClienteComCpf() {
        UUID usuarioId = UUID.randomUUID();
        return Cliente.reconstruir(
                UUID.randomUUID(),
                StatusRecursoEnum.ATIVO,
                "Cliente CPF",
                new CPF(CPF_VALIDO),
                null,
                new Email(EMAIL_VALIDO),
                new Telefone(TELEFONE_VALIDO),
                usuarioId
        );
    }

    public static Cliente criarClienteComCnpj() {
        UUID usuarioId = UUID.randomUUID();
        return Cliente.reconstruir(
                UUID.randomUUID(),
                StatusRecursoEnum.ATIVO,
                "Cliente CNPJ",
                null,
                new CNPJ(CNPJ_VALIDO),
                new Email("cnpj@email.com"),
                new Telefone(TELEFONE_VALIDO),
                usuarioId
        );
    }

    public static Veiculo criarVeiculoValido() {
        return Veiculo.reconstruir(
                UUID.randomUUID(),
                StatusRecursoEnum.ATIVO,
                new Placa(PLACA_ANTIGA),
                "Toyota",
                "Corolla",
                "2023",
                2,
                UUID.randomUUID()
        );
    }

}

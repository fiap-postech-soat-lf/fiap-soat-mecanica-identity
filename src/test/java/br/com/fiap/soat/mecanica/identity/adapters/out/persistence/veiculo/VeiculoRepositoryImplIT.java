package br.com.fiap.soat.mecanica.identity.adapters.out.persistence.veiculo;

import br.com.fiap.soat.mecanica.identity.adapters.out.persistence.veiculo.mapper.VeiculoMapper;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.Placa;
import br.com.fiap.soat.mecanica.identity.domain.veiculo.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;
import br.com.fiap.soat.mecanica.identity.support.IntegracaoTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class VeiculoRepositoryImplIT extends IntegracaoTest {

    @Autowired
    private VeiculoRepositoryImpl repository;

    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    }

    @Test
    @DisplayName("Deve salvar e buscar veículo por placa")
    void deveSalvarEBuscarPorPlaca() {
        Veiculo veiculo = new Veiculo(new Placa("ABC1234"), "Toyota", "Corolla", "2023", 2, clienteId);

        repository.salvar(veiculo);
        Optional<Veiculo> encontrado = repository.buscarPorPlaca("ABC1234");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMarca()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("Deve buscar todos veículos por cliente ID")
    void deveBuscarTodosPorClienteId() {
        Veiculo v1 = new Veiculo(new Placa("ABC1234"), "Toyota", "Corolla", "2023", 2, clienteId);
        Veiculo v2 = new Veiculo(new Placa("DEF5G67"), "Honda", "Civic", "2022", 2, clienteId);
        repository.salvar(v1);
        repository.salvar(v2);

        List<Veiculo> veiculos = repository.buscarTodosPorClienteId(clienteId);

        // o cliente do seed ja tem um veiculo (ABC1D23)
        assertThat(veiculos).extracting(v -> v.getPlaca().getValue())
                .contains("ABC1234", "DEF5G67", "ABC1D23").hasSize(3);
    }

    @Test
    @DisplayName("Deve buscar veículo por ID")
    void deveBuscarPorId() {
        Veiculo veiculo = new Veiculo(new Placa("ABC1234"), "Toyota", "Corolla", "2023", 2, clienteId);
        Veiculo salvo = repository.salvar(veiculo);

        Optional<Veiculo> encontrado = repository.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getPlaca().getValue()).isEqualTo("ABC1234");
    }
}

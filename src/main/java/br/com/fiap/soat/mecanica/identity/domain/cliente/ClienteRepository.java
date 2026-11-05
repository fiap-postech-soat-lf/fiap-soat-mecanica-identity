package br.com.fiap.soat.mecanica.identity.domain.cliente;

import br.com.fiap.soat.mecanica.identity.domain.valueobject.CNPJ;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.CPF;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {

    Cliente salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorCpf(CPF documento);

    Optional<Cliente> buscarPorCnpj(CNPJ documento);

    List<Cliente> buscarPorUsuarioId(UUID usuarioId);
}

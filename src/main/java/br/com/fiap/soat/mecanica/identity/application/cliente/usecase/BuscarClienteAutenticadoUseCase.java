package br.com.fiap.soat.mecanica.identity.application.cliente.usecase;

import br.com.fiap.soat.mecanica.identity.application.security.UsuarioAutenticadoPort;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.domain.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarClienteAutenticadoUseCase {

    private final UsuarioAutenticadoPort usuarioAutenticado;
    private final ClienteRepository clienteRepository;

    public Cliente executar() {
        return clienteRepository.buscarPorId(usuarioAutenticado.sub())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }
}

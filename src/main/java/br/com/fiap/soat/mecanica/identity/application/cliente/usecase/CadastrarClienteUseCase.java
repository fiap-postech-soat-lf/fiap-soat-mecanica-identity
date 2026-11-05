package br.com.fiap.soat.mecanica.identity.application.cliente.usecase;

import br.com.fiap.soat.mecanica.identity.application.security.UsuarioAutenticadoPort;
import br.com.fiap.soat.mecanica.identity.domain.cliente.Cliente;
import br.com.fiap.soat.mecanica.identity.domain.cliente.ClienteRepository;
import br.com.fiap.soat.mecanica.identity.domain.exception.RegraNegocioException;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.CNPJ;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.CPF;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.Email;
import br.com.fiap.soat.mecanica.identity.domain.valueobject.Telefone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CadastrarClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final UsuarioAutenticadoPort usuarioAutenticado;

    public Cliente executar(String nome, String cpf, String cnpj, String email, String telefone) {

        CPF cpfFormatado = cpf == null ? null : new CPF(cpf);
        CNPJ cnpjFormatado = cnpj == null ? null : new CNPJ(cnpj);

        if (cpfFormatado != null) {
            clienteRepository.buscarPorCpf(cpfFormatado)
                    .ifPresent(cliente -> {
                        throw new RegraNegocioException("CPF já existente");
                    });
        }

        if (cnpjFormatado != null) {
            clienteRepository.buscarPorCnpj(cnpjFormatado)
                    .ifPresent(cliente -> {
                        throw new RegraNegocioException("CNPJ já existente");
                    });
        }

        Cliente cliente = new Cliente(
                nome,
                cpfFormatado,
                cnpjFormatado,
                new Email(email),
                telefone == null ? null : new Telefone(telefone),
                usuarioAutenticado.sub()
        );
        return clienteRepository.salvar(cliente);
    }
}

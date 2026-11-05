package br.com.fiap.soat.mecanica.identity.domain.valueobject;

import br.com.fiap.soat.mecanica.identity.domain.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A regra do CPF e a mais restritiva entre o monolito e o Cpf do -auth: so 11 digitos ou 000.000.000-00. */
class CPFFormatoTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25 x", "529 982 247 25", "529-982-247.25", "529.98224725", "52998224725a",
            "(52998224725)", "529.982.247/25"})
    void rejeitaFormatosDiferentesDeOsOnzeDigitosOuAMascaraPadrao(String cpf) {
        assertThatThrownBy(() -> new CPF(cpf)).isInstanceOf(RegraNegocioException.class).hasMessage("CPF inválido");
    }

    @Test
    void aceitaOsDoisFormatosEIgnoraEspacosNasPontas() {
        assertThat(new CPF("  529.982.247-25 ").getValue()).isEqualTo("52998224725");
        assertThat(new CPF("52998224725").getValue()).isEqualTo("52998224725");
    }

    @Test
    void rejeitaDigitosRepetidos() {
        assertThatThrownBy(() -> new CPF("111.111.111-11")).isInstanceOf(RegraNegocioException.class);
    }
}

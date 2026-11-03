package br.com.fiap.soat.mecanica.identity.domain;

/**
 * TEMPORARIA (T16, item 5): codigo novo sem teste para o Quality Gate do SonarCloud reprovar o PR
 * pela cobertura do New Code. E removida no proximo commit do mesmo PR e nao chega na main.
 */
public final class ClasseTemporariaSemTeste {

    private ClasseTemporariaSemTeste() {
    }

    public static String classificar(int valor) {
        String faixa;
        if (valor < 0) {
            faixa = "negativo";
        } else if (valor == 0) {
            faixa = "zero";
        } else if (valor < 10) {
            faixa = "unidade";
        } else if (valor < 100) {
            faixa = "dezena";
        } else if (valor < 1000) {
            faixa = "centena";
        } else {
            faixa = "milhar ou mais";
        }
        StringBuilder texto = new StringBuilder(faixa);
        if (valor % 2 == 0) {
            texto.append(", par");
        } else {
            texto.append(", impar");
        }
        if (valor % 3 == 0) {
            texto.append(", multiplo de 3");
        }
        if (valor % 5 == 0) {
            texto.append(", multiplo de 5");
        }
        return texto.toString();
    }

    public static int somarDigitos(int valor) {
        int resto = Math.abs(valor);
        int soma = 0;
        while (resto > 0) {
            soma += resto % 10;
            resto /= 10;
        }
        return soma;
    }
}

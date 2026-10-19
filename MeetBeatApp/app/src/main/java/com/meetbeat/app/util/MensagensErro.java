package com.meetbeat.app.util;

/**
 * Mensagens de erro exibidas ao usuário. Os detalhes técnicos das exceções (em inglês, com
 * endereços e nomes de classes) não são mostrados, porque não ajudam a pessoa a resolver o problema.
 */
public class MensagensErro {

    private MensagensErro() {
    }

    public static final String SEM_CONEXAO =
            "Não foi possível conectar ao servidor. Verifique sua internet e tente novamente.";
}

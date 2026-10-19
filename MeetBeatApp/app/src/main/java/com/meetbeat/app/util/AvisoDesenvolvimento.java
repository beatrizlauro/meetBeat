package com.meetbeat.app.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Aviso padrão para os botões de funcionalidades previstas para os próximos módulos da plataforma
 * (busca, carteira de ingressos, caronas), para que o toque nunca fique sem resposta.
 */
public class AvisoDesenvolvimento {

    private AvisoDesenvolvimento() {
    }

    /** Ex.: mostrar(context, "A busca") exibe "A busca está em desenvolvimento e chega em breve!" */
    public static void mostrar(Context context, String funcionalidade) {
        Toast.makeText(context, funcionalidade + " está em desenvolvimento e chega em breve!",
                Toast.LENGTH_SHORT).show();
    }
}

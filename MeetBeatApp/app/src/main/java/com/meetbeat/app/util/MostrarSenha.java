package com.meetbeat.app.util;

import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;

import com.meetbeat.app.R;

/**
 * Botão de olho ao lado de um campo de senha, que alterna entre mostrar e ocultar o texto
 * digitado. Ajuda a pessoa a conferir a senha e evita erros de digitação.
 */
public class MostrarSenha {

    private MostrarSenha() {
    }

    public static void ligar(EditText campo, ImageView botao) {
        atualizarBotao(botao, false);
        botao.setOnClickListener(v -> {
            boolean visivel = !(campo.getTransformationMethod() instanceof PasswordTransformationMethod);
            boolean mostrar = !visivel;
            campo.setTransformationMethod(mostrar ? null : PasswordTransformationMethod.getInstance());
            campo.setSelection(campo.length()); // mantém o cursor no fim do texto
            atualizarBotao(botao, mostrar);
        });
    }

    private static void atualizarBotao(ImageView botao, boolean senhaVisivel) {
        botao.setImageResource(senhaVisivel ? R.drawable.ic_olho_fechado : R.drawable.ic_olho);
        botao.setContentDescription(senhaVisivel ? "Ocultar senha" : "Mostrar senha");
    }
}

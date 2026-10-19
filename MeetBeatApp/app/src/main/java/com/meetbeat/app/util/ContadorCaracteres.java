package com.meetbeat.app.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.meetbeat.app.R;

/**
 * Mostra, abaixo de um campo de texto, quantos caracteres já foram usados do limite (ex.: "42/300").
 * O limite em si é imposto pelo android:maxLength do campo; o contador fica vermelho ao atingi-lo.
 */
public class ContadorCaracteres {

    private ContadorCaracteres() {
    }

    public static void ligar(EditText campo, TextView contador, int limite) {
        TextWatcher observador = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                atualizar(contador, s.length(), limite);
            }
        };
        campo.addTextChangedListener(observador);
        atualizar(contador, campo.length(), limite);
    }

    private static void atualizar(TextView contador, int usados, int limite) {
        contador.setText(usados + "/" + limite + " caracteres");
        contador.setTextColor(ContextCompat.getColor(contador.getContext(),
                usados >= limite ? R.color.cor_erro : R.color.text_gray));
    }
}

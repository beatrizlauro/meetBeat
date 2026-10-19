package com.meetbeat.app.util;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class VisualHelper {

    // Gera uma cor consistente com base no nome, para o avatar
    public static int getCorAvatar(String nome) {
        if (nome == null || nome.isEmpty()) {
            return Color.GRAY;
        }
        int hash = nome.hashCode();
        float hue = Math.abs(hash % 360);
        return Color.HSVToColor(new float[]{hue, 0.55f, 0.85f});
    }

    // Exibe o texto (ex.: a bio) e esconde o campo quando ele está vazio
    public static void mostrarSeHouverTexto(TextView campo, String texto) {
        boolean temTexto = texto != null && !texto.trim().isEmpty();
        campo.setText(temTexto ? texto.trim() : "");
        campo.setVisibility(temTexto ? View.VISIBLE : View.GONE);
    }

    // Desativa um botão enquanto a requisição dele está em andamento (evita envio duplicado) e
    // deixa ele mais claro, para a pessoa perceber que o app está processando
    public static void definirOcupado(View botao, boolean ocupado) {
        botao.setEnabled(!ocupado);
        botao.setAlpha(ocupado ? 0.6f : 1f);
    }

    // Retorna a inicial do nome em maiúscula
    public static String getInicial(String nome) {
        if (nome == null || nome.isEmpty()) return "?";
        return String.valueOf(nome.charAt(0)).toUpperCase();
    }
}
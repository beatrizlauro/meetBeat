package com.meetbeat.app.util;

import android.graphics.Color;

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

    // Retorna a inicial do nome em maiúscula
    public static String getInicial(String nome) {
        if (nome == null || nome.isEmpty()) return "?";
        return String.valueOf(nome.charAt(0)).toUpperCase();
    }
}
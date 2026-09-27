package com.meetbeat.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemePreference {

    private static final String PREFS_NAME = "meetbeat_prefs";
    private static final String KEY_MODO_ESCURO = "modo_escuro_ativado";

    public static boolean isModoEscuroAtivado(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MODO_ESCURO, false);
    }

    // Aplica a preferência salva, ignorando o tema do sistema. Chamado no início do app.
    public static void aplicarSalva(Context context) {
        AppCompatDelegate.setDefaultNightMode(isModoEscuroAtivado(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Inverte o modo atual, salva a escolha e aplica imediatamente.
    public static void alternar(Context context) {
        boolean novoValor = !isModoEscuroAtivado(context);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MODO_ESCURO, novoValor).apply();

        AppCompatDelegate.setDefaultNightMode(novoValor
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}

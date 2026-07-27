package com.meetbeat.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {

    private static final String PREFS_NAME = "meetbeat_prefs";
    private static final String KEY_USER_ID = "usuario_logado_id";
    private static final long ID_PADRAO = 12; // Jungkook, usado na primeira vez

    public static long getUsuarioLogado(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_USER_ID, ID_PADRAO);
    }

    public static void salvarUsuarioLogado(Context context, long id) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_USER_ID, id).apply();
    }
}
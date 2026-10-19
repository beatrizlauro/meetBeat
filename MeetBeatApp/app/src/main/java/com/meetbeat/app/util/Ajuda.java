package com.meetbeat.app.util;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Explicação curta do funcionamento do matching, acessível pela dica do feed e pelo menu do perfil
 * (heurística de Nielsen "ajuda e documentação").
 */
public class Ajuda {

    private Ajuda() {
    }

    public static void mostrarComoFunciona(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Como funciona o Meet & Beat")
                .setMessage("• Sugestões: o feed mostra pessoas que curtem os mesmos gêneros musicais que você, "
                        + "começando por quem tem mais gêneros em comum.\n\n"
                        + "• Ser amigo: curte o perfil. Se a pessoa também curtir você, vira um match e ela "
                        + "aparece em \"Meus matches\".\n\n"
                        + "• Não curti: tira a pessoa das suas sugestões. Logo depois, dá para tocar em "
                        + "\"Desfazer\" caso tenha sido sem querer.\n\n"
                        + "• Curtidas: em \"Curtidas recebidas\" você vê quem curtiu você e pode curtir de volta; "
                        + "em \"Curtidas enviadas\", quem você curtiu e ainda não respondeu.\n\n"
                        + "• Dica: mais gêneros no seu perfil geram mais sugestões. Você pode editá-los pelo "
                        + "menu do perfil.")
                .setPositiveButton("Entendi", null)
                .show();
    }
}

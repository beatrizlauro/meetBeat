package com.meetbeat.app.util;

import com.meetbeat.app.R;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Escolhe o ícone de cada gênero musical. Todos os ícones ic_genero_* seguem o mesmo estilo
 * (traço de 1.8 numa grade 24x24), para que a grade de gêneros fique padronizada.
 */
public class IconeGenero {

    // Nome do gênero (sem acento e em minúsculas) -> ícone
    private static final Map<String, Integer> POR_NOME = new HashMap<>();

    // Palavras-chave usadas para gêneros que ainda não estão no mapa acima
    // (ex.: "Pop Rock" cai em guitarra, "Funk Carioca" em caixa de som).
    // A ordem importa: a primeira palavra encontrada no nome define o ícone.
    private static final Map<String, Integer> POR_PALAVRA = new LinkedHashMap<>();

    static {
        registrar(R.drawable.ic_genero_guitarra, "rock", "rock alternativo", "grunge", "punk", "hard rock");
        registrar(R.drawable.ic_genero_raio, "metal");
        registrar(R.drawable.ic_genero_violao, "mpb", "bossa nova", "folk");
        registrar(R.drawable.ic_genero_pandeiro, "samba", "pagode");
        registrar(R.drawable.ic_genero_sanfona, "forro", "piseiro");
        registrar(R.drawable.ic_genero_chapeu, "sertanejo", "country");
        registrar(R.drawable.ic_genero_saxofone, "jazz", "blues");
        registrar(R.drawable.ic_genero_microfone, "pop", "r&b", "soul");
        registrar(R.drawable.ic_genero_lightstick, "k-pop", "j-pop");
        registrar(R.drawable.ic_genero_caixa_som, "hip-hop", "rap", "trap", "funk");
        registrar(R.drawable.ic_genero_toca_discos, "house", "techno");
        registrar(R.drawable.ic_genero_equalizador, "eletronica");
        registrar(R.drawable.ic_genero_bateria, "drum and bass");
        registrar(R.drawable.ic_genero_maracas, "reggaeton", "axe");
        registrar(R.drawable.ic_genero_palmeira, "reggae");
        registrar(R.drawable.ic_genero_cassete, "indie");
        registrar(R.drawable.ic_genero_fones, "lo-fi");
        registrar(R.drawable.ic_genero_coracao_partido, "emo");
        registrar(R.drawable.ic_genero_coracao, "brega", "arrocha");
        registrar(R.drawable.ic_genero_piano, "classica");
        registrar(R.drawable.ic_genero_cruz, "gospel");

        POR_PALAVRA.put("metal", R.drawable.ic_genero_raio);
        POR_PALAVRA.put("rock", R.drawable.ic_genero_guitarra);
        POR_PALAVRA.put("punk", R.drawable.ic_genero_guitarra);
        POR_PALAVRA.put("k-pop", R.drawable.ic_genero_lightstick);
        POR_PALAVRA.put("kpop", R.drawable.ic_genero_lightstick);
        POR_PALAVRA.put("sertanejo", R.drawable.ic_genero_chapeu);
        POR_PALAVRA.put("forro", R.drawable.ic_genero_sanfona);
        POR_PALAVRA.put("samba", R.drawable.ic_genero_pandeiro);
        POR_PALAVRA.put("pagode", R.drawable.ic_genero_pandeiro);
        POR_PALAVRA.put("funk", R.drawable.ic_genero_caixa_som);
        POR_PALAVRA.put("rap", R.drawable.ic_genero_caixa_som);
        POR_PALAVRA.put("hip", R.drawable.ic_genero_caixa_som);
        POR_PALAVRA.put("jazz", R.drawable.ic_genero_saxofone);
        POR_PALAVRA.put("eletr", R.drawable.ic_genero_equalizador);
        POR_PALAVRA.put("house", R.drawable.ic_genero_toca_discos);
        POR_PALAVRA.put("reggae", R.drawable.ic_genero_palmeira);
        POR_PALAVRA.put("gospel", R.drawable.ic_genero_cruz);
        POR_PALAVRA.put("pop", R.drawable.ic_genero_microfone);
    }

    private IconeGenero() {
    }

    private static void registrar(int icone, String... nomes) {
        for (String nome : nomes) {
            POR_NOME.put(nome, icone);
        }
    }

    public static int para(String nomeGenero) {
        String nome = normalizar(nomeGenero);

        Integer exato = POR_NOME.get(nome);
        if (exato != null) return exato;

        for (Map.Entry<String, Integer> entrada : POR_PALAVRA.entrySet()) {
            if (nome.contains(entrada.getKey())) return entrada.getValue();
        }
        return R.drawable.ic_genero_nota;
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}

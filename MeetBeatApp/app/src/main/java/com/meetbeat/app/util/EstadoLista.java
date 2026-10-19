package com.meetbeat.app.util;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.meetbeat.app.R;

/**
 * Controla o layout view_estado_lista, que ocupa o lugar de uma lista enquanto ela carrega,
 * quando ela vem vazia ou quando a requisição falha.
 */
public class EstadoLista {

    private final View raiz;
    private final View progresso;
    private final ImageView icone;
    private final TextView titulo;
    private final TextView subtitulo;
    private final TextView acao;
    private final View lista;

    public EstadoLista(Activity activity, View lista) {
        this(activity.findViewById(android.R.id.content), lista);
    }

    /** Para fragments: procura o layout dentro da view do próprio fragment, e não na Activity inteira. */
    public EstadoLista(View container, View lista) {
        this.lista = lista;
        raiz = container.findViewById(R.id.estadoLista);
        progresso = container.findViewById(R.id.progressEstadoLista);
        icone = container.findViewById(R.id.imageEstadoLista);
        titulo = container.findViewById(R.id.textTituloEstadoLista);
        subtitulo = container.findViewById(R.id.textSubtituloEstadoLista);
        acao = container.findViewById(R.id.btnAcaoEstadoLista);
    }

    public void carregando() {
        lista.setVisibility(View.GONE);
        raiz.setVisibility(View.VISIBLE);
        progresso.setVisibility(View.VISIBLE);
        icone.setVisibility(View.GONE);
        titulo.setVisibility(View.GONE);
        subtitulo.setVisibility(View.GONE);
        acao.setVisibility(View.GONE);
    }

    /** Exibe a mensagem; {@code textoAcao} nulo esconde o botão. */
    public void mostrar(int iconeRes, String textoTitulo, String textoSubtitulo, String textoAcao, Runnable aoTocar) {
        lista.setVisibility(View.GONE);
        raiz.setVisibility(View.VISIBLE);
        progresso.setVisibility(View.GONE);

        icone.setImageResource(iconeRes);
        icone.setVisibility(View.VISIBLE);
        titulo.setText(textoTitulo);
        titulo.setVisibility(View.VISIBLE);
        subtitulo.setText(textoSubtitulo);
        subtitulo.setVisibility(textoSubtitulo == null ? View.GONE : View.VISIBLE);

        if (textoAcao == null) {
            acao.setVisibility(View.GONE);
        } else {
            acao.setText(textoAcao);
            acao.setOnClickListener(v -> aoTocar.run());
            acao.setVisibility(View.VISIBLE);
        }
    }

    /** Mensagem padrão de falha, com botão para tentar novamente. */
    public void erro(Runnable tentarDeNovo) {
        mostrar(R.drawable.ic_genero_nota, "Não foi possível carregar",
                "Verifique sua conexão com a internet e tente novamente.", "Tentar de novo", tentarDeNovo);
    }

    public void esconder() {
        raiz.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);
    }
}

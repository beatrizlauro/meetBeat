package com.meetbeat.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.R;
import com.meetbeat.app.model.Genero;
import com.meetbeat.app.util.IconeGenero;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GeneroCardAdapter extends RecyclerView.Adapter<GeneroCardAdapter.ViewHolder> {

    // "todos" guarda a lista completa vinda da API; "generos" é o que está visível
    // na tela depois de aplicado o termo de busca.
    private final List<Genero> todos = new ArrayList<>();
    private final List<Genero> generos = new ArrayList<>();
    private final Set<Long> selecionados = new HashSet<>();
    private String termoBusca = "";

    public GeneroCardAdapter(List<Genero> generos) {
        definirGeneros(generos);
    }

    public void definirGeneros(List<Genero> lista) {
        todos.clear();
        todos.addAll(lista);
        aplicarFiltro();
    }

    // A seleção fica guardada por id, então continua valendo mesmo para os gêneros
    // que a busca escondeu no momento.
    public void filtrar(String termo) {
        termoBusca = normalizar(termo);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        generos.clear();
        for (Genero genero : todos) {
            if (termoBusca.isEmpty() || normalizar(genero.getNome()).contains(termoBusca)) {
                generos.add(genero);
            }
        }
        notifyDataSetChanged();
    }

    // Ignora maiúsculas e acentos, para que "forro" encontre "Forró" e "eletronica"
    // encontre "Eletrônica".
    private static String normalizar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    public Set<Long> getSelecionados() {
        return selecionados;
    }

    public void definirSelecionados(Set<Long> ids) {
        selecionados.clear();
        selecionados.addAll(ids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genero_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Genero genero = generos.get(position);

        holder.textNome.setText(genero.getNome());
        holder.imageIcone.setImageResource(IconeGenero.para(genero.getNome()));

        boolean selecionado = selecionados.contains(genero.getId());
        holder.card.setBackgroundResource(selecionado ? R.drawable.bg_card_genero_selecionado : R.drawable.bg_card_genero);
        holder.textNome.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                selecionado ? R.color.vidro_conteudo_claro : R.color.text_dark));

        // Sobrescreve a cor "de fábrica" de cada ícone (varia de um pra outro) para manter
        // o mesmo padrão em todos os cards, e trocar para branco quando selecionado, já que
        // o cinza padrão fica ilegível em cima do fundo roxo do card selecionado.
        holder.imageIcone.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(),
                selecionado ? R.color.vidro_conteudo_claro : R.color.text_gray));

        holder.card.setOnClickListener(v -> {
            if (selecionados.contains(genero.getId())) {
                selecionados.remove(genero.getId());
            } else {
                selecionados.add(genero.getId());
            }
            notifyItemChanged(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return generos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;
        ImageView imageIcone;
        TextView textNome;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardGenero);
            imageIcone = itemView.findViewById(R.id.imageIconeGenero);
            textNome = itemView.findViewById(R.id.textNomeGenero);
        }
    }
}

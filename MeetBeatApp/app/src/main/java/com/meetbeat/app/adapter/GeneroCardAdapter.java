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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneroCardAdapter extends RecyclerView.Adapter<GeneroCardAdapter.ViewHolder> {

    private final List<Genero> generos;
    private final Set<Long> selecionados = new HashSet<>();

    public GeneroCardAdapter(List<Genero> generos) {
        this.generos = generos;
    }

    public Set<Long> getSelecionados() {
        return selecionados;
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
        holder.imageIcone.setImageResource(escolherIcone(genero.getNome()));

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
            notifyItemChanged(position);
        });
    }

    private int escolherIcone(String nomeGenero) {
        String nome = nomeGenero.toLowerCase();
        if (nome.contains("bts") || nome.contains("k-pop")) return R.drawable.ic_mic;
        if (nome.contains("blackpink") || nome.contains("blink")) return R.drawable.ic_heart;
        if (nome.contains("hip-hop")) return R.drawable.ic_headphones;
        if (nome.contains("pop")) return R.drawable.ic_music_note;
        if (nome.contains("r&b")) return R.drawable.ic_headphones;
        return R.drawable.ic_music_note;
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

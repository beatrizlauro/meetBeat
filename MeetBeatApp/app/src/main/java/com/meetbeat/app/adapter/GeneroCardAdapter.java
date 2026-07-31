package com.meetbeat.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.textEmoji.setText(escolherEmoji(genero.getNome()));

        boolean selecionado = selecionados.contains(genero.getId());
        holder.card.setBackgroundResource(selecionado ? R.drawable.bg_card_genero_selecionado : R.drawable.bg_card_genero);

        holder.card.setOnClickListener(v -> {
            if (selecionados.contains(genero.getId())) {
                selecionados.remove(genero.getId());
            } else {
                selecionados.add(genero.getId());
            }
            notifyItemChanged(position);
        });
    }

    private String escolherEmoji(String nomeGenero) {
        String nome = nomeGenero.toLowerCase();
        if (nome.contains("bts") || nome.contains("k-pop")) return "🎤";
        if (nome.contains("blackpink") || nome.contains("blink")) return "💜";
        if (nome.contains("hip-hop")) return "🎧";
        if (nome.contains("pop")) return "🎶";
        if (nome.contains("r&b")) return "🎹";
        return "🎵";
    }

    @Override
    public int getItemCount() {
        return generos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;
        TextView textEmoji, textNome;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardGenero);
            textEmoji = itemView.findViewById(R.id.textEmojiGenero);
            textNome = itemView.findViewById(R.id.textNomeGenero);
        }
    }
}
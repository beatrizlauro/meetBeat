package com.meetbeat.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.R;
import com.meetbeat.app.model.MatchSuggestion;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    public interface OnMatchActionListener {
        void onCurtir(MatchSuggestion pessoa);
        void onRejeitar(MatchSuggestion pessoa);
    }

    private final List<MatchSuggestion> lista;
    private final OnMatchActionListener listener;

    public MatchAdapter(List<MatchSuggestion> lista, OnMatchActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_card, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchSuggestion pessoa = lista.get(position);

        holder.textNome.setText(pessoa.getNome());
        holder.textScore.setText(pessoa.getScore() + " gênero(s) em comum");

        holder.btnCurtir.setOnClickListener(v -> listener.onCurtir(pessoa));
        holder.btnRejeitar.setOnClickListener(v -> listener.onRejeitar(pessoa));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView textNome, textScore, btnCurtir, btnRejeitar;

        MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNome);
            textScore = itemView.findViewById(R.id.textScore);
            btnCurtir = itemView.findViewById(R.id.btnCurtir);
            btnRejeitar = itemView.findViewById(R.id.btnRejeitar);
        }
    }
}